package com.InvestIA.service;

import com.InvestIA.entity.Ativo;
import com.InvestIA.entity.Investimento;
import com.InvestIA.entity.Usuario;
import com.InvestIA.enums.NivelExperiencia;
import com.InvestIA.enums.TipoPerfil;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IAService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.timeout}")
    private long timeout;

    private OpenAiService getOpenAiService() {
        return new OpenAiService(openaiApiKey, Duration.ofMillis(timeout));
    }

    public String analisarPerfil(TipoPerfil tipoPerfil, NivelExperiencia nivelExp, Map<Integer, String> respostas) {
        try {
            String prompt = buildPerfilAnalysisPrompt(tipoPerfil, nivelExp, respostas);
            return callOpenAI(prompt, "Análise de Perfil de Investidor");
        } catch (Exception e) {
            log.error("Erro ao analisar perfil: {}", e.getMessage());
            return getDefaultPerfilAnalysis(tipoPerfil, nivelExp);
        }
    }

    public String gerarRecomendacoes(Usuario usuario, List<Investimento> investimentos, List<Ativo> ativosDisponiveis) {
        try {
            String prompt = buildRecommendationPrompt(usuario, investimentos, ativosDisponiveis);
            return callOpenAI(prompt, "Recomendações de Investimento");
        } catch (Exception e) {
            log.error("Erro ao gerar recomendações: {}", e.getMessage());
            return getDefaultRecommendations(usuario);
        }
    }

    public String analisarCarteira(List<Investimento> investimentos, BigDecimal valorTotal) {
        try {
            String prompt = buildPortfolioAnalysisPrompt(investimentos, valorTotal);
            return callOpenAI(prompt, "Análise de Carteira");
        } catch (Exception e) {
            log.error("Erro ao analisar carteira: {}", e.getMessage());
            return getDefaultPortfolioAnalysis(valorTotal);
        }
    }

    public String responderConsulta(String pergunta, Usuario usuario, List<Investimento> investimentos) {
        try {
            String prompt = buildConsultationPrompt(pergunta, usuario, investimentos);
            return callOpenAI(prompt, "Consulta sobre Investimentos");
        } catch (Exception e) {
            log.error("Erro ao responder consulta: {}", e.getMessage());
            return "Desculpe, não consegui processar sua pergunta no momento. Tente novamente ou entre em contato com nosso suporte.";
        }
    }

    private String callOpenAI(String prompt, String context) {
        try {
            OpenAiService service = getOpenAiService();
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
                "Você é um consultor financeiro especializado em investimentos no Brasil. " +
                "Forneça análises precisas, recomendações personalizadas e conselhos práticos. " +
                "Use linguagem clara e profissional. Foque em investimentos brasileiros (B3, Tesouro, etc.)."));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .maxTokens(500)
                    .temperature(0.7)
                    .build();

            String response = service.createChatCompletion(request)
                    .getChoices().get(0)
                    .getMessage().getContent();

            log.info("Resposta da OpenAI para {}: {}", context, response.substring(0, Math.min(100, response.length())));
            return response;
        } catch (Exception e) {
            log.error("Erro na chamada para OpenAI: {}", e.getMessage());
            throw e;
        }
    }

    private String buildPerfilAnalysisPrompt(TipoPerfil tipoPerfil, NivelExperiencia nivelExp, Map<Integer, String> respostas) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise o perfil de investidor com base nas seguintes informações:\n\n");
        prompt.append("Perfil identificado: ").append(tipoPerfil.getNome()).append("\n");
        prompt.append("Nível de experiência: ").append(nivelExp.getNome()).append("\n\n");
        prompt.append("Respostas do questionário:\n");
        
        respostas.forEach((pergunta, resposta) -> 
            prompt.append("Pergunta ").append(pergunta).append(": ").append(resposta).append("\n"));
        
        prompt.append("\nForneça uma análise detalhada do perfil, incluindo:\n");
        prompt.append("1. Características do perfil identificado\n");
        prompt.append("2. Tolerância ao risco\n");
        prompt.append("3. Estratégias de investimento recomendadas\n");
        prompt.append("4. Tipos de ativos mais adequados");
        
        return prompt.toString();
    }

    private String buildRecommendationPrompt(Usuario usuario, List<Investimento> investimentos, List<Ativo> ativosDisponiveis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Gere recomendações de investimento personalizadas para:\n\n");
        prompt.append("Usuário: ").append(usuario.getNome()).append("\n");
        
        if (usuario.getPerfil() != null) {
            prompt.append("Perfil: ").append(usuario.getPerfil().getTipoPerfil().getNome()).append("\n");
            prompt.append("Tolerância ao risco: ").append(usuario.getPerfil().getToleranciaRisco()).append("/10\n");
        }
        
        prompt.append("\nCarteira atual:\n");
        BigDecimal totalInvestido = investimentos.stream()
                .map(Investimento::getValorTotalInvestido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        prompt.append("Total investido: R$ ").append(totalInvestido).append("\n");
        
        investimentos.forEach(inv -> 
            prompt.append("- ").append(inv.getAtivo().getNome()).append(": R$ ")
                  .append(inv.getValorTotalInvestido()).append("\n"));
        
        prompt.append("\nAtivos disponíveis na plataforma:\n");
        ativosDisponiveis.stream().limit(10).forEach(ativo ->
            prompt.append("- ").append(ativo.getNome()).append(" (").append(ativo.getTipoAtivo().getNome()).append(")\n"));
        
        prompt.append("\nBaseado no perfil e carteira atual, forneça:\n");
        prompt.append("1. 3-5 recomendações específicas de ativos\n");
        prompt.append("2. Percentual sugerido para cada recomendação\n");
        prompt.append("3. Justificativa para cada recomendação\n");
        prompt.append("4. Estratégia de diversificação");
        
        return prompt.toString();
    }

    private String buildPortfolioAnalysisPrompt(List<Investimento> investimentos, BigDecimal valorTotal) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise a seguinte carteira de investimentos:\n\n");
        prompt.append("Valor total: R$ ").append(valorTotal).append("\n\n");
        prompt.append("Composição:\n");
        
        Map<String, BigDecimal> composicaoPorTipo = investimentos.stream()
                .collect(Collectors.groupingBy(
                    inv -> inv.getAtivo().getTipoAtivo().getNome(),
                    Collectors.reducing(BigDecimal.ZERO, Investimento::getValorTotalInvestido, BigDecimal::add)
                ));
        
        composicaoPorTipo.forEach((tipo, valor) -> {
            BigDecimal percentual = valor.divide(valorTotal, 4, BigDecimal.ROUND_HALF_UP)
                                        .multiply(BigDecimal.valueOf(100));
            prompt.append("- ").append(tipo).append(": R$ ").append(valor)
                  .append(" (").append(percentual).append("%)\n");
        });
        
        prompt.append("\nForneça uma análise incluindo:\n");
        prompt.append("1. Avaliação da diversificação\n");
        prompt.append("2. Nível de risco da carteira\n");
        prompt.append("3. Pontos fortes e fracos\n");
        prompt.append("4. Sugestões de rebalanceamento");
        
        return prompt.toString();
    }

    private String buildConsultationPrompt(String pergunta, Usuario usuario, List<Investimento> investimentos) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Pergunta do cliente: ").append(pergunta).append("\n\n");
        prompt.append("Contexto do cliente:\n");
        prompt.append("Nome: ").append(usuario.getNome()).append("\n");
        
        if (usuario.getPerfil() != null) {
            prompt.append("Perfil: ").append(usuario.getPerfil().getTipoPerfil().getNome()).append("\n");
        }
        
        if (!investimentos.isEmpty()) {
            BigDecimal totalInvestido = investimentos.stream()
                    .map(Investimento::getValorTotalInvestido)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            prompt.append("Total investido: R$ ").append(totalInvestido).append("\n");
        }
        
        prompt.append("\nResponda a pergunta de forma clara e personalizada, considerando o perfil e situação do cliente.");
        
        return prompt.toString();
    }

    private String getDefaultPerfilAnalysis(TipoPerfil tipoPerfil, NivelExperiencia nivelExp) {
        return String.format("Análise do perfil %s com nível de experiência %s. " +
                "Este perfil é caracterizado por uma abordagem %s aos investimentos. " +
                "Recomenda-se uma estratégia diversificada adequada ao seu nível de experiência.",
                tipoPerfil.getNome(), nivelExp.getNome(),
                tipoPerfil == TipoPerfil.CONSERVADOR ? "cautelosa" :
                tipoPerfil == TipoPerfil.MODERADO ? "equilibrada" : "arrojada");
    }

    private String getDefaultRecommendations(Usuario usuario) {
        return "Baseado no seu perfil, recomendamos uma carteira diversificada incluindo " +
               "Tesouro Direto, fundos de investimento e ações de empresas sólidas. " +
               "Sugerimos revisar periodicamente seus investimentos para manter o alinhamento com seus objetivos.";
    }

    private String getDefaultPortfolioAnalysis(BigDecimal valorTotal) {
        return String.format("Sua carteira possui um valor total de R$ %s. " +
                "Para uma análise mais detalhada, recomendamos diversificar entre diferentes " +
                "classes de ativos para reduzir riscos e otimizar retornos.", valorTotal);
    }
}