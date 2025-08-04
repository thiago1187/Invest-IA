package com.InvestIA.controller;

import com.InvestIA.dto.chat.ChatRequest;
import com.InvestIA.dto.chat.ChatResponse;
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

import java.time.LocalDateTime;
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

    // Endpoint público com contexto realista (dados do usuário padrão) - COM HISTÓRICO
    @PostMapping("/teste")
    public ResponseEntity<ChatResponse> testarNina(@RequestBody ChatRequest request, Authentication authentication) {
        try {
            Usuario usuario;
            
            // Se há autenticação, usar o usuário autenticado
            if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                usuario = usuarioRepository.findByEmail(authentication.getName())
                        .orElse(null);
                
                if (usuario == null) {
                    return ResponseEntity.ok(ChatResponse.builder()
                            .resposta("Usuário autenticado não encontrado. Faça login novamente.")
                            .success(false)
                            .timestamp(LocalDateTime.now())
                            .build());
                }
            } else {
                // Fallback para usuário de teste se não autenticado
                usuario = usuarioRepository.findByEmail("teste@investia.com")
                        .orElse(null);
                
                if (usuario == null) {
                    return ResponseEntity.ok(ChatResponse.builder()
                            .resposta("Usuário de teste não encontrado. Configure primeiro os dados de teste.")
                            .success(false)
                            .timestamp(LocalDateTime.now())
                            .build());
                }
            }
            
            // Buscar investimentos reais do usuário
            List<Investimento> investimentos = investimentoRepository
                    .findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
            
            // USAR O SERVIÇO QUE TEM CONTEXTO CONVERSACIONAL!
            String resposta = chatBotService.responderComContexto(
                    request.getMensagem(),
                    usuario,
                    investimentos
            );
            
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta(resposta)
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Desculpe, estou com problemas técnicos no momento. Tente novamente em alguns instantes.")
                    .success(false)
                    .timestamp(LocalDateTime.now())
                    .erro(e.getMessage())
                    .build());
        }
    }
    
    // Endpoint para testar com perfil específico
    @PostMapping("/teste-perfil/{tipoPerfil}")
    public ResponseEntity<ChatResponse> testarNinaPerfil(
            @PathVariable String tipoPerfil,
            @RequestBody ChatRequest request) {
        try {
            // Criar usuário fictício com perfil específico para teste
            Usuario usuarioTeste = new Usuario();
            usuarioTeste.setNome("Usuário Teste");
            usuarioTeste.setEmail("teste-perfil@investia.com");
            
            // Criar perfil baseado no parâmetro
            com.InvestIA.entity.Perfil perfil = new com.InvestIA.entity.Perfil();
            
            switch (tipoPerfil.toLowerCase()) {
                case "iniciante":
                    perfil.setTipoPerfil(com.InvestIA.enums.TipoPerfil.CONSERVADOR);
                    perfil.setNivelExperiencia(com.InvestIA.enums.NivelExperiencia.INICIANTE);
                    perfil.setToleranciaRisco(0.2); // 20% tolerância ao risco
                    break;
                case "moderado":
                    perfil.setTipoPerfil(com.InvestIA.enums.TipoPerfil.MODERADO);
                    perfil.setNivelExperiencia(com.InvestIA.enums.NivelExperiencia.INTERMEDIARIO);
                    perfil.setToleranciaRisco(0.5); // 50% tolerância ao risco
                    break;
                case "agressivo":
                    perfil.setTipoPerfil(com.InvestIA.enums.TipoPerfil.AGRESSIVO);
                    perfil.setNivelExperiencia(com.InvestIA.enums.NivelExperiencia.AVANCADO);
                    perfil.setToleranciaRisco(0.8); // 80% tolerância ao risco
                    break;
                default:
                    perfil.setTipoPerfil(com.InvestIA.enums.TipoPerfil.CONSERVADOR);
                    perfil.setNivelExperiencia(com.InvestIA.enums.NivelExperiencia.INICIANTE);
                    perfil.setToleranciaRisco(0.3); // 30% tolerância ao risco
            }
            
            usuarioTeste.setPerfil(perfil);
            
            // Lista vazia de investimentos para teste
            List<Investimento> investimentos = List.of();
            
            // USAR O SERVIÇO QUE TEM CONTEXTO CONVERSACIONAL!
            String resposta = chatBotService.responderComContexto(
                    request.getMensagem() + " (PERFIL TESTE: " + tipoPerfil.toUpperCase() + ")",
                    usuarioTeste,
                    investimentos
            );
            
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta(resposta)
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Erro no teste de perfil: " + e.getMessage())
                    .success(false)
                    .timestamp(LocalDateTime.now())
                    .erro(e.getMessage())
                    .build());
        }
    }

    // Endpoint principal do chat
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            Authentication authentication) {
        
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            List<Investimento> investimentos = investimentoRepository
                    .findByUsuarioIdAndAtivoStatusTrue(usuario.getId());
            
            String resposta = chatBotService.responderComContexto(
                    request.getMensagem(), 
                    usuario, 
                    investimentos
            );
            
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta(resposta)
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Desculpe, não consegui processar sua mensagem no momento. " +
                             "Tente reformular ou entre em contato com nosso suporte.")
                    .success(false)
                    .erro(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

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
                    request.getMensagem(), 
                    usuario, 
                    investimentos
            );
            
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta(resposta)
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Desculpe, não consegui processar sua pergunta no momento. " +
                             "Tente reformular ou entre em contato com nosso suporte.")
                    .success(false)
                    .timestamp(LocalDateTime.now())
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
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Não foi possível analisar sua carteira no momento. Tente novamente.")
                    .success(false)
                    .erro(e.getMessage())
                    .timestamp(LocalDateTime.now())
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
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .resposta("Não foi possível gerar recomendações no momento. Tente novamente.")
                    .success(false)
                    .erro(e.getMessage())
                    .timestamp(LocalDateTime.now())
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