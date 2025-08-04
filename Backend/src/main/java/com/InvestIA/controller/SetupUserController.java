package com.InvestIA.controller;

import com.InvestIA.entity.*;
import com.InvestIA.enums.*;
import com.InvestIA.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupUserController {
    
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final InvestimentoRepository investimentoRepository;
    private final AtivoRepository ativoRepository;
    
    @PostMapping("/create-user-data/{email}")
    public ResponseEntity<?> createUserData(@PathVariable String email) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
            
            // Criar perfil se não existir
            if (usuario.getPerfil() == null) {
                Perfil perfil = Perfil.builder()
                        .usuario(usuario)
                        .tipoPerfil(TipoPerfil.MODERADO)
                        .nivelExperiencia(NivelExperiencia.INTERMEDIARIO)
                        .toleranciaRisco(0.6)
                        .criadoEm(LocalDateTime.now())
                        .build();
                
                perfilRepository.save(perfil);
                usuario.setPerfil(perfil);
                usuarioRepository.save(usuario);
            }
            
            // Criar investimentos se não existir
            if (investimentoRepository.findByUsuarioIdAndAtivoStatusTrue(usuario.getId()).isEmpty()) {
                // BBAS3
                Ativo bbas3 = ativoRepository.findByTicker("BBAS3")
                        .orElseThrow(() -> new RuntimeException("BBAS3 não encontrado"));
                
                Investimento investBbas3 = Investimento.builder()
                        .usuario(usuario)
                        .ativo(bbas3)
                        .quantidade(500)
                        .valorMedioCompra(BigDecimal.valueOf(19.50))
                        .valorAtual(bbas3.getPrecoAtual())
                        .valorTotalInvestido(BigDecimal.valueOf(9750.00))
                        .dataCompra(LocalDate.of(2024, 3, 1))
                        .ativoStatus(true)
                        .criadoEm(LocalDateTime.now())
                        .atualizadoEm(LocalDateTime.now())
                        .build();
                
                investimentoRepository.save(investBbas3);
                
                // PETR4
                Ativo petr4 = ativoRepository.findByTicker("PETR4")
                        .orElseThrow(() -> new RuntimeException("PETR4 não encontrado"));
                
                Investimento investPetr4 = Investimento.builder()
                        .usuario(usuario)
                        .ativo(petr4)
                        .quantidade(300)
                        .valorMedioCompra(BigDecimal.valueOf(31.00))
                        .valorAtual(petr4.getPrecoAtual())
                        .valorTotalInvestido(BigDecimal.valueOf(9300.00))
                        .dataCompra(LocalDate.of(2024, 3, 15))
                        .ativoStatus(true)
                        .criadoEm(LocalDateTime.now())
                        .atualizadoEm(LocalDateTime.now())
                        .build();
                
                investimentoRepository.save(investPetr4);
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Dados criados para usuário: " + usuario.getNome(),
                    "usuario", Map.of(
                            "id", usuario.getId(),
                            "nome", usuario.getNome(),
                            "email", usuario.getEmail(),
                            "perfil", usuario.getPerfil() != null ? usuario.getPerfil().getTipoPerfil() : "SEM PERFIL"
                    )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erro ao criar dados: " + e.getMessage()
            ));
        }
    }
}