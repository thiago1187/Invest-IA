package com.InvestIA.service;

import com.InvestIA.entity.*;
import com.InvestIA.repository.HistoricoConversaRepository;
import com.InvestIA.repository.InvestimentoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotPersonalizadoService {
    
    private final IAService iaService;
    private final HistoricoConversaRepository historicoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final ObjectMapper objectMapper;
    
    public String responderComContexto(String pergunta, Usuario usuario, List<Investimento> investimentos) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Buscar histórico recente para contexto
            List<HistoricoConversa> historicoRecente = historicoRepository
                    .findRecentesByUsuario(usuario.getId(), LocalDateTime.now().minusDays(7));
            
            // 2. Construir contexto personalizado
            String contextoPersonalizado = construirContextoPersonalizado(usuario, investimentos, historicoRecente);
            
            // 3. Gerar resposta com IA considerando o contexto
            String resposta = iaService.responderComContextoPersonalizado(pergunta, contextoPersonalizado);
            
            // 4. Salvar conversa no histórico
            salvarConversaHistorico(pergunta, resposta, usuario, investimentos, 
                    HistoricoConversa.TipoConversa.PERGUNTA_GERAL, startTime);
            
            return resposta;
            
        } catch (Exception e) {
            log.error("Erro ao processar pergunta com contexto: {}", e.getMessage(), e);
            
            // Fallback para resposta simples
            String respostaFallback = iaService.responderConsulta(pergunta, usuario, investimentos);
            salvarConversaHistorico(pergunta, respostaFallback, usuario, investimentos, 
                    HistoricoConversa.TipoConversa.PERGUNTA_GERAL, startTime);
            
            return respostaFallback;
        }
    }
    
    public String analisarCarteiraPersonalizada(Usuario usuario, List<Investimento> investimentos) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Buscar análises anteriores para comparação
            List<HistoricoConversa> analisesAnteriores = historicoRepository
                    .findByUsuarioAndTipo(usuario.getId(), HistoricoConversa.TipoConversa.ANALISE_CARTEIRA, 
                            PageRequest.of(0, 3));
            
            String contextoEvolucao = construirContextoEvolucaoCarteira(analisesAnteriores, investimentos);
            String analise = iaService.analisarCarteiraComEvolucao(investimentos, contextoEvolucao);
            
            salvarConversaHistorico("Análise de carteira personalizada", analise, usuario, investimentos,
                    HistoricoConversa.TipoConversa.ANALISE_CARTEIRA, startTime);
            
            return analise;
            
        } catch (Exception e) {
            log.error("Erro na análise personalizada: {}", e.getMessage(), e);
            
            // Fallback para análise simples
            java.math.BigDecimal valorTotal = calcularValorTotalCarteira(investimentos);
            String analiseFallback = iaService.analisarCarteira(investimentos, valorTotal);
            
            salvarConversaHistorico("Análise de carteira (fallback)", analiseFallback, usuario, investimentos,
                    HistoricoConversa.TipoConversa.ANALISE_CARTEIRA, startTime);
            
            return analiseFallback;
        }
    }
    
    public String gerarRecomendacoesPersonalizadas(Usuario usuario, List<Investimento> investimentos) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Buscar padrões do histórico para personalizar recomendações
            List<HistoricoConversa> historicoCompleto = historicoRepository
                    .findTop10ByUsuarioIdOrderByCriadoEmDesc(usuario.getId());
            
            String padroesBehavior = analisarPadroesComportamento(historicoCompleto);
            String recomendacoes = iaService.gerarRecomendacoesPersonalizadas(
                    usuario, investimentos, padroesBehavior);
            
            salvarConversaHistorico("Recomendações personalizadas", recomendacoes, usuario, investimentos,
                    HistoricoConversa.TipoConversa.RECOMENDACOES, startTime);
            
            return recomendacoes;
            
        } catch (Exception e) {
            log.error("Erro nas recomendações personalizadas: {}", e.getMessage(), e);
            
            // Fallback
            String recomendacoesFallback = iaService.gerarRecomendacoes(usuario, investimentos, List.of());
            salvarConversaHistorico("Recomendações (fallback)", recomendacoesFallback, usuario, investimentos,
                    HistoricoConversa.TipoConversa.RECOMENDACOES, startTime);
            
            return recomendacoesFallback;
        }
    }
    
    public List<HistoricoConversa> obterHistoricoUsuario(Usuario usuario, int limite) {
        return historicoRepository.findTop10ByUsuarioIdOrderByCriadoEmDesc(usuario.getId())
                .stream()
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    public void avaliarResposta(UUID conversaId, Integer avaliacao) {
        historicoRepository.findById(conversaId).ifPresent(conversa -> {
            conversa.setAvaliacaoUsuario(avaliacao);
            historicoRepository.save(conversa);
        });
    }
    
    private String construirContextoPersonalizado(Usuario usuario, List<Investimento> investimentos, 
                                                List<HistoricoConversa> historicoRecente) {
        
        StringBuilder contexto = new StringBuilder();
        
        // Perfil do usuário
        contexto.append("PERFIL DO USUÁRIO:\n");
        contexto.append("Nome: ").append(usuario.getNome()).append("\n");
        
        if (usuario.getPerfil() != null) {
            contexto.append("Perfil de Risco: ").append(usuario.getPerfil().getTipoPerfil()).append("\n");
            contexto.append("Experiência: ").append(usuario.getPerfil().getNivelExperiencia()).append("\n");
            contexto.append("Tolerância ao Risco: ").append(usuario.getPerfil().getToleranciaRisco()).append("/10\n");
        }
        
        // Resumo da carteira
        contexto.append("\nCARTEIRA ATUAL:\n");
        if (investimentos.isEmpty()) {
            contexto.append("Carteira vazia - usuário iniciante\n");
        } else {
            java.math.BigDecimal valorTotal = calcularValorTotalCarteira(investimentos);
            contexto.append("Valor total: R$ ").append(valorTotal).append("\n");
            contexto.append("Ativos na carteira: ").append(investimentos.size()).append("\n");
            
            // Top 3 posições
            investimentos.stream()
                    .limit(3)
                    .forEach(inv -> contexto.append("- ")
                            .append(inv.getAtivo().getTicker())
                            .append(": R$ ")
                            .append(inv.getValorAtual().multiply(java.math.BigDecimal.valueOf(inv.getQuantidade())))
                            .append("\n"));
        }
        
        // Histórico recente de perguntas
        if (!historicoRecente.isEmpty()) {
            contexto.append("\nTÓPICOS RECENTES DE INTERESSE:\n");
            historicoRecente.stream()
                    .limit(5)
                    .forEach(h -> contexto.append("- ").append(h.getPergunta()).append("\n"));
        }
        
        return contexto.toString();
    }
    
    private String construirContextoEvolucaoCarteira(List<HistoricoConversa> analisesAnteriores, 
                                                   List<Investimento> investimentosAtuais) {
        
        if (analisesAnteriores.isEmpty()) {
            return "Primeira análise da carteira - sem histórico anterior.";
        }
        
        StringBuilder contexto = new StringBuilder();
        contexto.append("EVOLUÇÃO DA CARTEIRA:\n");
        contexto.append("Análises anteriores: ").append(analisesAnteriores.size()).append("\n");
        
        // Última análise como referência
        HistoricoConversa ultimaAnalise = analisesAnteriores.get(0);
        contexto.append("Última análise em: ").append(ultimaAnalise.getCriadoEm()).append("\n");
        
        return contexto.toString();
    }
    
    private String analisarPadroesComportamento(List<HistoricoConversa> historico) {
        if (historico.isEmpty()) {
            return "Usuário novo - sem padrões identificados ainda.";
        }
        
        // Análise simples de padrões
        Map<HistoricoConversa.TipoConversa, Long> tiposFrequentes = historico.stream()
                .collect(Collectors.groupingBy(HistoricoConversa::getTipo, Collectors.counting()));
        
        StringBuilder padroes = new StringBuilder();
        padroes.append("PADRÕES DE USO:\n");
        tiposFrequentes.forEach((tipo, count) -> 
                padroes.append("- ").append(tipo).append(": ").append(count).append(" vezes\n"));
        
        return padroes.toString();
    }
    
    private java.math.BigDecimal calcularValorTotalCarteira(List<Investimento> investimentos) {
        return investimentos.stream()
                .map(inv -> inv.getValorAtual().multiply(java.math.BigDecimal.valueOf(inv.getQuantidade())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    private void salvarConversaHistorico(String pergunta, String resposta, Usuario usuario, 
                                       List<Investimento> investimentos, HistoricoConversa.TipoConversa tipo, 
                                       long startTime) {
        try {
            // Criar snapshot da carteira
            String contextoCarteira = criarSnapshotCarteira(investimentos);
            
            HistoricoConversa conversa = HistoricoConversa.builder()
                    .usuario(usuario)
                    .pergunta(pergunta)
                    .resposta(resposta)
                    .tipo(tipo)
                    .contextoCarteira(contextoCarteira)
                    .tempoRespostaMs(System.currentTimeMillis() - startTime)
                    .build();
            
            historicoRepository.save(conversa);
            
        } catch (Exception e) {
            log.error("Erro ao salvar histórico: {}", e.getMessage(), e);
        }
    }
    
    private String criarSnapshotCarteira(List<Investimento> investimentos) {
        try {
            var snapshot = investimentos.stream()
                    .collect(Collectors.toMap(
                            inv -> inv.getAtivo().getTicker(),
                            inv -> Map.of(
                                    "quantidade", inv.getQuantidade(),
                                    "valorAtual", inv.getValorAtual(),
                                    "valorTotal", inv.getValorAtual().multiply(java.math.BigDecimal.valueOf(inv.getQuantidade()))
                            )
                    ));
            
            return objectMapper.writeValueAsString(snapshot);
            
        } catch (JsonProcessingException e) {
            log.warn("Erro ao serializar snapshot da carteira: {}", e.getMessage());
            return "{}";
        }
    }
}