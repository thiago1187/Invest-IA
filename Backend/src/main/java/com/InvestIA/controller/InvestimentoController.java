package com.InvestIA.controller;

import com.InvestIA.dto.investimento.*;
import com.InvestIA.service.InvestimentoService;
import com.InvestIA.service.FinanceAPIService;
import com.InvestIA.util.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/investimentos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Investimentos", description = "Gestão de investimentos do usuário")
public class InvestimentoController {
    
    private final InvestimentoService investimentoService;
    private final FinanceAPIService financeAPIService;
    
    @GetMapping
    @Operation(summary = "Listar investimentos do usuário")
    public ResponseEntity<Page<InvestimentoResponse>> listarInvestimentos(
            Pageable pageable,
            Authentication authentication) {
        UUID usuarioId = obterUsuarioId(authentication);
        return ResponseEntity.ok(investimentoService.listarPorUsuario(usuarioId, pageable));
    }
    
    @PostMapping
    @Operation(summary = "Adicionar novo investimento")
    public ResponseEntity<InvestimentoResponse> adicionarInvestimento(
            @Valid @RequestBody CriarInvestimentoRequest request,
            Authentication authentication) {
        UUID usuarioId = obterUsuarioId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investimentoService.criar(usuarioId, request));
    }
    
    @PutMapping("/{investimentoId}")
    @Operation(summary = "Atualizar investimento")
    public ResponseEntity<InvestimentoResponse> atualizarInvestimento(
            @PathVariable UUID investimentoId,
            @Valid @RequestBody AtualizarInvestimentoRequest request,
            Authentication authentication) {
        UUID usuarioId = obterUsuarioId(authentication);
        return ResponseEntity.ok(investimentoService.atualizar(usuarioId, investimentoId, request));
    }
    
    @DeleteMapping("/{investimentoId}")
    @Operation(summary = "Remover investimento")
    public ResponseEntity<Void> removerInvestimento(
            @PathVariable UUID investimentoId,
            Authentication authentication) {
        UUID usuarioId = obterUsuarioId(authentication);
        investimentoService.remover(usuarioId, investimentoId);
        return ResponseEntity.noContent().build();
    }
    
    private UUID obterUsuarioId(Authentication authentication) {
        return AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
    }
}