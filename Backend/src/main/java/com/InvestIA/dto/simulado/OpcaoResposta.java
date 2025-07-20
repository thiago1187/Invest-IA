package com.InvestIA.dto.simulado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpcaoResposta {
    private String id;
    private String texto;
    private Integer pontos;
}