package com.InvestIA.controller;

import com.InvestIA.dto.simulado.ResultadoSimuladoResponse;
import com.InvestIA.dto.simulado.SimuladoQuestoesResponse;
import com.InvestIA.dto.simulado.SimuladoRespostasRequest;
import com.InvestIA.entity.Usuario;
import com.InvestIA.service.SimuladoService;
import com.InvestIA.util.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/simulado")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Simulado", description = "Questionário de perfil de investidor")
public class SimuladoController {

    private final SimuladoService simuladoService;

    @GetMapping("/questoes")
    @Operation(summary = "Obter questões do simulado de perfil")
    public ResponseEntity<SimuladoQuestoesResponse> obterQuestoes() {
        try {
            SimuladoQuestoesResponse questoes = simuladoService.obterPerguntas();
            return ResponseEntity.ok(questoes);
        } catch (Exception e) {
            log.error("Erro ao obter questões do simulado: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/processar")
    @Operation(summary = "Processar respostas do simulado e determinar perfil")
    public ResponseEntity<Object> processarRespostas(
            @RequestBody SimuladoRespostasRequest request,
            Authentication authentication) {
        try {
            Usuario usuario = AuthenticationUtils.obterUsuarioAutenticado(authentication);
            log.info("Processando respostas do simulado para usuário: {}", usuario.getEmail());
            
            ResultadoSimuladoResponse resultado = simuladoService.processarRespostas(request, usuario.getEmail());
            log.info("Perfil determinado: {} com {} pontos", resultado.getPerfil(), resultado.getPontuacaoTotal());
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro ao processar respostas do simulado: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erro interno do servidor",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}