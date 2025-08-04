package com.InvestIA.controller;

import com.InvestIA.entity.Ativo;
import com.InvestIA.entity.Investimento;
import com.InvestIA.entity.Usuario;
import com.InvestIA.enums.TipoAtivo;
import com.InvestIA.enums.SetorAtivo;
import com.InvestIA.repository.AtivoRepository;
import com.InvestIA.repository.InvestimentoRepository;
import com.InvestIA.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test/investment")
@RequiredArgsConstructor
@Slf4j
public class TestInvestmentController {

    private final UsuarioRepository usuarioRepository;
    private final InvestimentoRepository investimentoRepository;
    private final AtivoRepository ativoRepository;

    @PostMapping("/create-sample/{email}")
    public ResponseEntity<Object> createSampleInvestments(@PathVariable String email) {
        try {
            log.info("🎯 Criando investimentos de exemplo para: {}", email);
            
            // Buscar usuário
            Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
            
            log.info("✅ Usuário encontrado: {}", usuario.getId());
            
            // Criar ou buscar ativos
            Ativo petr4 = criarOuBuscarAtivo("PETR4", "PETR4.SA", "Petrobras PN", TipoAtivo.ACAO, SetorAtivo.ENERGIA, BigDecimal.valueOf(32.45));
            Ativo vale3 = criarOuBuscarAtivo("VALE3", "VALE3.SA", "Vale ON", TipoAtivo.ACAO, SetorAtivo.MATERIAIS_BASICOS, BigDecimal.valueOf(58.12));
            Ativo itub4 = criarOuBuscarAtivo("ITUB4", "ITUB4.SA", "Itaú Unibanco PN", TipoAtivo.ACAO, SetorAtivo.FINANCEIRO, BigDecimal.valueOf(30.25));
            
            // Remover investimentos existentes para evitar duplicatas
            List<Investimento> existentes = investimentoRepository.findByUsuarioId(usuario.getId());
            if (!existentes.isEmpty()) {
                investimentoRepository.deleteAll(existentes);
                log.info("🗑️ Removidos {} investimentos existentes", existentes.size());
            }
            
            // Criar investimentos
            Investimento inv1 = Investimento.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .ativo(petr4)
                .quantidade(100)
                .valorMedioCompra(BigDecimal.valueOf(32.50))
                .valorAtual(BigDecimal.valueOf(32.45))
                .valorTotalInvestido(BigDecimal.valueOf(3250.00))
                .dataCompra(LocalDate.of(2024, 1, 15))
                .ativoStatus(true)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
                
            Investimento inv2 = Investimento.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .ativo(vale3)
                .quantidade(50)
                .valorMedioCompra(BigDecimal.valueOf(55.00))
                .valorAtual(BigDecimal.valueOf(58.12))
                .valorTotalInvestido(BigDecimal.valueOf(2750.00))
                .dataCompra(LocalDate.of(2024, 2, 10))
                .ativoStatus(true)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
                
            Investimento inv3 = Investimento.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .ativo(itub4)
                .quantidade(150)
                .valorMedioCompra(BigDecimal.valueOf(28.00))
                .valorAtual(BigDecimal.valueOf(30.25))
                .valorTotalInvestido(BigDecimal.valueOf(4200.00))
                .dataCompra(LocalDate.of(2024, 3, 5))
                .ativoStatus(true)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
            
            // Salvar investimentos
            investimentoRepository.save(inv1);
            investimentoRepository.save(inv2);
            investimentoRepository.save(inv3);
            
            log.info("✅ Criados 3 investimentos de exemplo");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Investimentos criados com sucesso!",
                "userId", usuario.getId(),
                "investments", List.of(
                    Map.of("ativo", "PETR4", "quantidade", 100, "valor", 3250.00),
                    Map.of("ativo", "VALE3", "quantidade", 50, "valor", 2750.00),
                    Map.of("ativo", "ITUB4", "quantidade", 150, "valor", 4200.00)
                )
            ));
            
        } catch (Exception e) {
            log.error("❌ Erro ao criar investimentos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erro: " + e.getMessage()
            ));
        }
    }
    
    private Ativo criarOuBuscarAtivo(String ticker, String simbolo, String nome, TipoAtivo tipo, SetorAtivo setor, BigDecimal preco) {
        return ativoRepository.findByTicker(ticker)
            .orElseGet(() -> {
                Ativo ativo = Ativo.builder()
                    .id(UUID.randomUUID())
                    .ticker(ticker)
                    .simbolo(simbolo)
                    .nome(nome)
                    .tipoAtivo(tipo)
                    .setor(setor)
                    .precoAtual(preco)
                    .status(true)
                    .criadoEm(LocalDateTime.now())
                    .build();
                return ativoRepository.save(ativo);
            });
    }
}