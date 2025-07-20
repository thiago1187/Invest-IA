package com.InvestIA.dto.simulado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerguntaSimulado {
    private Integer id;
    private String pergunta;
    private List<OpcaoResposta> opcoes;
    private String categoria; // RISCO, EXPERIENCIA, OBJETIVO, PRAZO
}