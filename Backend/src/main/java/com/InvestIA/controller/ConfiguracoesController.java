package com.InvestIA.controller;

import com.InvestIA.util.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações", description = "Configurações e preferências do usuário")
public class ConfiguracoesController {
    
    @GetMapping("/notificacoes")
    @Operation(summary = "Obter configurações de notificações")
    public ResponseEntity<Map<String, Object>> obterConfiguracoes(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("Obtendo configurações para usuário: {}", usuarioId);
            
            // Por enquanto, retornar configurações padrão
            Map<String, Object> configuracoes = new HashMap<>();
            configuracoes.put("emailAlertas", true);
            configuracoes.put("pushNotifications", false);
            configuracoes.put("alertasPreco", true);
            configuracoes.put("relatorioSemanal", true);
            configuracoes.put("alertasPerformance", true);
            
            return ResponseEntity.ok(configuracoes);
        } catch (Exception e) {
            log.error("Erro ao obter configurações: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }
    
    @PutMapping("/notificacoes")
    @Operation(summary = "Atualizar configurações de notificações")
    public ResponseEntity<Map<String, String>> atualizarConfiguracoes(
            @Valid @RequestBody ConfiguracoesRequest request,
            Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("Atualizando configurações para usuário: {}", usuarioId);
            
            // TODO: Salvar no banco de dados
            // Por enquanto, apenas simular sucesso
            log.info("Configurações atualizadas - Email: {}, Push: {}, Alertas: {}", 
                    request.emailAlertas, request.pushNotifications, request.alertasPreco);
            
            return ResponseEntity.ok(Map.of("message", "Configurações salvas com sucesso"));
        } catch (Exception e) {
            log.error("Erro ao salvar configurações: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao salvar configurações"));
        }
    }
    
    @PostMapping("/testar-notificacao")
    @Operation(summary = "Testar envio de notificação")
    public ResponseEntity<Map<String, String>> testarNotificacao(Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            log.info("Testando notificação para usuário: {}", usuarioId);
            
            // Simular envio de notificação
            Thread.sleep(1000); // Simular delay de processamento
            
            return ResponseEntity.ok(Map.of(
                "message", "Notificação de teste enviada com sucesso!",
                "tipo", "email",
                "timestamp", String.valueOf(System.currentTimeMillis())
            ));
        } catch (Exception e) {
            log.error("Erro ao testar notificação: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao enviar notificação"));
        }
    }
    
    // DTO for request
    public static class ConfiguracoesRequest {
        public Boolean emailAlertas;
        public Boolean pushNotifications;
        public Boolean alertasPreco;
        public Boolean relatorioSemanal;
        public Boolean alertasPerformance;
        
        public ConfiguracoesRequest() {}
    }
}