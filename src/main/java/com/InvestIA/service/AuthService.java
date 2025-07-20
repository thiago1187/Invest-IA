package com.InvestIA.service;

import com.InvestIA.config.JwtService;
import com.InvestIA.dto.auth.*;
import com.InvestIA.entity.Perfil;
import com.InvestIA.entity.Usuario;
import com.InvestIA.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        var usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .telefone(request.getTelefone())
                .build();
        
        usuarioRepository.save(usuario);
        
        var accessToken = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L) // 1 hora
                .usuario(mapToUsuarioResponse(usuario))
                .build();
    }
    
    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getSenha()
                )
        );
        
        var usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow();
        
        var accessToken = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L)
                .usuario(mapToUsuarioResponse(usuario))
                .build();
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String userEmail = jwtService.extractUsername(request.getRefreshToken());
        
        if (userEmail != null) {
            var usuario = usuarioRepository.findByEmail(userEmail)
                    .orElseThrow();
            
            if (jwtService.isTokenValid(request.getRefreshToken(), usuario)) {
                var accessToken = jwtService.generateToken(usuario);
                
                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(request.getRefreshToken())
                        .expiresIn(3600L)
                        .usuario(mapToUsuarioResponse(usuario))
                        .build();
            }
        }
        
        throw new RuntimeException("Token inválido");
    }
    
    private UsuarioResponse mapToUsuarioResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .telefone(usuario.getTelefone())
                .perfil(usuario.getPerfil() != null ? mapToPerfilResponse(usuario.getPerfil()) : null)
                .criadoEm(usuario.getCriadoEm())
                .build();
    }
    
    private PerfilResponse mapToPerfilResponse(Perfil perfil) {
        return PerfilResponse.builder()
                .tipoPerfil(perfil.getTipoPerfil())
                .nivelExperiencia(perfil.getNivelExperiencia())
                .toleranciaRisco(perfil.getToleranciaRisco())
                .pontuacaoSimulado(perfil.getPontuacaoSimulado())
                .build();
    }
}