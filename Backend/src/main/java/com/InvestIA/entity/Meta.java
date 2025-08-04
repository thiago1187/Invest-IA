package com.InvestIA.entity;

import jakarta.persistence.*;
import lombok.*;
import com.InvestIA.enums.StatusMeta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "metas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(nullable = false)
    private String nome;
    
    private String descricao;
    
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal valorObjetivo;
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorAtual = BigDecimal.ZERO;
    
    private LocalDate prazo;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusMeta status = StatusMeta.EM_ANDAMENTO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal aporteMensalSugerido;
    
    @Column(columnDefinition = "TEXT")
    private String estrategiaSugerida; // JSON com sugestões da IA
    
    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
    
    @Column(name = "atualizado_em")
    @Builder.Default
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}