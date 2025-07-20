package com.InvestIA.dto.auth;

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
}