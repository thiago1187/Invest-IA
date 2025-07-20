package com.InvestIA.controller;

import com.InvestIA.dto.dashboard.*;
import com.InvestIA.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/usuarios/{usuarioId}/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Dashboard", description = "Dashboard do usuário")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping
    @Operation(summary = "Obter dados do dashboard")
    public ResponseEntity<DashboardResponse> obterDashboard(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(dashboardService.obterDashboard(usuarioId));
    }
    
    @GetMapping("/recomendacoes")
    @Operation(summary = "Obter recomendações personalizadas")
    public ResponseEntity<RecomendacoesResponse> obterRecomendacoes(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(dashboardService.obterRecomendacoes(usuarioId));
    }
    
    @GetMapping("/alertas")
    @Operation(summary = "Obter alertas inteligentes")
    public ResponseEntity<AlertasResponse> obterAlertas(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(dashboardService.obterAlertas(usuarioId));
    }
}