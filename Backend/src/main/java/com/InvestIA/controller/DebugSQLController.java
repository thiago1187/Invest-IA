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
@RequestMapping("/api/debug-sql")
public class DebugSQLController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/check-investments")
    public ResponseEntity<Object> checkInvestments() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            
            // 1. Verificar usuário
            PreparedStatement userStmt = conn.prepareStatement("SELECT id, email FROM usuarios WHERE email = 'teste@teste.com'");
            ResultSet userRs = userStmt.executeQuery();
            
            String userId = null;
            if (userRs.next()) {
                userId = userRs.getString("id");
                result.put("usuario", Map.of("id", userId, "email", userRs.getString("email")));
            } else {
                result.put("error", "Usuário não encontrado");
                return ResponseEntity.ok(result);
            }
            
            // 2. Verificar todos os investimentos
            PreparedStatement allInvStmt = conn.prepareStatement(
                "SELECT i.id, i.usuario_id, i.ativo_id, i.quantidade, i.valor_total_investido, i.ativo_status, a.ticker " +
                "FROM investimentos i LEFT JOIN ativos a ON i.ativo_id = a.id WHERE i.usuario_id = ?"
            );
            allInvStmt.setString(1, userId);
            ResultSet allInvRs = allInvStmt.executeQuery();
            
            List<Map<String, Object>> allInvestments = new ArrayList<>();
            while (allInvRs.next()) {
                allInvestments.add(Map.of(
                    "id", allInvRs.getString("id"),
                    "usuario_id", allInvRs.getString("usuario_id"),
                    "ativo_id", allInvRs.getString("ativo_id"),
                    "quantidade", allInvRs.getInt("quantidade"),
                    "valor_total", allInvRs.getDouble("valor_total_investido"),
                    "ativo_status", allInvRs.getBoolean("ativo_status"),
                    "ticker", allInvRs.getString("ticker")
                ));
            }
            result.put("allInvestments", allInvestments);
            
            // 3. Verificar query do dashboard
            PreparedStatement dashStmt = conn.prepareStatement(
                "SELECT i.quantidade, i.valor_total_investido, i.valor_medio_compra, a.ticker, a.nome " +
                "FROM investimentos i JOIN ativos a ON i.ativo_id = a.id " +
                "WHERE i.usuario_id = ? AND i.ativo_status = true"
            );
            dashStmt.setString(1, userId);
            ResultSet dashRs = dashStmt.executeQuery();
            
            List<Map<String, Object>> dashboardResults = new ArrayList<>();
            while (dashRs.next()) {
                dashboardResults.add(Map.of(
                    "ticker", dashRs.getString("ticker"),
                    "nome", dashRs.getString("nome"),
                    "quantidade", dashRs.getInt("quantidade"),
                    "valor_total", dashRs.getDouble("valor_total_investido"),
                    "valor_medio", dashRs.getDouble("valor_medio_compra")
                ));
            }
            result.put("dashboardQuery", dashboardResults);
            
            // 4. Verificar ativos
            PreparedStatement ativoStmt = conn.prepareStatement("SELECT COUNT(*) as count FROM ativos");
            ResultSet ativoRs = ativoStmt.executeQuery();
            ativoRs.next();
            result.put("ativosCount", ativoRs.getInt("count"));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "stackTrace", e.getStackTrace()[0].toString()
            ));
        }
    }
}