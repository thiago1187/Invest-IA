package com.InvestIA.controller;

import com.InvestIA.dto.auth.*;
import com.InvestIA.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "success", false,
                    "message", "Erro no cadastro: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @PostMapping("/cadastro")
    @Operation(summary = "Cadastrar novo usuário (endpoint brasileiro)")
    public ResponseEntity<?> cadastro(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "success", false,
                    "message", "Erro no cadastro: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login de usuário")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.authenticate(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "success", false,
                    "message", "Erro no login: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @PostMapping("/refresh-token")
    @Operation(summary = "Renovar token de acesso")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}