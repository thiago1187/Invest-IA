package com.InvestIA.controller;

import com.InvestIA.dto.auth.UsuarioResponse;
import com.InvestIA.entity.Usuario;
import com.InvestIA.service.UsuarioService;
import com.InvestIA.util.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Perfil", description = "Gerenciamento do perfil do usuário")
public class PerfilController {
    
    private final UsuarioService usuarioService;
    
    @GetMapping
    @Operation(summary = "Obter perfil do usuário autenticado")
    public ResponseEntity<UsuarioResponse> obterPerfil(Authentication authentication) {
        try {
            Usuario usuario = AuthenticationUtils.obterUsuarioAutenticado(authentication);
            UsuarioResponse response = UsuarioResponse.fromEntity(usuario);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @PutMapping
    @Operation(summary = "Atualizar perfil do usuário")
    public ResponseEntity<UsuarioResponse> atualizarPerfil(
            @Valid @RequestBody AtualizarPerfilRequest request,
            Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            Usuario usuarioAtualizado = usuarioService.atualizarPerfil(usuarioId, request);
            UsuarioResponse response = UsuarioResponse.fromEntity(usuarioAtualizado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @PostMapping("/avaliacao")
    @Operation(summary = "Salvar resultado da avaliação de perfil")
    public ResponseEntity<Map<String, String>> salvarAvaliacao(
            @Valid @RequestBody SalvarAvaliacaoRequest request,
            Authentication authentication) {
        try {
            UUID usuarioId = AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
            usuarioService.salvarAvaliacaoPerfil(usuarioId, request);
            return ResponseEntity.ok(Map.of("message", "Avaliação salva com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao salvar avaliação"));
        }
    }
    
    // DTOs
    public static class AtualizarPerfilRequest {
        public String nome;
        public String telefone;
        public String email; // Note: changing email might require re-auth
    }
    
    public static class SalvarAvaliacaoRequest {
        public String tipoPerfil; // CONSERVADOR, MODERADO, AGRESSIVO
        public String nivelExperiencia; // INICIANTE, INTERMEDIARIO, AVANCADO, EXPERT
        public Integer toleranciaRisco; // 0-10
        public Map<String, Object> respostasCompletas; // JSON das respostas
    }
}