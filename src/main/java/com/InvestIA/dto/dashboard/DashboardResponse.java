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
public class DashboardResponse {
    private ResumoCarteira resumoCarteira;
    private DistribuicaoAtivos distribuicaoAtivos;
    private PerformanceCarteira performance;
    private List<AlertaResponse> alertasRecentes;
    private List<RecomendacaoResponse> recomendacoesDestaque;
}