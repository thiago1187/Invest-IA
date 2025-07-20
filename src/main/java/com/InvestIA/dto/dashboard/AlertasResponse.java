package com.InvestIA.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertasResponse {
    private List<AlertaResponse> alertas;
    private Integer totalNaoLidos;
}