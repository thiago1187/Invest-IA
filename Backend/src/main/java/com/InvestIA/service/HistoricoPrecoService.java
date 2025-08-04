package com.InvestIA.service;

import com.InvestIA.entity.Ativo;
import com.InvestIA.entity.HistoricoPreco;
import com.InvestIA.repository.AtivoRepository;
import com.InvestIA.repository.HistoricoPrecoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricoPrecoService {
    
    private final HistoricoPrecoRepository historicoPrecoRepository;
    private final AtivoRepository ativoRepository;
    private final FinanceAPIService financeAPIService;
    
    /**
     * Sincroniza histórico de preços para um ativo específico
     */
    @Async
    @Transactional
    public CompletableFuture<Void> sincronizarHistoricoAtivo(Ativo ativo, int diasHistorico) {
        try {
            log.info("📈 Sincronizando histórico de {} dias para {}", diasHistorico, ativo.getTicker());
            
            LocalDate dataInicio = LocalDate.now().minusDays(diasHistorico);
            LocalDate dataFim = LocalDate.now();
            
            // Buscar histórico existente
            List<HistoricoPreco> historicoExistente = historicoPrecoRepository
                .findByAtivoAndDataBetween(ativo, dataInicio, dataFim);
            
            Set<LocalDate> datasExistentes = new HashSet<>();
            historicoExistente.forEach(h -> datasExistentes.add(h.getData()));
            
            // Gerar dados históricos simulados para os dias que faltam
            List<HistoricoPreco> novosRegistros = new ArrayList<>();
            LocalDate dataAtual = dataInicio;
            BigDecimal precoBase = ativo.getPrecoAtual() != null ? ativo.getPrecoAtual() : BigDecimal.valueOf(50.0);
            
            while (!dataAtual.isAfter(dataFim)) {
                if (!datasExistentes.contains(dataAtual) && !dataAtual.isAfter(LocalDate.now())) {
                    // Gerar preço simulado baseado no preço atual com variação aleatória
                    BigDecimal variacao = gerarVariacaoRealista();
                    BigDecimal precoFechamento = precoBase.multiply(BigDecimal.ONE.add(variacao))
                        .setScale(2, RoundingMode.HALF_UP);
                    
                    HistoricoPreco novoHistorico = HistoricoPreco.builder()
                        .ativo(ativo)
                        .data(dataAtual)
                        .precoFechamento(precoFechamento)
                        .precoAbertura(precoFechamento.multiply(BigDecimal.valueOf(0.98 + Math.random() * 0.04)))
                        .precoMaximo(precoFechamento.multiply(BigDecimal.valueOf(1.00 + Math.random() * 0.03)))
                        .precoMinimo(precoFechamento.multiply(BigDecimal.valueOf(0.97 + Math.random() * 0.03)))
                        .volume((long) (Math.random() * 1000000 + 100000))
                        .variacaoPercentual(variacao.multiply(BigDecimal.valueOf(100)))
                        .build();
                    
                    novosRegistros.add(novoHistorico);
                    precoBase = precoFechamento; // Usar como base para o próximo dia
                }
                dataAtual = dataAtual.plusDays(1);
            }
            
            if (!novosRegistros.isEmpty()) {
                historicoPrecoRepository.saveAll(novosRegistros);
                log.info("✅ Salvos {} registros históricos para {}", novosRegistros.size(), ativo.getTicker());
            }
            
        } catch (Exception e) {
            log.error("❌ Erro ao sincronizar histórico para {}: {}", ativo.getTicker(), e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Sincroniza histórico para todos os ativos ativos
     */
    @Async
    @Transactional
    public CompletableFuture<Void> sincronizarTodosAtivos(int diasHistorico) {
        try {
            List<Ativo> ativos = ativoRepository.findByStatusTrue();
            log.info("🔄 Iniciando sincronização de histórico para {} ativos", ativos.size());
            
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (Ativo ativo : ativos) {
                CompletableFuture<Void> future = sincronizarHistoricoAtivo(ativo, diasHistorico);
                futures.add(future);
            }
            
            // Aguardar todas as sincronizações completarem
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("✅ Sincronização de histórico concluída para todos os ativos");
            
        } catch (Exception e) {
            log.error("❌ Erro na sincronização geral de histórico: {}", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Busca histórico de preços para um ativo nos últimos N dias
     */
    public List<HistoricoPreco> obterHistorico(Ativo ativo, int dias) {
        LocalDate dataInicio = LocalDate.now().minusDays(dias);
        return historicoPrecoRepository.findByAtivoAndDataAfter(ativo, dataInicio);
    }
    
    /**
     * Busca histórico para múltiplos ativos
     */
    public List<HistoricoPreco> obterHistoricoMultiplosAtivos(List<Ativo> ativos, int dias) {
        LocalDate dataInicio = LocalDate.now().minusDays(dias);
        return historicoPrecoRepository.findByAtivosAndDataAfter(ativos, dataInicio);
    }
    
    /**
     * Obter preço mais recente de um ativo
     */
    public Optional<HistoricoPreco> obterPrecoMaisRecente(Ativo ativo) {
        return historicoPrecoRepository.findLatestByAtivo(ativo);
    }
    
    /**
     * Calcular evolução percentual de um ativo no período
     */
    public BigDecimal calcularEvolucaoPercentual(Ativo ativo, int dias) {
        List<HistoricoPreco> historico = obterHistorico(ativo, dias);
        
        if (historico.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal precoInicial = historico.get(0).getPrecoFechamento();
        BigDecimal precoFinal = historico.get(historico.size() - 1).getPrecoFechamento();
        
        if (precoInicial.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return precoFinal.subtract(precoInicial)
            .divide(precoInicial, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Calcular volatilidade de um ativo (desvio padrão dos retornos diários)
     */
    public BigDecimal calcularVolatilidade(Ativo ativo, int dias) {
        List<HistoricoPreco> historico = obterHistorico(ativo, dias);
        
        if (historico.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> retornosDiarios = new ArrayList<>();
        
        for (int i = 1; i < historico.size(); i++) {
            BigDecimal precoAnterior = historico.get(i - 1).getPrecoFechamento();
            BigDecimal precoAtual = historico.get(i).getPrecoFechamento();
            
            if (precoAnterior.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal retorno = precoAtual.subtract(precoAnterior)
                    .divide(precoAnterior, 6, RoundingMode.HALF_UP);
                retornosDiarios.add(retorno);
            }
        }
        
        if (retornosDiarios.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calcular média dos retornos
        BigDecimal media = retornosDiarios.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(retornosDiarios.size()), 6, RoundingMode.HALF_UP);
        
        // Calcular variância
        BigDecimal variancia = retornosDiarios.stream()
            .map(retorno -> retorno.subtract(media).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(retornosDiarios.size()), 6, RoundingMode.HALF_UP);
        
        // Retornar desvio padrão (raiz quadrada da variância) em percentual
        return BigDecimal.valueOf(Math.sqrt(variancia.doubleValue()) * 100);
    }
    
    /**
     * Gera variação realista para simulação de preços
     */
    private BigDecimal gerarVariacaoRealista() {
        // Usar distribuição normal para gerar variações mais realistas
        Random random = new Random();
        double variacao = random.nextGaussian() * 0.02; // Desvio padrão de 2%
        
        // Limitar variações extremas
        variacao = Math.max(-0.10, Math.min(0.10, variacao)); // Entre -10% e +10%
        
        return BigDecimal.valueOf(variacao).setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Inicializar histórico para novos ativos
     */
    @Transactional
    public void inicializarHistoricoParaNovoAtivo(Ativo ativo) {
        Long count = historicoPrecoRepository.countByAtivo(ativo);
        
        if (count == 0) {
            log.info("🆕 Inicializando histórico para novo ativo: {}", ativo.getTicker());
            sincronizarHistoricoAtivo(ativo, 90); // 90 dias de histórico
        }
    }
}