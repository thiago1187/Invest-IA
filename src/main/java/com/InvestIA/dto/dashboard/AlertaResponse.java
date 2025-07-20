package com.InvestIA.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertaResponse {
    private String id;
    private String tipo; // OPORTUNIDADE, RISCO, REBALANCEAMENTO
    private String titulo;
    private String mensagem;
    private String severidade; // BAIXA, MEDIA, ALTA
    private LocalDateTime dataHora;
    private Map<String, Object> dados;
}