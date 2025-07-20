package com.InvestIA.dto.investimento;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CriarInvestimentoRequest {
    @NotBlank(message = "Ticker é obrigatório")
    private String ticker;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;
    
    @NotNull(message = "Valor de compra é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valorCompra;
    
    @NotNull(message = "Data de compra é obrigatória")
    @PastOrPresent(message = "Data de compra não pode ser futura")
    private LocalDate dataCompra;
}