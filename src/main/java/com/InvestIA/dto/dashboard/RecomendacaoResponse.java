package com.InvestIA.dto.dashboard;

import com.InvestIA.dto.investimento.AtivoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecomendacaoResponse {
    private String id;
    private AtivoResponse ativo;
    private String tipoRecomendacao; // COMPRA, VENDA, MANTER
    private String motivo;
    private BigDecimal precoAlvo;
    private Integer confianca; // 0-100
    private LocalDateTime dataRecomendacao;
}