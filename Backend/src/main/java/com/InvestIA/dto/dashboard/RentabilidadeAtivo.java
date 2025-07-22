package com.InvestIA.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RentabilidadeAtivo {
    private String simbolo;
    private String nome;
    private BigDecimal rentabilidade;
    private BigDecimal valorInvestido;
    private BigDecimal valorAtual;
    private BigDecimal participacao;
}