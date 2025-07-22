package com.InvestIA.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasRisco {
    private BigDecimal sharpeRatio;
    private BigDecimal varDiario;
    private BigDecimal beta;
    private BigDecimal volatilidade30d;
    private BigDecimal correlacaoIbov;
}