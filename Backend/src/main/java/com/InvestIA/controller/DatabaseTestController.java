package com.InvestIA.controller;

import com.InvestIA.dto.investimento.CriarInvestimentoRequest;
import com.InvestIA.dto.investimento.InvestimentoResponse;
import com.InvestIA.entity.Ativo;
import com.InvestIA.entity.Investimento;
import com.InvestIA.entity.Usuario;
import com.InvestIA.repository.AtivoRepository;
import com.InvestIA.repository.InvestimentoRepository;
import com.InvestIA.repository.UsuarioRepository;
import com.InvestIA.service.InvestimentoService;
import com.InvestIA.util.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/database-test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Database Test", description = "Testes de integridade do banco de dados")
public class DatabaseTestController {

    private final UsuarioRepository usuarioRepository;
    private final AtivoRepository ativoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final InvestimentoService investimentoService;

    @GetMapping("/health")
    @Operation(summary = "Verificar saúde do banco de dados")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            long totalUsuarios = usuarioRepository.count();
            long totalAtivos = ativoRepository.count();
            long totalInvestimentos = investimentoRepository.count();

            return ResponseEntity.ok(Map.of(
                "status", "OK",
                "database", "H2",
                "tables", Map.of(
                    "usuarios", totalUsuarios,
                    "ativos", totalAtivos,
                    "investimentos", totalInvestimentos
                ),
                "message", "Banco de dados funcionando perfeitamente",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Erro no health check: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar todos os usuários de teste")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            log.info("Encontrados {} usuários", usuarios.size());
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            log.error("Erro ao listar usuários: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar todos os ativos disponíveis")
    public ResponseEntity<List<Ativo>> listarAtivos() {
        try {
            List<Ativo> ativos = ativoRepository.findAll();
            log.info("Encontrados {} ativos", ativos.size());
            return ResponseEntity.ok(ativos);
        } catch (Exception e) {
            log.error("Erro ao listar ativos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/investimentos")
    @Operation(summary = "Listar todos os investimentos")
    public ResponseEntity<List<Investimento>> listarInvestimentos() {
        try {
            List<Investimento> investimentos = investimentoRepository.findAll();
            log.info("Encontrados {} investimentos", investimentos.size());
            return ResponseEntity.ok(investimentos);
        } catch (Exception e) {
            log.error("Erro ao listar investimentos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/criar-investimento-teste")
    @Operation(summary = "Criar investimento de teste para validação")
    public ResponseEntity<Object> criarInvestimentoTeste(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("Criando investimento de teste para usuário: {}", usuarioId);

            // Criar request de teste
            CriarInvestimentoRequest request = CriarInvestimentoRequest.builder()
                .ticker("PETR4.SA")
                .quantidade(50)
                .valorCompra(BigDecimal.valueOf(33.00))
                .dataCompra(LocalDate.now())
                .build();

            // Criar investimento usando o service
            InvestimentoResponse investimento = investimentoService.criar(usuarioId, request);
            
            log.info("Investimento de teste criado com sucesso: {}", investimento.getId());
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Investimento de teste criado com sucesso",
                "investimento", investimento,
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("ERRO ao criar investimento de teste: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", "Erro ao criar investimento: " + e.getMessage(),
                "details", e.getClass().getSimpleName(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @GetMapping("/validar-foreign-keys")
    @Operation(summary = "Validar integridade das foreign keys")
    public ResponseEntity<Map<String, Object>> validarForeignKeys() {
        try {
            // Verificar se existem investimentos órfãos (sem usuário)
            long investimentosSemUsuario = investimentoRepository.countByUsuarioIsNull();
            
            // Verificar se existem investimentos sem ativo
            long investimentosSemAtivo = investimentoRepository.countByAtivoIsNull();
            
            // Verificar se existem perfis órfãos
            long perfisSemUsuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getPerfil() == null)
                .mapToLong(u -> 1)
                .sum();

            boolean integridadeOk = investimentosSemUsuario == 0 && 
                                  investimentosSemAtivo == 0;

            return ResponseEntity.ok(Map.of(
                "integridade", integridadeOk ? "OK" : "ERRO",
                "detalhes", Map.of(
                    "investimentos_sem_usuario", investimentosSemUsuario,
                    "investimentos_sem_ativo", investimentosSemAtivo,
                    "usuarios_sem_perfil", perfisSemUsuario
                ),
                "message", integridadeOk ? "Todas as foreign keys estão corretas" : "Existem problemas de integridade",
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("Erro na validação de foreign keys: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PostMapping("/testar-cenario-real")
    @Operation(summary = "Testar cenário específico: R$ 19.000, 1000 ações, cotação R$ 25")
    public ResponseEntity<Map<String, Object>> testarCenarioReal(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("🧪 Testando cenário: R$ 19.000 investido, 1000 ações, cotação R$ 25");
            
            // Limpar investimentos anteriores
            investimentoRepository.deleteAll();
            
            // Buscar ou criar ativo
            Ativo ativo = ativoRepository.findByTicker("TESTE.SA")
                .orElseGet(() -> ativoRepository.save(Ativo.builder()
                    .ticker("TESTE.SA")
                    .simbolo("TESTE")
                    .nome("Empresa Teste")
                    .tipoAtivo(com.InvestIA.enums.TipoAtivo.ACAO)
                    .setor(com.InvestIA.enums.SetorAtivo.TECNOLOGIA)
                    .precoAtual(BigDecimal.valueOf(25.00)) // Cotação atual R$ 25
                    .status(true)
                    .build()));
            
            // Buscar usuário
            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Criar investimento: R$ 19.000 investido, 1000 ações (preço médio R$ 19)
            Investimento investimento = Investimento.builder()
                .usuario(usuario)
                .ativo(ativo)
                .quantidade(1000)
                .valorMedioCompra(BigDecimal.valueOf(19.00)) // Comprou a R$ 19
                .valorTotalInvestido(BigDecimal.valueOf(19000.00)) // Total investido R$ 19.000
                .valorAtual(BigDecimal.valueOf(25.00)) // Cotação atual R$ 25
                .dataCompra(LocalDate.now())
                .ativoStatus(true)
                .build();
            
            investimento = investimentoRepository.save(investimento);
            
            // Calcular valores corretos
            BigDecimal cotacaoAtual = BigDecimal.valueOf(25.00);
            int quantidade = 1000;
            BigDecimal valorTotalAtual = cotacaoAtual.multiply(BigDecimal.valueOf(quantidade)); // R$ 25.000
            BigDecimal valorInvestido = BigDecimal.valueOf(19000.00);
            BigDecimal lucroPreju = valorTotalAtual.subtract(valorInvestido); // R$ 6.000
            BigDecimal percentual = lucroPreju.divide(valorInvestido, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)); // 31.58%
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Cenário de teste criado",
                "investimento", Map.of(
                    "id", investimento.getId(),
                    "ticker", ativo.getTicker(),
                    "quantidade", quantidade,
                    "valorMedioCompra", BigDecimal.valueOf(19.00),
                    "cotacaoAtual", cotacaoAtual,
                    "valorTotalInvestido", valorInvestido,
                    "valorTotalAtual", valorTotalAtual,
                    "lucroPreju", lucroPreju,
                    "percentualLucroPreju", percentual
                ),
                "calculo", Map.of(
                    "formula", "Valor Total Atual = Cotação Atual × Quantidade",
                    "valorTotalAtual", cotacaoAtual + " × " + quantidade + " = " + valorTotalAtual,
                    "lucroFormula", "Lucro = Valor Total Atual - Valor Investido",
                    "lucroCalculo", valorTotalAtual + " - " + valorInvestido + " = " + lucroPreju,
                    "percentualFormula", "Percentual = (Lucro ÷ Valor Investido) × 100",
                    "percentualCalculo", "(" + lucroPreju + " ÷ " + valorInvestido + ") × 100 = " + percentual + "%"
                ),
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("❌ Erro no teste de cenário: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PostMapping("/reset-database")
    @Operation(summary = "Resetar banco de dados para estado inicial")
    public ResponseEntity<Map<String, Object>> resetDatabase() {
        try {
            log.info("🔄 Iniciando reset do banco de dados...");
            
            // Contar registros antes
            long usuariosAntes = usuarioRepository.count();
            long ativosAntes = ativoRepository.count(); 
            long investimentosAntes = investimentoRepository.count();
            
            // Limpar dados (em ordem devido às foreign keys)
            investimentoRepository.deleteAll();
            // Não deletar usuários e ativos, apenas investimentos para reset parcial
            
            log.info("✅ Banco resetado com sucesso");
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Banco de dados resetado com sucesso",
                "antes", Map.of(
                    "usuarios", usuariosAntes,
                    "ativos", ativosAntes,
                    "investimentos", investimentosAntes
                ),
                "depois", Map.of(
                    "usuarios", usuarioRepository.count(),
                    "ativos", ativoRepository.count(),
                    "investimentos", investimentoRepository.count()
                ),
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("❌ Erro no reset do banco: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}