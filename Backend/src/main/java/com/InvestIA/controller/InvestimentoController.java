package com.InvestIA.controller;

import com.InvestIA.dto.investimento.*;
import com.InvestIA.service.InvestimentoService;
import com.InvestIA.service.FinanceAPIService;
import com.InvestIA.util.AuthenticationUtils;
import com.InvestIA.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    
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
    public ResponseEntity<?> adicionarInvestimento(
            @Valid @RequestBody CriarInvestimentoRequest request,
            Authentication authentication) {
        try {
            UUID usuarioId = obterUsuarioId(authentication);
            InvestimentoResponse response = investimentoService.criar(usuarioId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao criar investimento. " + e.getMessage(), 
                               "detalhes", e.getClass().getSimpleName(),
                               "authentication", authentication != null ? authentication.getName() : "null",
                               "principal", authentication != null ? authentication.getPrincipal().getClass().getSimpleName() : "null"));
        }
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
    
    // Endpoint de teste para criar investimentos sem autenticação
    @PostMapping("/teste")
    @Operation(summary = "Criar investimento de teste (usuário padrão)")
    public ResponseEntity<?> criarInvestimentoTeste(
            @Valid @RequestBody CriarInvestimentoRequest request) {
        try {
            // Buscar usuário de teste, primeiro tentar teste@investia.com, depois teste@teste.com, depois novo@teste.com
            Optional<UUID> usuarioTeste = usuarioRepository.findByEmail("teste@investia.com")
                    .map(u -> u.getId());
            
            if (usuarioTeste.isEmpty()) {
                usuarioTeste = usuarioRepository.findByEmail("novo@teste.com")
                        .map(u -> u.getId());
            }
            
            if (usuarioTeste.isEmpty()) {
                usuarioTeste = usuarioRepository.findByEmail("teste@teste.com")
                        .map(u -> u.getId());
            }
            
            if (usuarioTeste.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("erro", "Nenhum usuário de teste encontrado", 
                                   "detalhes", "Verifique se data.sql foi executado"));
            }
            
            InvestimentoResponse response = investimentoService.criar(usuarioTeste.get(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", e.getMessage(), 
                               "detalhes", e.getClass().getSimpleName(),
                               "stackTrace", e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : ""));
        }
    }
    
    // Endpoint de teste para simular criação com autenticação
    @PostMapping("/teste-auth")
    @Operation(summary = "Simular criação de investimento com usuário específico")
    public ResponseEntity<?> criarInvestimentoTesteAuth(
            @Valid @RequestBody CriarInvestimentoRequest request,
            @RequestParam(defaultValue = "novo@teste.com") String email) {
        try {
            // Buscar usuário específico
            Optional<UUID> usuarioId = usuarioRepository.findByEmail(email)
                    .map(u -> u.getId());
            
            if (usuarioId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Usuário não encontrado: " + email, 
                                   "detalhes", "Email fornecido não existe no banco"));
            }
            
            InvestimentoResponse response = investimentoService.criar(usuarioId.get(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", e.getMessage(), 
                               "detalhes", e.getClass().getSimpleName(),
                               "stackTrace", e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : ""));
        }
    }
    
    // Endpoint de teste para listar investimentos do usuário de teste
    @GetMapping("/teste")
    @Operation(summary = "Listar investimentos do usuário de teste")
    public ResponseEntity<Page<InvestimentoResponse>> listarInvestimentosTeste(Pageable pageable) {
        try {
            // Buscar usuário de teste
            Optional<UUID> usuarioTeste = usuarioRepository.findByEmail("teste@investia.com")
                    .map(u -> u.getId());
            
            if (usuarioTeste.isEmpty()) {
                usuarioTeste = usuarioRepository.findByEmail("teste@teste.com")
                        .map(u -> u.getId());
            }
            
            if (usuarioTeste.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            return ResponseEntity.ok(investimentoService.listarPorUsuario(usuarioTeste.get(), pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private UUID obterUsuarioId(Authentication authentication) {
        return AuthenticationUtils.obterUsuarioIdAutenticado(authentication);
    }
}