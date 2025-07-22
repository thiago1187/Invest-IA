package com.InvestIA.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PerformanceResponse {
    private List<PontoHistorico> evolucaoPatrimonio;
    private List<RentabilidadeAtivo> rentabilidadePorAtivo;
    private MetricasRisco metricas;
    private ComparativoIndices comparativoIndices;
}