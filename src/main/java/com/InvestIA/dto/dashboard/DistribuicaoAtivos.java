package com.InvestIA.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistribuicaoAtivos {
    private Map<String, BigDecimal> porTipo; // ACAO: 45%, FII: 30%, etc
    private Map<String, BigDecimal> porSetor; // TECNOLOGIA: 20%, FINANCEIRO: 15%, etc
    private BigDecimal percentualRendaVariavel;
    private BigDecimal percentualRendaFixa;
}