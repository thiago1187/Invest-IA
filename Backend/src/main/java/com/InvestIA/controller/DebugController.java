package com.InvestIA.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/check-investments/{email}")
    public ResponseEntity<Object> checkInvestments(@PathVariable String email) {
        try (Connection conn = dataSource.getConnection()) {
            
            // Buscar usuário
            PreparedStatement userStmt = conn.prepareStatement("SELECT id, nome FROM usuarios WHERE email = ?");
            userStmt.setString(1, email);
            ResultSet userRs = userStmt.executeQuery();
            
            Map<String, Object> result = new HashMap<>();
            
            if (userRs.next()) {
                String userId = userRs.getString("id");
                String nome = userRs.getString("nome");
                
                result.put("usuario", Map.of("id", userId, "nome", nome, "email", email));
                
                // Buscar investimentos
                PreparedStatement invStmt = conn.prepareStatement(
                    "SELECT i.id, i.quantidade, i.valor_medio_compra, i.valor_total_investido, i.ativo_status, " +
                    "a.ticker, a.nome as ativo_nome " +
                    "FROM investimentos i " +
                    "JOIN ativos a ON i.ativo_id = a.id " +
                    "WHERE i.usuario_id = ?"
                );
                invStmt.setString(1, userId);
                ResultSet invRs = invStmt.executeQuery();
                
                List<Map<String, Object>> investments = new ArrayList<>();
                while (invRs.next()) {
                    investments.add(Map.of(
                        "id", invRs.getString("id"),
                        "ticker", invRs.getString("ticker"),
                        "nome", invRs.getString("ativo_nome"),
                        "quantidade", invRs.getInt("quantidade"),
                        "valorMedioCompra", invRs.getDouble("valor_medio_compra"),
                        "valorTotalInvestido", invRs.getDouble("valor_total_investido"),
                        "ativoStatus", invRs.getBoolean("ativo_status")
                    ));
                }
                
                result.put("investments", investments);
                result.put("investmentCount", investments.size());
                
                // Verificar query que o dashboard usa
                PreparedStatement dashStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM investimentos i " +
                    "LEFT JOIN usuarios u ON u.id = i.usuario_id " +
                    "WHERE u.id = ? AND i.ativo_status = true"
                );
                dashStmt.setString(1, userId);
                ResultSet dashRs = dashStmt.executeQuery();
                
                if (dashRs.next()) {
                    result.put("dashboardQueryCount", dashRs.getInt("count"));
                }
                
            } else {
                result.put("error", "Usuário não encontrado");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}