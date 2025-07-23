package com.InvestIA.dto.auth;

import com.InvestIA.entity.Perfil;
import com.InvestIA.enums.NivelExperiencia;
import com.InvestIA.enums.TipoPerfil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerfilResponse {
    private TipoPerfil tipoPerfil;
    private NivelExperiencia nivelExperiencia;
    private Double toleranciaRisco;
    private Integer pontuacaoSimulado;
    
    public static PerfilResponse fromEntity(Perfil perfil) {
        return PerfilResponse.builder()
                .tipoPerfil(perfil.getTipoPerfil())
                .nivelExperiencia(perfil.getNivelExperiencia())
                .toleranciaRisco(perfil.getToleranciaRisco())
                .pontuacaoSimulado(perfil.getPontuacaoSimulado())
                .build();
    }
}