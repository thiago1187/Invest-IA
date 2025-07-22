package com.InvestIA.service;

import com.InvestIA.entity.HistoricoConversa;
import com.InvestIA.entity.Investimento;
import com.InvestIA.entity.Usuario;
import com.InvestIA.repository.HistoricoConversaRepository;
import com.InvestIA.repository.InvestimentoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotService {
    
    private final IAService iaService;
    private final HistoricoConversaRepository historicoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final ObjectMapper objectMapper;
    
    public String responderComContexto(String pergunta, Usuario usuario) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Buscar histórico recente (últimas 24h)
            LocalDateTime ontem = LocalDateTime.now().minusDays(1);
            List<HistoricoConversa> historicoRecente = historicoRepository
                    .findRecentesByUsuario(usuario.getId(), ontem);
            
            // Buscar carteira atual
            List<Investimento> investimentos = investimentoRepository
                    .findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
            
            // Criar contexto personalizado
            String contextoPersonalizado = criarContextoPersonalizado(usuario, historicoRecente, investimentos);
            
            // Chamar IA com contexto enriquecido
            String resposta = iaService.responderConsultaComContexto(pergunta, usuario, investimentos, contextoPersonalizado);
            
            // Salvar no histórico
            HistoricoConversa conversa = HistoricoConversa.builder()
                    .usuario(usuario)
                    .pergunta(pergunta)
                    .resposta(resposta)
                    .tipo(classificarTipoConversa(pergunta))
                    .contextoCarteira(criarSnapshotCarteira(investimentos))
                    .tempoRespostaMs(System.currentTimeMillis() - startTime)
                    .build();
            
            historicoRepository.save(conversa);
            
            return resposta;
            
        } catch (Exception e) {
            log.error("Erro ao responder com contexto para usuário {}: {}", usuario.getId(), e.getMessage());
            throw new RuntimeException("Erro ao processar pergunta", e);
        }
    }
    
    public String analisarCarteiraPersonalizada(Usuario usuario) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<Investimento> investimentos = investimentoRepository
                    .findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
            
            if (investimentos.isEmpty()) {
                return "Sua carteira ainda está vazia. Vamos começar? Com base no seu perfil " +
                       usuario.getPerfil().getTipoPerfil().getDescricao().toLowerCase() +
                       ", posso sugerir alguns investimentos alinhados com seu perfil.";
            }
            
            // Buscar análises anteriores para comparar evolução
            List<HistoricoConversa> analisesPrevias = historicoRepository
                    .findByUsuarioAndTipo(usuario.getId(), HistoricoConversa.TipoConversa.ANALISE_CARTEIRA, null);
            
            BigDecimal valorTotal = calcularValorTotalCarteira(investimentos);
            
            String analiseCompleta = iaService.analisarCarteiraComHistorico(
                    investimentos, valorTotal, usuario, analisesPrevias.subList(0, Math.min(3, analisesPrevias.size()))
            );
            
            // Salvar análise
            HistoricoConversa conversa = HistoricoConversa.builder()
                    .usuario(usuario)
                    .pergunta("Análise de carteira")
                    .resposta(analiseCompleta)
                    .tipo(HistoricoConversa.TipoConversa.ANALISE_CARTEIRA)
                    .contextoCarteira(criarSnapshotCarteira(investimentos))
                    .tempoRespostaMs(System.currentTimeMillis() - startTime)
                    .build();
            
            historicoRepository.save(conversa);
            
            return analiseCompleta;
            
        } catch (Exception e) {
            log.error("Erro ao analisar carteira para usuário {}: {}", usuario.getId(), e.getMessage());
            throw new RuntimeException("Erro ao analisar carteira", e);
        }
    }
    
    public String gerarRecomendacoesPersonalizadas(Usuario usuario) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<Investimento> investimentos = investimentoRepository
                    .findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
            
            // Buscar recomendações anteriores para evitar repetição
            List<HistoricoConversa> recomendacoesPrevias = historicoRepository
                    .findByUsuarioAndTipo(usuario.getId(), HistoricoConversa.TipoConversa.RECOMENDACOES, null);
            
            // Extrair padrões das recomendações anteriores
            String padroesBehavior = analisarPadroesRecomendacoes(recomendacoesPrevias.subList(0, Math.min(5, recomendacoesPrevias.size())));
            
            String recomendacoes = iaService.gerarRecomendacoesPersonalizadas(
                    usuario, investimentos, padroesBehavior
            );
            
            // Salvar recomendações
            HistoricoConversa conversa = HistoricoConversa.builder()
                    .usuario(usuario)
                    .pergunta("Recomendações personalizadas")
                    .resposta(recomendacoes)
                    .tipo(HistoricoConversa.TipoConversa.RECOMENDACOES)
                    .contextoCarteira(criarSnapshotCarteira(investimentos))
                    .tempoRespostaMs(System.currentTimeMillis() - startTime)
                    .build();
            
            historicoRepository.save(conversa);
            
            return recomendacoes;
            
        } catch (Exception e) {
            log.error("Erro ao gerar recomendações para usuário {}: {}", usuario.getId(), e.getMessage());
            throw new RuntimeException("Erro ao gerar recomendações", e);
        }
    }
    
    public List<HistoricoConversa> obterHistoricoRecente(Usuario usuario, int limite) {
        return historicoRepository.findTop10ByUsuarioIdOrderByCriadoEmDesc(usuario.getId())
                .subList(0, Math.min(limite, 10));
    }
    
    public void avaliarResposta(UUID conversaId, Integer avaliacao) {
        historicoRepository.findById(conversaId)
                .ifPresent(conversa -> {
                    conversa.setAvaliacaoUsuario(avaliacao);
                    historicoRepository.save(conversa);
                });
    }
    
    private String criarContextoPersonalizado(Usuario usuario, List<HistoricoConversa> historico, List<Investimento> investimentos) {
        StringBuilder contexto = new StringBuilder();
        
        // Informações do perfil
        if (usuario.getPerfil() != null) {
            contexto.append("Perfil do usuário: ")
                    .append(usuario.getPerfil().getTipoPerfil().getDescricao())
                    .append(" (").append(usuario.getPerfil().getNivelExperiencia().name()).append("). ");
        }
        
        // Resumo da carteira
        if (!investimentos.isEmpty()) {
            BigDecimal valorTotal = calcularValorTotalCarteira(investimentos);
            contexto.append("Carteira atual: R$ ").append(valorTotal.toString())
                    .append(" distribuída em ").append(investimentos.size()).append(" ativos. ");
        }
        
        // Padrões do histórico recente
        if (!historico.isEmpty()) {
            Map<HistoricoConversa.TipoConversa, Long> tiposFrequentes = historico.stream()
                    .collect(Collectors.groupingBy(HistoricoConversa::getTipo, Collectors.counting()));
            
            contexto.append("Assuntos de interesse recente: ");
            tiposFrequentes.entrySet().stream()
                    .sorted(Map.Entry.<HistoricoConversa.TipoConversa, Long>comparingByValue().reversed())
                    .limit(2)
                    .forEach(entry -> contexto.append(entry.getKey().name()).append(" "));
        }
        
        return contexto.toString();
    }
    
    private HistoricoConversa.TipoConversa classificarTipoConversa(String pergunta) {
        String perguntaLower = pergunta.toLowerCase();
        
        if (perguntaLower.contains("analis") || perguntaLower.contains("carteira")) {
            return HistoricoConversa.TipoConversa.ANALISE_CARTEIRA;
        }
        if (perguntaLower.contains("recomen") || perguntaLower.contains("suger") || perguntaLower.contains("comprar")) {
            return HistoricoConversa.TipoConversa.RECOMENDACOES;
        }
        if (perguntaLower.contains("como") || perguntaLower.contains("aprend") || perguntaLower.contains("ensina")) {
            return HistoricoConversa.TipoConversa.EDUCACIONAL;
        }
        if (perguntaLower.contains("problem") || perguntaLower.contains("erro") || perguntaLower.contains("ajuda")) {
            return HistoricoConversa.TipoConversa.SUPORTE;
        }
        
        return HistoricoConversa.TipoConversa.PERGUNTA_GERAL;
    }
    
    private String criarSnapshotCarteira(List<Investimento> investimentos) {
        try {
            return objectMapper.writeValueAsString(investimentos.stream()
                    .collect(Collectors.toMap(
                            inv -> inv.getAtivo().getTicker(),
                            inv -> Map.of(
                                    "quantidade", inv.getQuantidade(),
                                    "valorAtual", inv.getValorAtual(),
                                    "valorMedio", inv.getValorMedioCompra()
                            )
                    )));
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private BigDecimal calcularValorTotalCarteira(List<Investimento> investimentos) {
        return investimentos.stream()
                .map(inv -> inv.getValorAtual().multiply(BigDecimal.valueOf(inv.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private String analisarPadroesRecomendacoes(List<HistoricoConversa> recomendacoesPrevias) {
        if (recomendacoesPrevias.isEmpty()) {
            return "Usuário sem histórico de recomendações anteriores.";
        }
        
        StringBuilder padroes = new StringBuilder();
        padroes.append("HISTÓRICO DE RECOMENDAÇÕES ANTERIORES:\n");
        padroes.append("Total de recomendações: ").append(recomendacoesPrevias.size()).append("\n");
        
        recomendacoesPrevias.forEach(rec -> 
            padroes.append("- ").append(rec.getCriadoEm().toLocalDate())
                   .append(": ").append(rec.getResposta().substring(0, Math.min(200, rec.getResposta().length())))
                   .append("...\n"));
        
        padroes.append("INSTRUÇÃO: Evite repetir exatamente as mesmas recomendações anteriores.");
        
        return padroes.toString();
    }
}