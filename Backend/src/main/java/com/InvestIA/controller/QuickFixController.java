package com.InvestIA.controller;

import com.InvestIA.entity.Investimento;
import com.InvestIA.repository.InvestimentoRepository;
import com.InvestIA.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/quick-fix")
public class QuickFixController {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private InvestimentoRepository investimentoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/check-data")
    public ResponseEntity<Object> checkData() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Verificar usuário
            var userRs = stmt.executeQuery("SELECT id, nome, email FROM usuarios WHERE email = 'teste@teste.com'");
            if (!userRs.next()) {
                return ResponseEntity.ok(Map.of("error", "Usuário não encontrado"));
            }
            String userId = userRs.getString("id");
            
            // Verificar investimentos
            var invRs = stmt.executeQuery("SELECT COUNT(*) as count FROM investimentos WHERE usuario_id = '" + userId + "'");
            invRs.next();
            int invCount = invRs.getInt("count");
            
            // Testar query específica do dashboard
            var dashRs = stmt.executeQuery("SELECT COUNT(*) as count FROM investimentos i LEFT JOIN usuarios u ON u.id = i.usuario_id WHERE u.id = '" + userId + "' AND i.ativo_status = true");
            dashRs.next();
            int dashCount = dashRs.getInt("count");
            
            // Verificar ativos
            var ativoRs = stmt.executeQuery("SELECT COUNT(*) as count FROM ativos");
            ativoRs.next();
            int ativoCount = ativoRs.getInt("count");
            
            return ResponseEntity.ok(Map.of(
                "usuario", Map.of("id", userId, "email", userRs.getString("email")),
                "investimentosCount", invCount,
                "dashboardQueryCount", dashCount,
                "ativosCount", ativoCount
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add-sample-investments")
    public ResponseEntity<Object> addSampleInvestments() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Inserir ativos se não existem
            stmt.execute("""
                INSERT INTO ativos (id, ticker, simbolo, nome, tipo_ativo, setor, preco_atual, status, criado_em) 
                SELECT * FROM (
                    SELECT '11111111-1111-1111-1111-111111111111' as id, 'PETR4' as ticker, 'PETR4.SA' as simbolo, 'Petrobras PN' as nome, 'ACAO' as tipo_ativo, 'ENERGIA' as setor, 32.45 as preco_atual, true as status, CURRENT_TIMESTAMP as criado_em
                    UNION ALL
                    SELECT '22222222-2222-2222-2222-222222222222', 'VALE3', 'VALE3.SA', 'Vale ON', 'ACAO', 'MATERIAIS_BASICOS', 58.12, true, CURRENT_TIMESTAMP
                    UNION ALL
                    SELECT '33333333-3333-3333-3333-333333333333', 'ITUB4', 'ITUB4.SA', 'Itaú Unibanco PN', 'ACAO', 'FINANCEIRO', 30.25, true, CURRENT_TIMESTAMP
                ) WHERE NOT EXISTS (SELECT 1 FROM ativos WHERE ticker IN ('PETR4', 'VALE3', 'ITUB4'))
                """);
            
            // Inserir investimentos para o usuário teste@teste.com
            stmt.execute("""
                INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) 
                SELECT * FROM (
                    SELECT 'cccccccc-cccc-cccc-cccc-cccccccccccc' as id, 
                           (SELECT id FROM usuarios WHERE email = 'teste@teste.com') as usuario_id,
                           '11111111-1111-1111-1111-111111111111' as ativo_id,
                           100 as quantidade, 32.50 as valor_medio_compra, 32.45 as valor_atual, 3250.00 as valor_total_investido,
                           '2024-01-15' as data_compra, true as ativo_status, CURRENT_TIMESTAMP as criado_em, CURRENT_TIMESTAMP as atualizado_em
                    UNION ALL
                    SELECT 'dddddddd-dddd-dddd-dddd-dddddddddddd',
                           (SELECT id FROM usuarios WHERE email = 'teste@teste.com'),
                           '22222222-2222-2222-2222-222222222222',
                           50, 55.00, 58.12, 2750.00, '2024-02-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    UNION ALL
                    SELECT 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
                           (SELECT id FROM usuarios WHERE email = 'teste@teste.com'),
                           '33333333-3333-3333-3333-333333333333',
                           150, 28.00, 30.25, 4200.00, '2024-03-05', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                ) WHERE NOT EXISTS (SELECT 1 FROM investimentos WHERE usuario_id = (SELECT id FROM usuarios WHERE email = 'teste@teste.com'))
                """);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Investimentos adicionados com sucesso!"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/test-repository/{email}")
    public ResponseEntity<Object> testRepository(@PathVariable String email) {
        try {
            // Buscar usuário usando repository
            var usuario = usuarioRepository.findByEmail(email);
            if (usuario.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "Usuário não encontrado"));
            }
            
            UUID userId = usuario.get().getId();
            
            // Testar ambas as queries do repository
            List<Investimento> investimentosComStatus = investimentoRepository.findByUsuarioIdAndAtivoStatusTrue(userId);
            List<Investimento> todosInvestimentos = investimentoRepository.findByUsuarioId(userId);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email", email,
                "investimentosComStatus", investimentosComStatus.size(),
                "todosInvestimentos", todosInvestimentos.size(),
                "investimentosDetalhes", todosInvestimentos.stream().map(inv -> Map.of(
                    "id", inv.getId().toString(),
                    "ativoTicker", inv.getAtivo() != null ? inv.getAtivo().getTicker() : "null",
                    "quantidade", inv.getQuantidade(),
                    "ativoStatus", inv.isAtivoStatus()
                )).toList()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "stackTrace", e.getStackTrace()[0].toString()
            ));
        }
    }
}