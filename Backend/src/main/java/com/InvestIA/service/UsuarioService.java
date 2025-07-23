package com.InvestIA.service;

import com.InvestIA.controller.PerfilController;
import com.InvestIA.entity.Perfil;
import com.InvestIA.entity.Usuario;
import com.InvestIA.enums.NivelExperiencia;
import com.InvestIA.enums.TipoPerfil;
import com.InvestIA.repository.PerfilRepository;
import com.InvestIA.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    
    @Transactional
    public Usuario atualizarPerfil(UUID usuarioId, PerfilController.AtualizarPerfilRequest request) {
        log.info("Atualizando perfil do usuário: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Atualizar dados básicos
        if (request.nome != null && !request.nome.trim().isEmpty()) {
            usuario.setNome(request.nome.trim());
        }
        
        if (request.telefone != null) {
            usuario.setTelefone(request.telefone.trim());
        }
        
        // Note: Email changes are sensitive and might require re-authentication
        // For now, we'll skip email updates for security
        
        usuario.setAtualizadoEm(LocalDateTime.now());
        
        return usuarioRepository.save(usuario);
    }
    
    @Transactional
    public void salvarAvaliacaoPerfil(UUID usuarioId, PerfilController.SalvarAvaliacaoRequest request) {
        log.info("Salvando avaliação de perfil para usuário: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Buscar ou criar perfil
        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElse(Perfil.builder()
                        .usuario(usuario)
                        .criadoEm(LocalDateTime.now())
                        .build());
        
        // Atualizar dados do perfil
        perfil.setTipoPerfil(TipoPerfil.valueOf(request.tipoPerfil.toUpperCase()));
        perfil.setNivelExperiencia(NivelExperiencia.valueOf(request.nivelExperiencia.toUpperCase()));
        perfil.setToleranciaRisco(request.toleranciaRisco.doubleValue());
        
        // Salvar respostas completas como JSON
        if (request.respostasCompletas != null) {
            try {
                // Convert map to JSON string (simple approach)
                String respostasJson = request.respostasCompletas.toString();
                perfil.setRespostasSimulado(respostasJson);
            } catch (Exception e) {
                log.warn("Erro ao serializar respostas do simulado: {}", e.getMessage());
            }
        }
        
        perfil.setAtualizadoEm(LocalDateTime.now());
        
        perfilRepository.save(perfil);
        
        log.info("Perfil salvo com sucesso - Tipo: {}, Experiência: {}, Tolerância: {}", 
                request.tipoPerfil, request.nivelExperiencia, request.toleranciaRisco);
    }
    
    public Usuario buscarPorId(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
    
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}