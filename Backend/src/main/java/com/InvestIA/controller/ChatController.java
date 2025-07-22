package com.InvestIA.controller;

import com.InvestIA.entity.HistoricoConversa;
import com.InvestIA.entity.Investimento;
import com.InvestIA.entity.Usuario;
import com.InvestIA.repository.InvestimentoRepository;
import com.InvestIA.repository.UsuarioRepository;
import com.InvestIA.service.ChatBotPersonalizadoService;
import com.InvestIA.service.IAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatBotPersonalizadoService chatBotService;
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
            
            String resposta = chatBotService.responderComContexto(
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
            
            String analise = chatBotService.analisarCarteiraPersonalizada(usuario, investimentos);
            
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
            
            String recomendacoes = chatBotService.gerarRecomendacoesPersonalizadas(usuario, investimentos);
            
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

    @GetMapping("/historico")
    public ResponseEntity<List<HistoricoConversa>> obterHistorico(
            @RequestParam(defaultValue = "10") int limite,
            Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            List<HistoricoConversa> historico = chatBotService.obterHistoricoUsuario(usuario, limite);
            
            return ResponseEntity.ok(historico);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/avaliar/{conversaId}")
    public ResponseEntity<Void> avaliarResposta(
            @PathVariable UUID conversaId,
            @RequestBody AvaliacaoRequest request) {
        try {
            chatBotService.avaliarResposta(conversaId, request.getAvaliacao());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasChat> obterEstatisticas(Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Implementar lógica de estatísticas se necessário
            EstatisticasChat stats = EstatisticasChat.builder()
                    .totalConversas(0)
                    .mediaAvaliacao(0.0)
                    .tiposMaisUsados(List.of())
                    .build();
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
    
    public static class AvaliacaoRequest {
        private Integer avaliacao;
        
        public Integer getAvaliacao() { return avaliacao; }
        public void setAvaliacao(Integer avaliacao) { this.avaliacao = avaliacao; }
    }
    
    public static class EstatisticasChat {
        private Integer totalConversas;
        private Double mediaAvaliacao;
        private List<String> tiposMaisUsados;
        
        public static EstatisticasChatBuilder builder() {
            return new EstatisticasChatBuilder();
        }
        
        public static class EstatisticasChatBuilder {
            private Integer totalConversas;
            private Double mediaAvaliacao;
            private List<String> tiposMaisUsados;
            
            public EstatisticasChatBuilder totalConversas(Integer totalConversas) {
                this.totalConversas = totalConversas;
                return this;
            }
            
            public EstatisticasChatBuilder mediaAvaliacao(Double mediaAvaliacao) {
                this.mediaAvaliacao = mediaAvaliacao;
                return this;
            }
            
            public EstatisticasChatBuilder tiposMaisUsados(List<String> tiposMaisUsados) {
                this.tiposMaisUsados = tiposMaisUsados;
                return this;
            }
            
            public EstatisticasChat build() {
                EstatisticasChat stats = new EstatisticasChat();
                stats.totalConversas = this.totalConversas;
                stats.mediaAvaliacao = this.mediaAvaliacao;
                stats.tiposMaisUsados = this.tiposMaisUsados;
                return stats;
            }
        }
        
        public Integer getTotalConversas() { return totalConversas; }
        public Double getMediaAvaliacao() { return mediaAvaliacao; }
        public List<String> getTiposMaisUsados() { return tiposMaisUsados; }
    }
}