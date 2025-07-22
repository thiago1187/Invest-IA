package com.InvestIA.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_conversas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoConversa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String pergunta;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String resposta;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConversa tipo;
    
    @Column(name = "contexto_carteira", columnDefinition = "TEXT")
    private String contextoCarteira; // JSON com snapshot da carteira no momento
    
    @Column(name = "avaliacao_usuario")
    private Integer avaliacaoUsuario; // 1-5 para feedback
    
    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
    
    @Column(name = "tempo_resposta_ms")
    private Long tempoRespostaMs;
    
    public enum TipoConversa {
        PERGUNTA_GERAL,
        ANALISE_CARTEIRA,
        RECOMENDACOES,
        EDUCACIONAL,
        SUPORTE
    }
}