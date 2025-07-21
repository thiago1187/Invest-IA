package com.InvestIA.controller;

import com.InvestIA.dto.simulado.*;
import com.InvestIA.service.SimuladoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulado")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Simulado", description = "Simulado de perfil de investidor")
public class SimuladoController {
    
    private final SimuladoService simuladoService;
    
    @GetMapping("/test")
    @Operation(summary = "Teste simples")
    public ResponseEntity<String> test() {
        try {
            return ResponseEntity.ok("Simulado Controller funcionando!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }
    
    @GetMapping("/perguntas")
    @Operation(summary = "Obter perguntas do simulado")
    public ResponseEntity<SimuladoQuestoesResponse> obterPerguntas() {
        try {
            SimuladoQuestoesResponse response = simuladoService.obterPerguntas();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erro ao obter perguntas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter perguntas: " + e.getMessage());
        }
    }
    
    @PostMapping("/responder")
    @Operation(summary = "Enviar respostas do simulado")
    public ResponseEntity<ResultadoSimuladoResponse> responderSimulado(
            @Valid @RequestBody SimuladoRespostasRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(simuladoService.processarRespostas(request, authentication.getName()));
    }
}