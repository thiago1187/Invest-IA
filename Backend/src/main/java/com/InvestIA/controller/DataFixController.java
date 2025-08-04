package com.InvestIA.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test/data-fix")
public class DataFixController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/add-investments/{userEmail}")
    public ResponseEntity<Object> addInvestments(@PathVariable String userEmail) {
        try {
            // Buscar ID do usuário
            String userId = jdbcTemplate.queryForObject(
                "SELECT id FROM usuarios WHERE email = ?", 
                String.class, userEmail);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuário não encontrado"));
            }
            
            // Criar ativos se não existirem
            createAssetIfNotExists("PETR4", "PETR4.SA", "Petrobras PN", "ACAO", "ENERGIA", 32.45);
            createAssetIfNotExists("VALE3", "VALE3.SA", "Vale ON", "ACAO", "MATERIAIS_BASICOS", 58.12);
            createAssetIfNotExists("ITUB4", "ITUB4.SA", "Itaú Unibanco PN", "ACAO", "FINANCEIRO", 30.25);
            
            // Buscar IDs dos ativos
            String petr4Id = jdbcTemplate.queryForObject("SELECT id FROM ativos WHERE ticker = 'PETR4'", String.class);
            String vale3Id = jdbcTemplate.queryForObject("SELECT id FROM ativos WHERE ticker = 'VALE3'", String.class);
            String itub4Id = jdbcTemplate.queryForObject("SELECT id FROM ativos WHERE ticker = 'ITUB4'", String.class);
            
            // Remover investimentos existentes
            jdbcTemplate.update("DELETE FROM investimentos WHERE usuario_id = ?", userId);
            
            // Criar investimentos
            String inv1Id = UUID.randomUUID().toString();
            String inv2Id = UUID.randomUUID().toString();
            String inv3Id = UUID.randomUUID().toString();
            
            jdbcTemplate.update(
                "INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                inv1Id, userId, petr4Id, 100, 32.50, 32.45, 3250.00, "2024-01-15", true
            );
            
            jdbcTemplate.update(
                "INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                inv2Id, userId, vale3Id, 50, 55.00, 58.12, 2750.00, "2024-02-10", true
            );
            
            jdbcTemplate.update(
                "INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                inv3Id, userId, itub4Id, 150, 28.00, 30.25, 4200.00, "2024-03-05", true
            );
            
            // Verificar se foram criados
            int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM investimentos WHERE usuario_id = ?", 
                Integer.class, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Investimentos criados com sucesso!",
                "userId", userId,
                "investmentCount", count,
                "investments", Map.of(
                    "PETR4", "100 ações - R$ 3.250,00",
                    "VALE3", "50 ações - R$ 2.750,00", 
                    "ITUB4", "150 ações - R$ 4.200,00"
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    private void createAssetIfNotExists(String ticker, String simbolo, String nome, String tipo, String setor, double preco) {
        try {
            // Verificar se existe
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ativos WHERE ticker = ?", 
                Integer.class, ticker);
            
            if (count == 0) {
                String id = UUID.randomUUID().toString();
                jdbcTemplate.update(
                    "INSERT INTO ativos (id, ticker, simbolo, nome, tipo_ativo, setor, preco_atual, status, criado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                    id, ticker, simbolo, nome, tipo, setor, preco, true
                );
            }
        } catch (Exception e) {
            // Ignorar erros de criação de ativo
        }
    }
}