package com.InvestIA.dto.investimento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvestimentoResponse {
    private UUID id;
    private AtivoResponse ativo;
    private Integer quantidade;
    private BigDecimal valorMedioCompra;
    private BigDecimal valorAtual;
    private BigDecimal valorTotalInvestido;
    private BigDecimal valorTotalAtual;
    private BigDecimal lucroPreju;
    private BigDecimal percentualLucroPreju;
    private LocalDate dataCompra;
}