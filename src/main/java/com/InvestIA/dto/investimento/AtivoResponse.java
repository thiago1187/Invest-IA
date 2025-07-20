package com.InvestIA.dto.investimento;

import com.InvestIA.enums.TipoAtivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AtivoResponse {
    private UUID id;
    private String ticker;
    private String nome;
    private TipoAtivo tipoAtivo;
    private BigDecimal precoAtual;
    private BigDecimal variacaoDiaria;
}