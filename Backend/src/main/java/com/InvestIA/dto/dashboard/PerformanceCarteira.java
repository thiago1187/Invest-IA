package com.InvestIA.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceCarteira {
    private List<PontoHistorico> evolucaoPatrimonio;
    private BigDecimal rentabilidadeAno;
    private BigDecimal rentabilidadeMes;
    private BigDecimal volatilidade;
    private BigDecimal sharpeRatio;
}