package com.InvestIA.dto.investimento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AtualizarInvestimentoRequest {
    private Integer quantidade;
    private BigDecimal valorMedioCompra;
}