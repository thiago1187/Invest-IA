package com.InvestIA.controller;

import com.InvestIA.dto.dashboard.*;
import com.InvestIA.service.DashboardService;
import com.InvestIA.service.HistoricoPrecoService;
import com.InvestIA.util.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Dashboard", description = "Dashboard do usuário")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private HistoricoPrecoService historicoPrecoService;
    
    @GetMapping
    @Operation(summary = "Obter dados completos do dashboard")
    public ResponseEntity<Object> obterDashboard(Authentication authentication) {
        try {
            log.info("🔍 Tentando obter dashboard para usuário autenticado");
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("✅ Usuário autenticado: {}", usuarioId);
            DashboardResponse dashboard = dashboardService.obterDashboard(usuarioId);
            log.info("✅ Dashboard obtido com sucesso");
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("❌ Erro ao obter dashboard: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erro interno: " + e.getMessage(),
                "timestamp", System.currentTimeMillis(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }
    
    @GetMapping("/recomendacoes")
    @Operation(summary = "Obter recomendações personalizadas com IA")
    public ResponseEntity<RecomendacoesResponse> obterRecomendacoes(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            RecomendacoesResponse recomendacoes = dashboardService.obterRecomendacoes(usuarioId);
            return ResponseEntity.ok(recomendacoes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/alertas")
    @Operation(summary = "Obter alertas inteligentes")
    public ResponseEntity<AlertasResponse> obterAlertas(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            AlertasResponse alertas = dashboardService.obterAlertas(usuarioId);
            return ResponseEntity.ok(alertas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/performance")
    @Operation(summary = "Obter análise detalhada de performance")
    public ResponseEntity<PerformanceResponse> obterPerformance(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            PerformanceResponse performance = dashboardService.obterPerformanceDetalhada(usuarioId);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @PostMapping("/atualizar")
    @Operation(summary = "Forçar atualização dos dados em tempo real")
    public ResponseEntity<Void> atualizarDados(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            dashboardService.atualizarDadosTempoReal(usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/test-data")
    @Operation(summary = "Criar dados de teste para o usuário")
    public ResponseEntity<String> criarDadosTeste(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("Criando dados de teste para usuário: {}", usuarioId);
            
            // Primeiro, verificar se já existem ativos
            Integer countAtivos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ativos", Integer.class);
            if (countAtivos == 0) {
                // Inserir ativos
                String[] ativosSql = {
                    "INSERT INTO ativos (id, nome, simbolo, ticker, tipo_ativo, setor, preco_atual, variacao_diaria, variacao_mensal, variacao_anual, criado_em, atualizado_em, status) VALUES (RANDOM_UUID(), 'Vale SA', 'VALE3', 'VALE3.SA', 'ACAO', 'MINERACAO', 63.50, -2.3, 5.7, 15.2, NOW(), NOW(), 'ATIVO')",
                    "INSERT INTO ativos (id, nome, simbolo, ticker, tipo_ativo, setor, preco_atual, variacao_diaria, variacao_mensal, variacao_anual, criado_em, atualizado_em, status) VALUES (RANDOM_UUID(), 'Petrobras PN', 'PETR4', 'PETR4.SA', 'ACAO', 'PETROLEO_GAS', 35.80, 1.2, -3.1, 22.5, NOW(), NOW(), 'ATIVO')",
                    "INSERT INTO ativos (id, nome, simbolo, ticker, tipo_ativo, setor, preco_atual, variacao_diaria, variacao_mensal, variacao_anual, criado_em, atualizado_em, status) VALUES (RANDOM_UUID(), 'Itaú Unibanco PN', 'ITUB4', 'ITUB4.SA', 'ACAO', 'BANCOS', 25.90, 0.8, 2.4, 8.9, NOW(), NOW(), 'ATIVO')"
                };
                
                // Também atualizar ativos existentes que podem ter setor null
                jdbcTemplate.execute("UPDATE ativos SET setor = 'PETROLEO_GAS' WHERE simbolo = 'PETR4'");
                jdbcTemplate.execute("UPDATE ativos SET setor = 'MINERACAO' WHERE simbolo = 'VALE3'");
                jdbcTemplate.execute("UPDATE ativos SET setor = 'BANCOS' WHERE simbolo = 'ITUB4'");
                
                for (String sql : ativosSql) {
                    jdbcTemplate.execute(sql);
                }
                log.info("Ativos criados com sucesso");
            }
            
            // Verificar se já existem investimentos para este usuário
            Integer countInvestimentos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM investimentos WHERE usuario_id = ?", Integer.class, usuarioId);
                
            if (countInvestimentos == 0) {
                // Inserir investimentos
                String investimentosSql = """
                    INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_total_investido, valor_atual, data_compra, ativo_status, criado_em, atualizado_em)
                    SELECT 
                        RANDOM_UUID(),
                        ?,
                        a.id,
                        CASE 
                            WHEN a.ticker = 'VALE3.SA' THEN 100
                            WHEN a.ticker = 'PETR4.SA' THEN 150  
                            WHEN a.ticker = 'ITUB4.SA' THEN 200
                        END as quantidade,
                        CASE 
                            WHEN a.ticker = 'VALE3.SA' THEN 60.00
                            WHEN a.ticker = 'PETR4.SA' THEN 38.50
                            WHEN a.ticker = 'ITUB4.SA' THEN 24.20
                        END as valor_medio_compra,
                        CASE 
                            WHEN a.ticker = 'VALE3.SA' THEN 6000.00
                            WHEN a.ticker = 'PETR4.SA' THEN 5775.00
                            WHEN a.ticker = 'ITUB4.SA' THEN 4840.00
                        END as valor_total_investido,
                        CASE 
                            WHEN a.ticker = 'VALE3.SA' THEN 6350.00
                            WHEN a.ticker = 'PETR4.SA' THEN 5370.00
                            WHEN a.ticker = 'ITUB4.SA' THEN 5180.00
                        END as valor_atual,
                        CASE 
                            WHEN a.ticker = 'VALE3.SA' THEN '2024-01-15'
                            WHEN a.ticker = 'PETR4.SA' THEN '2024-02-20'
                            WHEN a.ticker = 'ITUB4.SA' THEN '2024-03-10'
                        END as data_compra,
                        true as ativo_status,
                        NOW() as criado_em,
                        NOW() as atualizado_em
                    FROM ativos a  
                    WHERE a.ticker IN ('VALE3.SA', 'PETR4.SA', 'ITUB4.SA')
                    """;
                
                jdbcTemplate.update(investimentosSql, usuarioId);
                log.info("Investimentos criados com sucesso para usuário: {}", usuarioId);
            }
            
            return ResponseEntity.ok("Dados de teste criados com sucesso! Total de investimentos: " + 
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM investimentos WHERE usuario_id = ?", Integer.class, usuarioId));
                
        } catch (Exception e) {
            log.error("Erro ao criar dados de teste: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao criar dados de teste: " + e.getMessage());
        }
    }
    
    @PostMapping("/init-historico")
    @Operation(summary = "Inicializar histórico de preços para todos os ativos")
    public ResponseEntity<String> inicializarHistorico(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("Inicializando histórico de preços para todos os ativos");
            
            // Sincronizar histórico de 90 dias para todos os ativos
            historicoPrecoService.sincronizarTodosAtivos(90);
            
            return ResponseEntity.ok("Inicialização de histórico iniciada! Os dados serão processados em background.");
            
        } catch (Exception e) {
            log.error("Erro ao inicializar histórico: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao inicializar histórico: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check do dashboard")
    public ResponseEntity<String> healthCheck(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("✅ Health check OK para usuário: {}", usuarioId);
            return ResponseEntity.ok("Backend funcionando! Usuário autenticado: " + usuarioId);
        } catch (Exception e) {
            log.error("❌ Health check falhou: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro no health check: " + e.getMessage());
        }
    }
    
    @GetMapping("/debug")
    @Operation(summary = "Debug endpoint para diagnosticar problemas do frontend")
    public ResponseEntity<Object> debugEndpoint(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            
            // Buscar informações básicas
            Integer countAtivos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ativos", Integer.class);
            Integer countInvestimentos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM investimentos WHERE usuario_id = ?", Integer.class, usuarioId);
            Integer countHistorico = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM historico_precos", Integer.class);
            
            return ResponseEntity.ok(Map.of(
                "status", "OK",
                "usuarioId", usuarioId.toString(),
                "timestamp", System.currentTimeMillis(),
                "database", Map.of(
                    "ativos", countAtivos,
                    "investimentos", countInvestimentos,
                    "historicoPrecos", countHistorico
                ),
                "message", "Debug info para frontend"
            ));
            
        } catch (Exception e) {
            log.error("❌ Erro no debug endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @GetMapping("/public-health")
    @Operation(summary = "Public health check - no authentication required")
    public ResponseEntity<Object> publicHealthCheck() {
        try {
            return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Backend is running",
                "timestamp", System.currentTimeMillis(),
                "version", "1.0.0",
                "cors", "enabled"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @GetMapping("/teste")
    @Operation(summary = "Dashboard de teste para usuário padrão - sem autenticação")
    public ResponseEntity<Object> obterDashboardTeste() {
        try {
            log.info("🔍 Obtendo dashboard de teste para usuário padrão");
            
            // Primeiro, testar apenas retorno simples
            log.info("📊 Retornando dashboard de teste simples");
            
            var dashboardTeste = Map.of(
                "resumoCarteira", Map.of(
                    "valorTotal", 15000.0,
                    "valorInvestido", 12000.0,
                    "lucroPreju", 3000.0,
                    "percentualLucroPreju", 25.0,
                    "totalAtivos", 3
                ),
                "performance", Map.of(
                    "rentabilidadeAno", 18.5,
                    "rentabilidadeMes", 1.2,
                    "volatilidade", 16.8
                ),
                "alertas", Map.of(
                    "total", 2,
                    "lista", java.util.List.of("Dashboard funcionando!", "Sistema operacional")
                ),
                "status", "teste_ok",
                "timestamp", System.currentTimeMillis()
            );
            
            log.info("✅ Dashboard de teste criado com sucesso");
            return ResponseEntity.ok(dashboardTeste);
            
        } catch (Exception e) {
            log.error("❌ Erro ao obter dashboard de teste: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erro interno: " + e.getMessage(),
                "timestamp", System.currentTimeMillis(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }
}