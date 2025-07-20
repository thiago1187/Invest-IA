package com.InvestIA.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumoCarteira {
    private BigDecimal valorTotal;
    private BigDecimal valorInvestido;
    private BigDecimal lucroPreju;
    private BigDecimal percentualLucroPreju;
    private BigDecimal variacaoDiaria;
    private BigDecimal variacaoMensal;
    private Integer totalAtivos;
}