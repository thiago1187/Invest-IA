package com.InvestIA.dto.auth;

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
}