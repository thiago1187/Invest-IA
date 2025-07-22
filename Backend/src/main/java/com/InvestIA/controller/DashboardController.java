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
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Dashboard", description = "Dashboard do usuário")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/{usuarioId}")
    @Operation(summary = "Obter dados completos do dashboard")
    public ResponseEntity<DashboardResponse> obterDashboard(@PathVariable UUID usuarioId) {
        try {
            DashboardResponse dashboard = dashboardService.obterDashboard(usuarioId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/{usuarioId}/recomendacoes")
    @Operation(summary = "Obter recomendações personalizadas com IA")
    public ResponseEntity<RecomendacoesResponse> obterRecomendacoes(@PathVariable UUID usuarioId) {
        try {
            RecomendacoesResponse recomendacoes = dashboardService.obterRecomendacoes(usuarioId);
            return ResponseEntity.ok(recomendacoes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/{usuarioId}/alertas")
    @Operation(summary = "Obter alertas inteligentes")
    public ResponseEntity<AlertasResponse> obterAlertas(@PathVariable UUID usuarioId) {
        try {
            AlertasResponse alertas = dashboardService.obterAlertas(usuarioId);
            return ResponseEntity.ok(alertas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/{usuarioId}/performance")
    @Operation(summary = "Obter análise detalhada de performance")
    public ResponseEntity<PerformanceResponse> obterPerformance(@PathVariable UUID usuarioId) {
        try {
            PerformanceResponse performance = dashboardService.obterPerformanceDetalhada(usuarioId);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @PostMapping("/{usuarioId}/atualizar")
    @Operation(summary = "Forçar atualização dos dados em tempo real")
    public ResponseEntity<Void> atualizarDados(@PathVariable UUID usuarioId) {
        try {
            dashboardService.atualizarDadosTempoReal(usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}