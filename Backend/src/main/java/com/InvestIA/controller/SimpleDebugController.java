package com.InvestIA.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

@RestController
@RequestMapping("/debug")
public class SimpleDebugController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/all-data")
    public ResponseEntity<String> getAllData(Authentication auth) {
        StringBuilder result = new StringBuilder();
        
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            
            result.append("=== USUÁRIO AUTENTICADO ===\n");
            result.append("Auth: ").append(auth != null ? auth.getName() : "NENHUM").append("\n\n");
            
            result.append("=== TODOS OS USUÁRIOS ===\n");
            ResultSet users = stmt.executeQuery("SELECT id, email, nome FROM usuarios ORDER BY criado_em DESC");
            while (users.next()) {
                result.append("ID: ").append(users.getString("id"))
                      .append(" | Email: ").append(users.getString("email"))
                      .append(" | Nome: ").append(users.getString("nome")).append("\n");
            }
            
            result.append("\n=== TODOS OS INVESTIMENTOS ===\n");
            ResultSet invs = stmt.executeQuery(
                "SELECT i.id, i.usuario_id, i.quantidade, i.valor_total_investido, i.ativo_status, " +
                "a.ticker, u.email " +
                "FROM investimentos i " +
                "LEFT JOIN ativos a ON i.ativo_id = a.id " +
                "LEFT JOIN usuarios u ON i.usuario_id = u.id " +
                "ORDER BY i.criado_em DESC"
            );
            while (invs.next()) {
                result.append("Investimento: ").append(invs.getString("id"))
                      .append(" | Usuário: ").append(invs.getString("email"))
                      .append(" | Ticker: ").append(invs.getString("ticker"))
                      .append(" | Qtd: ").append(invs.getInt("quantidade"))
                      .append(" | Valor: ").append(invs.getDouble("valor_total_investido"))
                      .append(" | Status: ").append(invs.getBoolean("ativo_status")).append("\n");
            }
            
            result.append("\n=== ATIVOS ===\n");
            ResultSet ativos = stmt.executeQuery("SELECT id, ticker, nome FROM ativos");
            while (ativos.next()) {
                result.append("Ativo: ").append(ativos.getString("ticker"))
                      .append(" | Nome: ").append(ativos.getString("nome")).append("\n");
            }
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=utf-8")
                .body(result.toString());
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }
}