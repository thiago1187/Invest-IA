package com.InvestIA.service;

import com.InvestIA.entity.Ativo;
import com.InvestIA.entity.Investimento;
import com.InvestIA.entity.Usuario;
import com.InvestIA.enums.NivelExperiencia;
import com.InvestIA.enums.TipoPerfil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IAService {

    @Value("${claude.api.key}")
    private String claudeApiKey;

    @Value("${claude.api.url}")
    private String claudeApiUrl;

    @Value("${claude.api.model}")
    private String claudeModel;

    @Value("${claude.api.timeout}")
    private long timeout;

    // DeepSeek backup
    @Value("${deepseek.api.key}")
    private String deepseekApiKey;

    @Value("${deepseek.api.url}")
    private String deepseekApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public IAService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String analisarPerfil(TipoPerfil tipoPerfil, NivelExperiencia nivelExp, Map<Integer, String> respostas) {
        try {
            String prompt = buildPerfilAnalysisPrompt(tipoPerfil, nivelExp, respostas);
            return callClaude(prompt, "Análise de Perfil de Investidor");
        } catch (Exception e) {
            log.error("Erro ao analisar perfil: {}", e.getMessage());
            return getDefaultPerfilAnalysis(tipoPerfil, nivelExp);
        }
    }

    public String gerarRecomendacoes(Usuario usuario, List<Investimento> investimentos, List<Ativo> ativosDisponiveis) {
        try {
            String prompt = buildRecommendationPrompt(usuario, investimentos, ativosDisponiveis);
            return callClaude(prompt, "Recomendações de Investimento");
        } catch (Exception e) {
            log.error("Erro ao gerar recomendações: {}", e.getMessage());
            return getDefaultRecommendations(usuario);
        }
    }

    public String analisarCarteira(List<Investimento> investimentos, BigDecimal valorTotal) {
        try {
            String prompt = buildPortfolioAnalysisPrompt(investimentos, valorTotal);
            return callClaude(prompt, "Análise de Carteira");
        } catch (Exception e) {
            log.error("Erro ao analisar carteira: {}", e.getMessage());
            return getDefaultPortfolioAnalysis(valorTotal);
        }
    }

    public String responderConsulta(String pergunta, Usuario usuario, List<Investimento> investimentos) {
        try {
            String prompt = buildConsultationPrompt(pergunta, usuario, investimentos);
            return callClaude(prompt, "Consulta sobre Investimentos");
        } catch (Exception e) {
            log.error("Erro ao responder consulta: {}", e.getMessage());
            return "Desculpe, não consegui processar sua pergunta no momento. Tente novamente ou entre em contato com nosso suporte.";
        }
    }

    private String callClaude(String prompt, String context) {
        // Se não tiver chave configurada, tentar DeepSeek ou usar resposta padrão
        if (claudeApiKey == null || claudeApiKey.equals("sk-placeholder-key")) {
            log.info("Claude não configurado, tentando DeepSeek para: {}", context);
            return callDeepSeek(prompt, context);
        }
        
        try {
            // Criar headers para Claude API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", claudeApiKey);
            headers.set("anthropic-version", "2023-06-01");

            // Criar corpo da requisição para Claude
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", claudeModel);
            requestBody.put("max_tokens", 1000);
            requestBody.put("system", "Você é Nina, uma consultora financeira especializada em investimentos no Brasil. " +
                "Forneça análises precisas, recomendações personalizadas e conselhos práticos. " +
                "Use linguagem clara, amigável e profissional. Foque em investimentos brasileiros (B3, Tesouro, etc.).");
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Fazer chamada para Claude
            ResponseEntity<String> response = restTemplate.exchange(
                claudeApiUrl, 
                HttpMethod.POST, 
                entity, 
                String.class
            );

            // Parsear resposta do Claude
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("content")
                                      .get(0)
                                      .path("text")
                                      .asText();

            log.info("Resposta do Claude para {}: {}", context, content.substring(0, Math.min(100, content.length())));
            return content;
        } catch (Exception e) {
            log.error("Erro na chamada para Claude: {}, tentando DeepSeek", e.getMessage());
            return callDeepSeek(prompt, context);
        }
    }

    private String callDeepSeek(String prompt, String context) {
        // Se não tiver chave configurada, usar resposta padrão
        if (deepseekApiKey == null || deepseekApiKey.equals("sk-placeholder-key")) {
            log.info("DeepSeek não configurado, usando resposta padrão para: {}", context);
            return getSmartDefaultResponse(prompt, context);
        }
        
        try {
            // Criar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey);

            // Criar corpo da requisição
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", 
                "Você é um consultor financeiro especializado em investimentos no Brasil. " +
                "Forneça análises precisas, recomendações personalizadas e conselhos práticas. " +
                "Use linguagem clara e profissional. Foque em investimentos brasileiros (B3, Tesouro, etc.).");
            messages.add(systemMessage);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Fazer chamada para DeepSeek
            ResponseEntity<String> response = restTemplate.exchange(
                deepseekApiUrl, 
                HttpMethod.POST, 
                entity, 
                String.class
            );

            // Parsear resposta
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices")
                                      .get(0)
                                      .path("message")
                                      .path("content")
                                      .asText();

            log.info("Resposta do DeepSeek para {}: {}", context, content.substring(0, Math.min(100, content.length())));
            return content;
        } catch (Exception e) {
            log.error("Erro na chamada para DeepSeek: {}, usando fallback", e.getMessage());
            return getSmartDefaultResponse(prompt, context);
        }
    }
    
    private String getSmartDefaultResponse(String prompt, String context) {
        // Respostas inteligentes baseadas no contexto
        if (context.contains("Perfil")) {
            return "Com base nas suas respostas, identificamos características importantes do seu perfil de investidor. " +
                   "Suas preferências indicam uma abordagem equilibrada aos investimentos, buscando crescimento " +
                   "com segurança. Recomendamos diversificar entre renda fixa e variável conforme seu perfil.";
        } else if (context.contains("Recomendações")) {
            return "Baseado no seu perfil, sugerimos: 1) Tesouro Direto para segurança (30-40%), " +
                   "2) Fundos de Investimento para diversificação (30-40%), " +
                   "3) Ações de empresas sólidas para crescimento (20-30%). " +
                   "Revise periodicamente e ajuste conforme seus objetivos.";
        } else if (context.contains("Carteira")) {
            return "Sua carteira apresenta boa diversificação entre diferentes classes de ativos. " +
                   "Para otimizar, considere rebalancear periodicamente mantendo sua estratégia de longo prazo. " +
                   "Continue acompanhando os resultados e ajuste conforme necessário.";
        } else {
            return "Nossa análise indica que você está no caminho certo. Continue diversificando seus investimentos " +
                   "e mantenha o foco em seus objetivos de longo prazo. Se precisar de mais orientações, " +
                   "consulte um profissional especializado.";
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

    // Novos métodos para ChatBot personalizado
    public String responderComContextoPersonalizado(String pergunta, String contextoPersonalizado) {
        try {
            String prompt = buildContextualizedPrompt(pergunta, contextoPersonalizado);
            return callClaude(prompt, "Consulta contextualizada");
        } catch (Exception e) {
            log.error("Erro ao responder com contexto: {}", e.getMessage());
            return "Desculpe, não consegui processar sua pergunta com o contexto personalizado. Tente novamente.";
        }
    }
    
    public String responderConsultaComContexto(String pergunta, Usuario usuario, List<Investimento> investimentos, String contextoPersonalizado) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Nina, você é a assistente de investimentos da InvestIA. Responda esta consulta considerando todo o contexto:\n\n");
            prompt.append("PERGUNTA: ").append(pergunta).append("\n\n");
            prompt.append("CONTEXTO PERSONALIZADO:\n").append(contextoPersonalizado).append("\n\n");
            
            // Adicionar contexto da carteira atual
            if (!investimentos.isEmpty()) {
                BigDecimal valorTotal = calcularValorTotalCarteira(investimentos);
                prompt.append("CARTEIRA ATUAL:\n");
                prompt.append("Valor total: R$ ").append(valorTotal).append("\n");
                prompt.append("Ativos:\n");
                investimentos.forEach(inv -> 
                    prompt.append("- ").append(inv.getAtivo().getTicker())
                          .append(": ").append(inv.getQuantidade()).append(" cotas")
                          .append(", R$ ").append(inv.getValorTotalInvestido()).append("\n"));
                prompt.append("\n");
            }
            
            prompt.append("INSTRUÇÕES:\n");
            prompt.append("1. Seja específica e personalizada na resposta\n");
            prompt.append("2. Use o histórico e contexto fornecido\n");
            prompt.append("3. Forneça dicas práticas e educativas\n");
            prompt.append("4. Foque em investimentos brasileiros\n");
            prompt.append("5. Mantenha tom amigável e profissional\n\n");
            prompt.append("Responda de forma completa e útil:");
            
            return callClaude(prompt.toString(), "Consulta com contexto completo");
        } catch (Exception e) {
            log.error("Erro ao responder consulta com contexto: {}", e.getMessage());
            return responderConsulta(pergunta, usuario, investimentos);
        }
    }
    
    public String analisarCarteiraComHistorico(List<Investimento> investimentos, BigDecimal valorTotal, 
                                              Usuario usuario, List<com.InvestIA.entity.HistoricoConversa> analisesPrevias) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Nina, analise esta carteira considerando o histórico de análises anteriores:\n\n");
            
            prompt.append("CARTEIRA ATUAL:\n");
            prompt.append("Usuário: ").append(usuario.getNome()).append("\n");
            if (usuario.getPerfil() != null) {
                prompt.append("Perfil: ").append(usuario.getPerfil().getTipoPerfil().getNome()).append("\n");
                prompt.append("Tolerância ao risco: ").append(usuario.getPerfil().getToleranciaRisco()).append("/10\n");
            }
            prompt.append("Valor total: R$ ").append(valorTotal).append("\n");
            prompt.append("Número de ativos: ").append(investimentos.size()).append("\n\n");
            
            prompt.append("Composição atual:\n");
            investimentos.forEach(inv -> 
                prompt.append("- ").append(inv.getAtivo().getTicker())
                      .append(" (").append(inv.getAtivo().getTipoAtivo().getNome()).append(")")
                      .append(": ").append(inv.getQuantidade()).append(" cotas")
                      .append(", R$ ").append(inv.getValorTotalInvestido()).append("\n"));
            
            if (!analisesPrevias.isEmpty()) {
                prompt.append("\nANÁLISES ANTERIORES:\n");
                analisesPrevias.forEach(analise -> 
                    prompt.append("- ").append(analise.getCriadoEm().toLocalDate())
                          .append(": ").append(analise.getResposta().substring(0, Math.min(150, analise.getResposta().length())))
                          .append("...\n"));
            }
            
            prompt.append("\nFORNEÇA UMA ANÁLISE INCLUINDO:\n");
            prompt.append("1. Avaliação da diversificação e exposição ao risco\n");
            prompt.append("2. Comparação com análises anteriores (se disponível)\n");
            prompt.append("3. Evolução positiva ou pontos de atenção\n");
            prompt.append("4. Recomendações específicas de melhorias\n");
            prompt.append("5. Estratégia para próximos passos\n\n");
            prompt.append("Análise detalhada e personalizada:");
            
            return callClaude(prompt.toString(), "Análise de carteira com histórico");
        } catch (Exception e) {
            log.error("Erro na análise com histórico: {}", e.getMessage());
            return analisarCarteira(investimentos, valorTotal);
        }
    }
    
    public String analisarCarteiraComEvolucao(List<Investimento> investimentos, String contextoEvolucao) {
        try {
            String prompt = buildEvolutionAnalysisPrompt(investimentos, contextoEvolucao);
            return callClaude(prompt, "Análise com evolução");
        } catch (Exception e) {
            log.error("Erro na análise com evolução: {}", e.getMessage());
            BigDecimal valorTotal = calcularValorTotalCarteira(investimentos);
            return getDefaultPortfolioAnalysis(valorTotal);
        }
    }
    
    public String gerarRecomendacoesPersonalizadas(Usuario usuario, List<Investimento> investimentos, 
                                                   String padroesBehavior) {
        try {
            String prompt = buildPersonalizedRecommendationPrompt(usuario, investimentos, padroesBehavior);
            return callClaude(prompt, "Recomendações personalizadas");
        } catch (Exception e) {
            log.error("Erro nas recomendações personalizadas: {}", e.getMessage());
            return getDefaultRecommendations(usuario);
        }
    }
    
    private String buildContextualizedPrompt(String pergunta, String contextoPersonalizado) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Nina, você é a assistente de investimentos da InvestIA. Responda esta pergunta considerando o contexto completo do usuário:\n\n");
        prompt.append("PERGUNTA: ").append(pergunta).append("\n\n");
        prompt.append("CONTEXTO DO USUÁRIO:\n").append(contextoPersonalizado).append("\n\n");
        prompt.append("INSTRUÇÕES:\n");
        prompt.append("1. Use o contexto fornecido para personalizar a resposta\n");
        prompt.append("2. Seja específico e prático nas recomendações\n");
        prompt.append("3. Considere o histórico de interações do usuário\n");
        prompt.append("4. Use linguagem clara e educativa\n");
        prompt.append("5. Foque em investimentos brasileiros (B3, Tesouro, etc.)\n\n");
        prompt.append("Responda de forma personalizada e útil:");
        return prompt.toString();
    }
    
    private String buildEvolutionAnalysisPrompt(List<Investimento> investimentos, String contextoEvolucao) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Nina, analise esta carteira considerando sua evolução histórica:\n\n");
        
        BigDecimal valorTotal = calcularValorTotalCarteira(investimentos);
        prompt.append("CARTEIRA ATUAL:\n");
        prompt.append("Valor total: R$ ").append(valorTotal).append("\n");
        prompt.append("Número de ativos: ").append(investimentos.size()).append("\n\n");
        
        prompt.append("Composição:\n");
        investimentos.forEach(inv -> 
            prompt.append("- ").append(inv.getAtivo().getTicker())
                  .append(" (").append(inv.getAtivo().getTipoAtivo().getNome()).append(")")
                  .append(": ").append(inv.getQuantidade()).append(" cotas")
                  .append(", R$ ").append(inv.getValorTotalInvestido()).append("\n"));
        
        prompt.append("\nCONTEXTO DE EVOLUÇÃO:\n").append(contextoEvolucao).append("\n\n");
        
        prompt.append("ANÁLISE SOLICITADA:\n");
        prompt.append("1. Compare com análises anteriores (se disponível)\n");
        prompt.append("2. Identifique mudanças e tendências na carteira\n");
        prompt.append("3. Avalie diversificação e exposição ao risco\n");
        prompt.append("4. Sugira melhorias específicas\n");
        prompt.append("5. Comente sobre a evolução do investidor\n\n");
        prompt.append("Forneça uma análise detalhada e personalizada:");
        
        return prompt.toString();
    }
    
    private String buildPersonalizedRecommendationPrompt(Usuario usuario, List<Investimento> investimentos, 
                                                        String padroesBehavior) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Nina, gere recomendações personalizadas baseadas no comportamento do usuário:\n\n");
        
        prompt.append("PERFIL DO USUÁRIO:\n");
        prompt.append("Nome: ").append(usuario.getNome()).append("\n");
        if (usuario.getPerfil() != null) {
            prompt.append("Perfil de risco: ").append(usuario.getPerfil().getTipoPerfil().getNome()).append("\n");
            prompt.append("Experiência: ").append(usuario.getPerfil().getNivelExperiencia().getNome()).append("\n");
            prompt.append("Tolerância ao risco: ").append(usuario.getPerfil().getToleranciaRisco()).append("/10\n");
        }
        
        if (!investimentos.isEmpty()) {
            BigDecimal valorTotal = calcularValorTotalCarteira(investimentos);
            prompt.append("\nCARTEIRA ATUAL:\n");
            prompt.append("Valor total: R$ ").append(valorTotal).append("\n");
            prompt.append("Ativos na carteira:\n");
            investimentos.forEach(inv -> 
                prompt.append("- ").append(inv.getAtivo().getTicker())
                      .append(": R$ ").append(inv.getValorTotalInvestido()).append("\n"));
        } else {
            prompt.append("\nCARTEIRA: Vazia - usuário iniciante\n");
        }
        
        prompt.append("\nPADRÕES DE COMPORTAMENTO:\n").append(padroesBehavior).append("\n\n");
        
        prompt.append("RECOMENDAÇÕES SOLICITADAS:\n");
        prompt.append("1. 3-5 recomendações específicas de ativos brasileiros\n");
        prompt.append("2. Percentual sugerido para cada recomendação\n");
        prompt.append("3. Justificativa baseada no perfil e comportamento\n");
        prompt.append("4. Estratégia de implementação gradual\n");
        prompt.append("5. Considere os padrões de uso identificados\n\n");
        prompt.append("Gere recomendações personalizadas e práticas:");
        
        return prompt.toString();
    }
    
    private BigDecimal calcularValorTotalCarteira(List<Investimento> investimentos) {
        return investimentos.stream()
                .map(inv -> inv.getValorAtual().multiply(BigDecimal.valueOf(inv.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
            BigDecimal percentual = valor.divide(valorTotal, 4, java.math.RoundingMode.HALF_UP)
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