package com.InvestIA.controller;

import com.InvestIA.dto.investimento.*;
import com.InvestIA.service.InvestimentoService;
import com.InvestIA.service.FinanceAPIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios/{usuarioId}/investimentos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Investimentos", description = "Gestão de investimentos")
public class InvestimentoController {
    
    private final InvestimentoService investimentoService;
    private final FinanceAPIService financeAPIService;
    
    @GetMapping
    @Operation(summary = "Listar investimentos do usuário")
    public ResponseEntity<Page<InvestimentoResponse>> listarInvestimentos(
            @PathVariable UUID usuarioId,
            Pageable pageable) {
        return ResponseEntity.ok(investimentoService.listarPorUsuario(usuarioId, pageable));
    }
    
    @PostMapping
    @Operation(summary = "Adicionar novo investimento")
    public ResponseEntity<InvestimentoResponse> adicionarInvestimento(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody CriarInvestimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investimentoService.criar(usuarioId, request));
    }
    
    @PutMapping("/{investimentoId}")
    @Operation(summary = "Atualizar investimento")
    public ResponseEntity<InvestimentoResponse> atualizarInvestimento(
            @PathVariable UUID usuarioId,
            @PathVariable UUID investimentoId,
            @Valid @RequestBody AtualizarInvestimentoRequest request) {
        return ResponseEntity.ok(investimentoService.atualizar(usuarioId, investimentoId, request));
    }
    
    @DeleteMapping("/{investimentoId}")
    @Operation(summary = "Remover investimento")
    public ResponseEntity<Void> removerInvestimento(
            @PathVariable UUID usuarioId,
            @PathVariable UUID investimentoId) {
        investimentoService.remover(usuarioId, investimentoId);
        return ResponseEntity.noContent().build();
    }
}