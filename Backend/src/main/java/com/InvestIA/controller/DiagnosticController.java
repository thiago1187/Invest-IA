package com.InvestIA.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/diagnostic")
public class DiagnosticController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/full-check")
    public ResponseEntity<Object> fullCheck(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            
            // 1. Verificar usuário autenticado
            String emailUsuario = authentication != null ? authentication.getName() : "NO_AUTH";
            result.put("authUser", emailUsuario);
            
            // 2. Listar TODOS os usuários
            PreparedStatement allUsersStmt = conn.prepareStatement("SELECT id, email, nome FROM usuarios ORDER BY criado_em DESC");
            ResultSet allUsersRs = allUsersStmt.executeQuery();
            
            List<Map<String, Object>> allUsers = new ArrayList<>();
            while (allUsersRs.next()) {
                allUsers.add(Map.of(
                    "id", allUsersRs.getString("id"),
                    "email", allUsersRs.getString("email"),
                    "nome", allUsersRs.getString("nome")
                ));
            }
            result.put("allUsers", allUsers);
            
            // 3. Listar TODOS os investimentos
            PreparedStatement allInvStmt = conn.prepareStatement(
                "SELECT i.id, i.usuario_id, i.quantidade, i.valor_total_investido, i.ativo_status, " +
                "a.ticker, a.nome as ativo_nome, u.email " +
                "FROM investimentos i " +
                "LEFT JOIN ativos a ON i.ativo_id = a.id " +
                "LEFT JOIN usuarios u ON i.usuario_id = u.id " +
                "ORDER BY i.criado_em DESC"
            );
            ResultSet allInvRs = allInvStmt.executeQuery();
            
            List<Map<String, Object>> allInvestments = new ArrayList<>();
            while (allInvRs.next()) {
                allInvestments.add(Map.of(
                    "id", allInvRs.getString("id"),
                    "usuario_id", allInvRs.getString("usuario_id"),
                    "usuario_email", allInvRs.getString("email"),
                    "ticker", allInvRs.getString("ticker"),
                    "ativo_nome", allInvRs.getString("ativo_nome"),
                    "quantidade", allInvRs.getInt("quantidade"),
                    "valor_total", allInvRs.getDouble("valor_total_investido"),
                    "ativo_status", allInvRs.getBoolean("ativo_status")
                ));
            }
            result.put("allInvestments", allInvestments);
            
            // 4. Se tem usuário autenticado, verificar seus investimentos especificamente
            if (authentication != null && authentication.getName() != null) {
                PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM usuarios WHERE email = ?");
                userStmt.setString(1, authentication.getName());
                ResultSet userRs = userStmt.executeQuery();
                
                if (userRs.next()) {
                    String userId = userRs.getString("id");
                    
                    // Query exata do dashboard
                    PreparedStatement dashStmt = conn.prepareStatement(
                        "SELECT i.quantidade, i.valor_total_investido, i.valor_medio_compra, a.ticker, a.nome " +
                        "FROM investimentos i " +
                        "JOIN ativos a ON i.ativo_id = a.id " +
                        "WHERE i.usuario_id = ? AND i.ativo_status = true"
                    );
                    dashStmt.setString(1, userId);
                    ResultSet dashRs = dashStmt.executeQuery();
                    
                    List<Map<String, Object>> userInvestments = new ArrayList<>();
                    while (dashRs.next()) {
                        userInvestments.add(Map.of(
                            "ticker", dashRs.getString("ticker"),
                            "nome", dashRs.getString("nome"),
                            "quantidade", dashRs.getInt("quantidade"),
                            "valor_total", dashRs.getDouble("valor_total_investido"),
                            "valor_medio", dashRs.getDouble("valor_medio_compra")
                        ));
                    }
                    
                    result.put("currentUserInvestments", userInvestments);
                    result.put("currentUserId", userId);
                }
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "stackTrace", e.getStackTrace()[0].toString()
            ));
        }
    }
}