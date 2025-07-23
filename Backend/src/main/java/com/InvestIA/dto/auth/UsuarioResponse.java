package com.InvestIA.dto.auth;

import com.InvestIA.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {
    private UUID id;
    private String nome;
    private String email;
    private String telefone;
    private PerfilResponse perfil;
    private LocalDateTime criadoEm;
    
    public static UsuarioResponse fromEntity(Usuario usuario) {
        PerfilResponse perfilResponse = null;
        if (usuario.getPerfil() != null) {
            perfilResponse = PerfilResponse.fromEntity(usuario.getPerfil());
        }
        
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .telefone(usuario.getTelefone())
                .perfil(perfilResponse)
                .criadoEm(usuario.getCriadoEm())
                .build();
    }
}