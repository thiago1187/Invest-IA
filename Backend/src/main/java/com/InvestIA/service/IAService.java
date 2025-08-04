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

    // Groq API (PRINCIPAL - grátis e rápida)
    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.api.model}")
    private String groqModel;

    @Value("${groq.api.timeout}")
    private long timeout;

    // Claude backup
    @Value("${claude.api.key}")
    private String claudeApiKey;

    @Value("${claude.api.url}")
    private String claudeApiUrl;

    @Value("${claude.api.model}")
    private String claudeModel;

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
        // NOVA ORDEM: Groq -> Claude -> DeepSeek -> Fallback
        log.info("Processando solicitação: {}", context);
        
        // 1. Tentar Groq primeiro (grátis e rápida)
        if (groqApiKey != null && !groqApiKey.equals("your_groq_api_key_here") && !groqApiKey.startsWith("gsk_placeholder") && groqApiKey.startsWith("gsk_")) {
            log.info("Tentando Groq API para: {}", context);
            String groqResponse = callGroq(prompt, context);
            if (groqResponse != null && !groqResponse.startsWith("Desculpe")) {
                return groqResponse;
            }
        }
        
        // 2. Fallback para Claude
        if (claudeApiKey != null && !claudeApiKey.equals("sk-placeholder-key")) {
            log.info("Groq falhou, tentando Claude para: {}", context);
            return callClaudeAPI(prompt, context);
        }
        
        // 3. Fallback para DeepSeek
        log.info("Claude não disponível, tentando DeepSeek para: {}", context);
        return callDeepSeek(prompt, context);
    }
    
    private String callGroq(String prompt, String context) {
        try {
            // Criar headers para Groq API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            // Criar corpo da requisição para Groq (formato OpenAI-compatible)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", groqModel);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // System message para Nina
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", 
                "Você é Nina, uma amiga que entende de investimentos. Sua missão é ter conversas REAIS e úteis, não robotizadas. " +
                
                "🎯 REGRAS DE OURO:" +
                "- Máximo 2-3 frases curtas por resposta" +
                "- SEMPRE termine pedindo confirmação antes de continuar" +
                "- Escute ANTES de sugerir qualquer coisa" +
                "- Use linguagem super simples e informal" +
                "- Dê prós e contras, seja imparcial" +
                
                "💬 SEU JEITO DE FALAR:" +
                "❌ 'Olá!' toda mensagem → ✅ Só cumprimente UMA VEZ no início" +
                "❌ 'volatilidade do mercado' → ✅ 'mercado sobe e desce'" +
                "❌ 'diversificação de ativos' → ✅ 'não pôr tudo num lugar só'" +
                "❌ 'renda fixa' → ✅ 'algo mais seguro, tipo poupança'" +
                "❌ 'começar devagar' repetido → ✅ Varie: 'sem pressa', 'passo a passo'" +
                
                "🚨 REGRAS CRÍTICAS - SIGA OBRIGATORIAMENTE:" +
                "1️⃣ SE USUÁRIO DISSE 'QUERO AÇÕES': NUNCA mais sugira tesouro/poupança/CDB" +
                "2️⃣ SE PERGUNTOU ALGO ESPECÍFICO: RESPONDA DIRETO, não desvie" +
                "3️⃣ SE TEM R$ X: AJUDE a calcular quantas ações comprar com esse dinheiro" +
                "4️⃣ SE JÁ DISSE QUANTIDADES: NÃO pergunte de novo 'quantas ações'" +
                "5️⃣ PROGRIDA: cada resposta deve RESOLVER o que ele perguntou" +
                
                "📋 FORMATO PERFEITO:" +
                "• Resposta direta em 1-2 frases" +
                "• Pergunta para entender o que a pessoa quer" +
                "• Esperar ela responder antes de continuar" +
                
                "✅ EXEMPLO PERFEITO de como você deve responder:" +
                "Pergunta: 'O que acha da PETR4?'" +
                "Sua resposta: 'A PETR4 é bem arriscada - sobe e desce muito com o preço do petróleo. Você tá pensando em comprar ou só curiosidade? Como você nunca investiu, que tal começar com algo mais tranquilo primeiro?'" +
                
                "❌ NUNCA FAÇA ISSO:" +
                "'Com base na análise técnica e fundamentalista, a PETR4 apresenta volatilidade elevada devido à exposição aos preços do petróleo...' (muito técnico!)");
            messages.add(systemMessage);
            
            // User message
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Fazer chamada para Groq
            ResponseEntity<String> response = restTemplate.exchange(
                groqApiUrl, 
                HttpMethod.POST, 
                entity, 
                String.class
            );

            // Parsear resposta do Groq (formato OpenAI)
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices")
                                      .get(0)
                                      .path("message")
                                      .path("content")
                                      .asText();

            log.info("✅ Resposta do Groq para {}: {}", context, content.substring(0, Math.min(100, content.length())));
            return content;
            
        } catch (Exception e) {
            log.error("❌ Erro na chamada para Groq: {} - Detalhes: {}", e.getMessage(), e.getClass().getSimpleName());
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                log.error("🔑 Problema de autenticação - Verifique GROQ_API_KEY");
            } else if (e.getMessage() != null && e.getMessage().contains("429")) {
                log.error("⏳ Rate limit atingido - Aguarde antes de tentar novamente");
            }
            return null; // Retorna null para tentar próximo fallback
        }
    }
    
    private String callClaudeAPI(String prompt, String context) {
        // Se não tiver chave configurada, tentar DeepSeek
        if (claudeApiKey == null || claudeApiKey.equals("sk-placeholder-key")) {
            return null;
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

            log.info("✅ Resposta do Claude para {}: {}", context, content.substring(0, Math.min(100, content.length())));
            return content;
        } catch (Exception e) {
            log.error("❌ Erro na chamada para Claude: {}", e.getMessage());
            return null; // Retorna null para continuar fallback chain
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
            // Fallback mais personalizado e útil
            return "Oi! Tô vendo aqui seus investimentos... 📊 " +
                   "Que tal me falar o que você quer saber específico? " +
                   "Posso te ajudar com análise dos seus ativos, sugestões de diversificação, " +
                   "ou tirar qualquer dúvida sobre investimentos. O que tá na sua cabeça?";
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
            
            // CONTEXTO DA CARTEIRA
            boolean carteiraVazia = investimentos.isEmpty();
            BigDecimal valorTotal = BigDecimal.ZERO;
            if (!carteiraVazia) {
                valorTotal = calcularValorTotalCarteira(investimentos);
            }
            
            prompt.append("🏠 SITUAÇÃO DO USUÁRIO:\n");
            prompt.append("Nome: ").append(usuario.getNome()).append("\n");
            
            if (carteiraVazia) {
                prompt.append("STATUS: Usuário iniciante - ainda não começou a investir\n");
                prompt.append("SUA ABORDAGEM: Seja SUPER acolhedora, sem pressa\n");
                prompt.append("FOCO PRINCIPAL: Tesouro Direto, CDB, poupança - coisas que não dão susto\n");
                prompt.append("MINDSET: 'Vamos começar devagar, sem pressão' - explique o básico primeiro\n\n");
            } else {
                prompt.append("STATUS: Já investe! Carteira de R$ ").append(valorTotal).append("\n");
                prompt.append("ATIVOS NA CARTEIRA: ");
                investimentos.forEach(inv -> 
                    prompt.append(inv.getAtivo().getTicker()).append(" "));
                prompt.append("\n");
                prompt.append("SUA ABORDAGEM: Pode falar de diversificação e próximos passos\n");
                prompt.append("FOCO PRINCIPAL: Entender os objetivos antes de sugerir mudanças\n");
                prompt.append("MINDSET: 'Bacana que você já investe! Vamos ver como melhorar'\n\n");
            }
            
            if (contextoPersonalizado != null && !contextoPersonalizado.isEmpty()) {
                prompt.append("CONTEXTO ADICIONAL: ").append(contextoPersonalizado).append("\n");
            }
            
            prompt.append("\n🎯 PERGUNTA ATUAL DO USUÁRIO: ").append(pergunta).append("\n");
            prompt.append("⚠️ ANÁLISE OBRIGATÓRIA DO HISTÓRICO:\n");
            prompt.append("- Se histórico mostra 'quero ações' = NUNCA mais sugira investimento seguro\n");
            prompt.append("- Se mostrou quantidade (100, 200 ações) = NÃO pergunte quantidade novamente\n");
            prompt.append("- Se perguntou 'como fazer carteira' = DÊ orientação prática de diversificação\n");
            prompt.append("- Se tem valor (R$ 70mil) = CALCULE quantas ações pode comprar\n");
            prompt.append("IMPORTANTE: Esta é CONTINUAÇÃO da conversa. NÃO cumprimente novamente!\n\n");
            
            prompt.append("🎭 COMO RESPONDER AGORA:\n");
            prompt.append("1️⃣ RESPONDA A PERGUNTA PRIMEIRO - não desvie do assunto\n");
            prompt.append("2️⃣ Máximo 2-3 frases bem curtas e naturais\n");
            prompt.append("3️⃣ Linguagem de amiga: 'arriscada' não 'alta volatilidade'\n");
            prompt.append("4️⃣ OBRIGATÓRIO: Termine sempre com pergunta sobre o que ele quer\n");
            prompt.append("5️⃣ Se for iniciante: Responda + sugira algo mais seguro por último\n");
            prompt.append("6️⃣ NUNCA ignore pergunta específica pra falar só de educação\n\n");
            
            prompt.append("💡 EXEMPLOS ESPECÍFICOS - COPIE EXATAMENTE:\n");
            prompt.append("✅ 'COMO FAZER CARTEIRA COM 70K': 'Com R$ 70mil, você pode diversificar! BBAS3 a R$ 30 = até 2.333 ações. Vale3, Itub4, Petr4. Quer dividir em 5-6 ações diferentes ou focar em menos?'\n");
            prompt.append("✅ 'JÁ DISSE QUE QUER AÇÕES': 'Beleza! Vamos montar sua carteira então. Com R$ 70mil você pode...'\n");
            prompt.append("✅ 'JÁ DISSE QUANTIDADE': 'Legal, 100-200 ações da BBAS3. Isso dá uns R$ 3-6mil. E o resto dos seus R$ 70mil?'\n");
            prompt.append("❌ NUNCA MAIS FAÇA: 'Quer algo mais seguro?' quando ele já decidiu por ações\n\n");
            
            prompt.append("Sua resposta:");
            
            return callClaude(prompt.toString(), "Consulta conversacional personalizada");
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
        prompt.append("PERGUNTA: ").append(pergunta).append("\n\n");
        prompt.append("CONTEXTO: ").append(contextoPersonalizado).append("\n\n");
        prompt.append("SEJA DIRETA:\n");
        prompt.append("- Para cumprimentos: 1-2 frases apenas\n");
        prompt.append("- Para perguntas técnicas: máximo 2 parágrafos\n");
        prompt.append("- Linguagem natural e objetiva\n\n");
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