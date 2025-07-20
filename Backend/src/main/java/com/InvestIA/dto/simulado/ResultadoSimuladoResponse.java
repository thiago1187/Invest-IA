package com.InvestIA.dto.simulado;

import com.InvestIA.enums.TipoPerfil;
import com.InvestIA.enums.NivelExperiencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoSimuladoResponse {
    private TipoPerfil perfil;
    private NivelExperiencia nivelExperiencia;
    private Integer pontuacaoTotal;
    private String descricaoPerfil;
    private List<String> caracteristicas;
    private List<String> recomendacoesIniciais;
    private Double toleranciaRisco;
}