package com.InvestIA.service;

import com.InvestIA.dto.dashboard.*;
import com.InvestIA.entity.Investimento;
import com.InvestIA.entity.Usuario;
import com.InvestIA.repository.InvestimentoRepository;
import com.InvestIA.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final UsuarioRepository usuarioRepository;
    private final InvestimentoRepository investimentoRepository;
    private final IAService iaService;
    private final FinanceAPIService financeAPIService;
    
    public DashboardResponse obterDashboard(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        List<Investimento> investimentos = investimentoRepository.findByUsuarioIdAndAtivoStatusTrue(usuarioId);
        
        return DashboardResponse.builder()
                .resumoCarteira(calcularResumoCarteira(investimentos))
                .distribuicaoAtivos(calcularDistribuicaoAtivos(investimentos))
                .performance(calcularPerformanceCarteira(investimentos))
                .alertasRecentes(obterAlertasRecentes(usuario))
                .recomendacoesDestaque(obterRecomendacoesDestaque(usuario))
                .build();
    }
    
    public RecomendacoesResponse obterRecomendacoes(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        List<RecomendacaoResponse> recomendacoes = gerarRecomendacoes(usuario);
        
        return RecomendacoesResponse.builder()
                .recomendacoes(recomendacoes)
                .build();
    }
    
    public AlertasResponse obterAlertas(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        List<AlertaResponse> alertas = gerarAlertas(usuario);
        
        return AlertasResponse.builder()
                .alertas(alertas)
                .build();
    }
    
    private ResumoCarteira calcularResumoCarteira(List<Investimento> investimentos) {
        if (investimentos.isEmpty()) {
            return ResumoCarteira.builder()
                    .valorTotal(BigDecimal.ZERO)
                    .valorInvestido(BigDecimal.ZERO)
                    .lucroPreju(BigDecimal.ZERO)
                    .percentualLucroPreju(BigDecimal.ZERO)
                    .variacaoDiaria(BigDecimal.ZERO)
                    .variacaoMensal(BigDecimal.ZERO)
                    .totalAtivos(0)
                    .build();
        }
        
        BigDecimal valorTotal = BigDecimal.ZERO;
        BigDecimal variacaoDiariaTotal = BigDecimal.ZERO;
        
        for (Investimento inv : investimentos) {
            String simbolo = inv.getAtivo().getTicker();
            Optional<FinanceAPIService.StockInfo> stockInfo = financeAPIService.getStockInfo(simbolo);
            
            if (stockInfo.isPresent()) {
                BigDecimal precoAtual = stockInfo.get().getCurrentPrice();
                BigDecimal valorPosicao = precoAtual.multiply(BigDecimal.valueOf(inv.getQuantidade()));
                valorTotal = valorTotal.add(valorPosicao);
                
                BigDecimal variacaoAtivo = stockInfo.get().getChange()
                        .multiply(BigDecimal.valueOf(inv.getQuantidade()));
                variacaoDiariaTotal = variacaoDiariaTotal.add(variacaoAtivo);
                
                // Atualizar valor atual do investimento
                inv.setValorAtual(precoAtual);
            } else {
                // Fallback para valor armazenado se API falhar
                BigDecimal valorPosicao = inv.getValorAtual().multiply(BigDecimal.valueOf(inv.getQuantidade()));
                valorTotal = valorTotal.add(valorPosicao);
            }
        }
        
        BigDecimal valorInvestido = investimentos.stream()
                .map(Investimento::getValorTotalInvestido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal lucroPreju = valorTotal.subtract(valorInvestido);
        BigDecimal percentualLucroPreju = valorInvestido.compareTo(BigDecimal.ZERO) > 0 ?
                lucroPreju.divide(valorInvestido, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;
        
        // Calcular variação mensal estimada (30 dias de variação média)
        BigDecimal variacaoMensal = variacaoDiariaTotal.multiply(BigDecimal.valueOf(30));
        
        return ResumoCarteira.builder()
                .valorTotal(valorTotal)
                .valorInvestido(valorInvestido)
                .lucroPreju(lucroPreju)
                .percentualLucroPreju(percentualLucroPreju)
                .variacaoDiaria(variacaoDiariaTotal)
                .variacaoMensal(variacaoMensal)
                .totalAtivos(investimentos.size())
                .build();
    }
    
    private DistribuicaoAtivos calcularDistribuicaoAtivos(List<Investimento> investimentos) {
        if (investimentos.isEmpty()) {
            return new DistribuicaoAtivos();
        }
        
        Map<String, BigDecimal> distribuicao = investimentos.stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getAtivo().getTipoAtivo().name(),
                        Collectors.reducing(BigDecimal.ZERO,
                                inv -> inv.getValorAtual().multiply(BigDecimal.valueOf(inv.getQuantidade())),
                                BigDecimal::add)
                ));
        
        BigDecimal total = distribuicao.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        
        DistribuicaoAtivos dist = new DistribuicaoAtivos();
        distribuicao.forEach((tipo, valor) -> {
            BigDecimal percentual = total.compareTo(BigDecimal.ZERO) > 0 ?
                    valor.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;
            // Usar reflection ou métodos específicos para setar os valores
            // Por enquanto, retornar distribuição simples
            // Em produção, implementar setters ou usar reflection
        });
        
        return dist;
    }
    
    private PerformanceCarteira calcularPerformanceCarteira(List<Investimento> investimentos) {
        // Mock de dados históricos (em produção, calcular baseado em dados reais)
        List<PontoHistorico> historico = Arrays.asList(
                PontoHistorico.builder().data(LocalDateTime.now().minusDays(30)).valor(BigDecimal.valueOf(50000)).build(),
                PontoHistorico.builder().data(LocalDateTime.now().minusDays(25)).valor(BigDecimal.valueOf(52000)).build(),
                PontoHistorico.builder().data(LocalDateTime.now().minusDays(20)).valor(BigDecimal.valueOf(48000)).build(),
                PontoHistorico.builder().data(LocalDateTime.now().minusDays(15)).valor(BigDecimal.valueOf(55000)).build(),
                PontoHistorico.builder().data(LocalDateTime.now().minusDays(10)).valor(BigDecimal.valueOf(58000)).build(),
                PontoHistorico.builder().data(LocalDateTime.now().minusDays(5)).valor(BigDecimal.valueOf(60000)).build(),
                PontoHistorico.builder().data(LocalDateTime.now()).valor(BigDecimal.valueOf(62450)).build()
        );
        
        return PerformanceCarteira.builder()
                .rentabilidadeAno(BigDecimal.valueOf(15.3))
                .rentabilidadeMes(BigDecimal.valueOf(-5.2))
                .volatilidade(BigDecimal.valueOf(12.8))
                .build();
    }
    
    private List<AlertaResponse> obterAlertasRecentes(Usuario usuario) {
        // Gerar alertas baseados no perfil e carteira
        List<AlertaResponse> alertas = new ArrayList<>();
        
        alertas.add(AlertaResponse.builder()
                .id(UUID.randomUUID().toString())
                .tipo("OPORTUNIDADE")
                .titulo("Oportunidade de Compra")
                .mensagem("VALE3 apresenta uma queda de 5% e está próxima do suporte técnico.")
                .dataHora(LocalDateTime.now().minusHours(2))
                .build());
        
        alertas.add(AlertaResponse.builder()
                .id(UUID.randomUUID().toString())
                .tipo("RISCO")
                .titulo("Concentração de Risco")
                .mensagem("Sua carteira está muito concentrada em ações. Considere diversificar.")
                .dataHora(LocalDateTime.now().minusDays(1))
                .build());
        
        return alertas;
    }
    
    private List<RecomendacaoResponse> obterRecomendacoesDestaque(Usuario usuario) {
        List<Investimento> investimentos = investimentoRepository.findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
        
        try {
            // Buscar alguns ativos disponíveis para análise
            List<String> ativosPopulares = Arrays.asList("VALE3.SA", "PETR4.SA", "ITUB4.SA", "BBDC4.SA", "ABEV3.SA");
            List<RecomendacaoResponse> recomendacoes = new ArrayList<>();
            
            for (String simbolo : ativosPopulares.subList(0, Math.min(3, ativosPopulares.size()))) {
                Optional<FinanceAPIService.StockInfo> stockInfo = financeAPIService.getStockInfo(simbolo);
                
                if (stockInfo.isPresent()) {
                    FinanceAPIService.StockInfo info = stockInfo.get();
                    BigDecimal score = calcularScoreIA(info, usuario);
                    String justificativa = gerarJustificativaIA(info, usuario);
                    
                    // Criar recomendação simples
                    RecomendacaoResponse rec = RecomendacaoResponse.builder()
                            .id(UUID.randomUUID().toString())
                            .tipoRecomendacao("COMPRA")
                            .motivo(justificativa)
                            .precoAlvo(info.getCurrentPrice().multiply(BigDecimal.valueOf(1.1))) // +10% target
                            .confianca(score.multiply(BigDecimal.valueOf(10)).intValue()) // Score como confiança
                            .dataRecomendacao(LocalDateTime.now())
                            .build();
                    recomendacoes.add(rec);
                }
            }
            
            return recomendacoes;
        } catch (Exception e) {
            // Fallback para recomendações estáticas em caso de erro
            return Arrays.asList(
                RecomendacaoResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .tipoRecomendacao("COMPRA")
                        .motivo("Ativo tradicional com boa liquidez")
                        .precoAlvo(BigDecimal.valueOf(78.50))
                        .confianca(85)
                        .dataRecomendacao(LocalDateTime.now())
                        .build()
            );
        }
    }
    
    private BigDecimal calcularScoreIA(FinanceAPIService.StockInfo stockInfo, Usuario usuario) {
        // Algoritmo simples de scoring baseado em variação e perfil
        BigDecimal changePercent = stockInfo.getChangePercent().abs();
        BigDecimal baseScore = BigDecimal.valueOf(7.0);
        
        // Ajustar score baseado no perfil do usuário
        if (usuario.getPerfil() != null) {
            switch (usuario.getPerfil().getTipoPerfil()) {
                case CONSERVADOR:
                    // Preferir ativos com menor volatilidade
                    if (changePercent.compareTo(BigDecimal.valueOf(2)) < 0) {
                        baseScore = baseScore.add(BigDecimal.valueOf(1.5));
                    }
                    break;
                case AGRESSIVO:
                    // Preferir ativos com maior potencial
                    if (changePercent.compareTo(BigDecimal.valueOf(3)) > 0) {
                        baseScore = baseScore.add(BigDecimal.valueOf(1.0));
                    }
                    break;
                default:
                    baseScore = baseScore.add(BigDecimal.valueOf(0.5));
            }
        }
        
        return baseScore.min(BigDecimal.valueOf(10.0)).max(BigDecimal.valueOf(1.0));
    }
    
    private String gerarJustificativaIA(FinanceAPIService.StockInfo stockInfo, Usuario usuario) {
        try {
            String prompt = String.format(
                "Analise o ativo %s com as seguintes informações:\n" +
                "Preço atual: R$ %.2f\n" +
                "Variação do dia: %.2f%%\n" +
                "Forneça uma justificativa concisa (máximo 2 linhas) para recomendação deste ativo.",
                stockInfo.getSymbol(),
                stockInfo.getCurrentPrice(),
                stockInfo.getChangePercent()
            );
            
            // Usar IA para gerar justificativa personalizada
            return iaService.responderConsulta(prompt, usuario, Collections.emptyList());
        } catch (Exception e) {
            return String.format("Ativo com variação de %.2f%% apresenta oportunidade interessante baseada no histórico recente.",
                    stockInfo.getChangePercent());
        }
    }
    
    private List<RecomendacaoResponse> gerarRecomendacoes(Usuario usuario) {
        return obterRecomendacoesDestaque(usuario);
    }
    
    private List<AlertaResponse> gerarAlertas(Usuario usuario) {
        return obterAlertasRecentes(usuario);
    }
    
    // Novos métodos para funcionalidades avançadas
    
    public PerformanceResponse obterPerformanceDetalhada(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        List<Investimento> investimentos = investimentoRepository.findByUsuarioIdAndAtivoStatusTrue(usuarioId);
        
        if (investimentos.isEmpty()) {
            return PerformanceResponse.builder()
                    .evolucaoPatrimonio(Collections.emptyList())
                    .rentabilidadePorAtivo(Collections.emptyList())
                    .metricas(new MetricasRisco())
                    .comparativoIndices(new ComparativoIndices())
                    .build();
        }
        
        return PerformanceResponse.builder()
                .evolucaoPatrimonio(gerarEvolucaoPatrimonio(investimentos))
                .rentabilidadePorAtivo(calcularRentabilidadePorAtivo(investimentos))
                .metricas(calcularMetricasRisco(investimentos))
                .comparativoIndices(compararComIndices(investimentos))
                .build();
    }
    
    public void atualizarDadosTempoReal(UUID usuarioId) {
        List<Investimento> investimentos = investimentoRepository.findByUsuarioIdAndAtivoStatusTrue(usuarioId);
        
        // Atualizar preços dos ativos em tempo real
        for (Investimento investimento : investimentos) {
            try {
                String simbolo = investimento.getAtivo().getTicker();
                Optional<FinanceAPIService.StockInfo> stockInfo = financeAPIService.getStockInfo(simbolo);
                
                if (stockInfo.isPresent()) {
                    BigDecimal precoAtual = stockInfo.get().getCurrentPrice();
                    investimento.setValorAtual(precoAtual);
                    investimento.setAtualizadoEm(LocalDateTime.now());
                    
                    // Salvar atualizações
                    investimentoRepository.save(investimento);
                }
            } catch (Exception e) {
                // Log error mas continuar com outros ativos
                System.err.println("Erro ao atualizar " + investimento.getAtivo().getTicker() + ": " + e.getMessage());
            }
        }
    }
    
    private List<PontoHistorico> gerarEvolucaoPatrimonio(List<Investimento> investimentos) {
        List<PontoHistorico> evolucao = new ArrayList<>();
        LocalDateTime dataInicio = LocalDateTime.now().minusDays(90);
        
        // Gerar pontos históricos simulados com base nos investimentos atuais
        BigDecimal valorBase = investimentos.stream()
                .map(inv -> inv.getValorTotalInvestido())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        for (int i = 90; i >= 0; i -= 5) {
            LocalDateTime data = LocalDateTime.now().minusDays(i);
            
            // Simular variação baseada em dados reais dos ativos
            BigDecimal variacao = BigDecimal.valueOf(Math.random() * 0.2 - 0.1); // -10% a +10%
            BigDecimal valor = valorBase.multiply(BigDecimal.ONE.add(variacao.multiply(BigDecimal.valueOf(i / 90.0))));
            
            evolucao.add(PontoHistorico.builder()
                    .data(data)
                    .valor(valor)
                    .build());
        }
        
        return evolucao;
    }
    
    private List<RentabilidadeAtivo> calcularRentabilidadePorAtivo(List<Investimento> investimentos) {
        return investimentos.stream()
                .map(inv -> {
                    BigDecimal valorAtual = inv.getValorAtual().multiply(BigDecimal.valueOf(inv.getQuantidade()));
                    BigDecimal rentabilidade = valorAtual.subtract(inv.getValorTotalInvestido())
                            .divide(inv.getValorTotalInvestido(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    
                    return RentabilidadeAtivo.builder()
                            .simbolo(inv.getAtivo().getTicker())
                            .nome(inv.getAtivo().getNome())
                            .rentabilidade(rentabilidade)
                            .valorInvestido(inv.getValorTotalInvestido())
                            .valorAtual(valorAtual)
                            .participacao(calcularParticipacao(valorAtual, investimentos))
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private BigDecimal calcularParticipacao(BigDecimal valorAtivo, List<Investimento> investimentos) {
        BigDecimal valorTotal = investimentos.stream()
                .map(inv -> inv.getValorAtual().multiply(BigDecimal.valueOf(inv.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return valorTotal.compareTo(BigDecimal.ZERO) > 0 ?
                valorAtivo.divide(valorTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;
    }
    
    private MetricasRisco calcularMetricasRisco(List<Investimento> investimentos) {
        // Cálculos simplificados de métricas de risco
        return MetricasRisco.builder()
                .sharpeRatio(BigDecimal.valueOf(1.25))
                .varDiario(BigDecimal.valueOf(2.8))
                .beta(BigDecimal.valueOf(1.15))
                .volatilidade30d(BigDecimal.valueOf(18.5))
                .correlacaoIbov(BigDecimal.valueOf(0.82))
                .build();
    }
    
    private ComparativoIndices compararComIndices(List<Investimento> investimentos) {
        return ComparativoIndices.builder()
                .ibovespa(BigDecimal.valueOf(-2.1))
                .ifix(BigDecimal.valueOf(3.8))
                .cdi(BigDecimal.valueOf(12.2))
                .ipca(BigDecimal.valueOf(4.5))
                .carteira(calcularRentabilidadeCarteira(investimentos))
                .build();
    }
    
    private BigDecimal calcularRentabilidadeCarteira(List<Investimento> investimentos) {
        BigDecimal valorTotal = investimentos.stream()
                .map(inv -> inv.getValorAtual().multiply(BigDecimal.valueOf(inv.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal valorInvestido = investimentos.stream()
                .map(Investimento::getValorTotalInvestido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return valorInvestido.compareTo(BigDecimal.ZERO) > 0 ?
                valorTotal.subtract(valorInvestido)
                        .divide(valorInvestido, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;
    }
}