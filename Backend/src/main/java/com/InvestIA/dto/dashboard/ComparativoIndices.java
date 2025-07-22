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
public class ComparativoIndices {
    private BigDecimal carteira;
    private BigDecimal ibovespa;
    private BigDecimal ifix;
    private BigDecimal cdi;
    private BigDecimal ipca;
}