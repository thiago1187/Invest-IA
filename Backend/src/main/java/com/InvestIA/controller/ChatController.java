package com.InvestIA.controller;

import com.InvestIA.entity.Investimento;
import com.InvestIA.entity.Usuario;
import com.InvestIA.repository.InvestimentoRepository;
import com.InvestIA.repository.UsuarioRepository;
import com.InvestIA.service.IAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IAService iaService;
    private final UsuarioRepository usuarioRepository;
    private final InvestimentoRepository investimentoRepository;

    @PostMapping("/pergunta")
    public ResponseEntity<ChatResponse> fazerPergunta(
            @RequestBody ChatRequest request,
            Authentication authentication) {
        
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            List<Investimento> investimentos = investimentoRepository
                    .findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
            
            String resposta = iaService.responderConsulta(
                    request.getPergunta(), 
                    usuario, 
                    investimentos
            );
            
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta(resposta)
                    .sucesso(true)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Desculpe, não consegui processar sua pergunta no momento. " +
                             "Tente reformular ou entre em contato com nosso suporte.")
                    .sucesso(false)
                    .erro(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/analise-carteira")
    public ResponseEntity<ChatResponse> analisarCarteira(Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            List<Investimento> investimentos = investimentoRepository
                    .findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
            
            if (investimentos.isEmpty()) {
                return ResponseEntity.ok(ChatResponse.builder()
                        .resposta("Você ainda não possui investimentos em sua carteira. " +
                                 "Que tal começar com algumas recomendações baseadas no seu perfil?")
                        .sucesso(true)
                        .build());
            }
            
            java.math.BigDecimal valorTotal = investimentos.stream()
                    .map(inv -> inv.getValorAtual().multiply(java.math.BigDecimal.valueOf(inv.getQuantidade())))
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            String analise = iaService.analisarCarteira(investimentos, valorTotal);
            
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta(analise)
                    .sucesso(true)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Não foi possível analisar sua carteira no momento. Tente novamente.")
                    .sucesso(false)
                    .erro(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/recomendacoes")
    public ResponseEntity<ChatResponse> obterRecomendacoes(Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            List<Investimento> investimentos = investimentoRepository
                    .findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
            
            // Lista vazia para ativos disponíveis - em produção, buscar do repositório
            List<com.InvestIA.entity.Ativo> ativosDisponiveis = List.of();
            
            String recomendacoes = iaService.gerarRecomendacoes(usuario, investimentos, ativosDisponiveis);
            
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta(recomendacoes)
                    .sucesso(true)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Não foi possível gerar recomendações no momento. Tente novamente.")
                    .sucesso(false)
                    .erro(e.getMessage())
                    .build());
        }
    }

    public static class ChatRequest {
        private String pergunta;
        
        public String getPergunta() { return pergunta; }
        public void setPergunta(String pergunta) { this.pergunta = pergunta; }
    }

    public static class ChatResponse {
        private String resposta;
        private boolean sucesso;
        private String erro;
        
        public static ChatResponseBuilder builder() {
            return new ChatResponseBuilder();
        }
        
        public static class ChatResponseBuilder {
            private String resposta;
            private boolean sucesso;
            private String erro;
            
            public ChatResponseBuilder resposta(String resposta) {
                this.resposta = resposta;
                return this;
            }
            
            public ChatResponseBuilder sucesso(boolean sucesso) {
                this.sucesso = sucesso;
                return this;
            }
            
            public ChatResponseBuilder erro(String erro) {
                this.erro = erro;
                return this;
            }
            
            public ChatResponse build() {
                ChatResponse response = new ChatResponse();
                response.resposta = this.resposta;
                response.sucesso = this.sucesso;
                response.erro = this.erro;
                return response;
            }
        }
        
        public String getResposta() { return resposta; }
        public boolean isSucesso() { return sucesso; }
        public String getErro() { return erro; }
    }
}