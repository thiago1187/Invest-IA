package com.InvestIA.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_precos", indexes = {
    @Index(name = "idx_historico_ativo_data", columnList = "ativo_id, data"),
    @Index(name = "idx_historico_data", columnList = "data")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoPreco {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;
    
    @Column(name = "data", nullable = false)
    private LocalDate data;
    
    @Column(name = "preco_abertura", precision = 15, scale = 2)
    private BigDecimal precoAbertura;
    
    @Column(name = "preco_fechamento", precision = 15, scale = 2, nullable = false)
    private BigDecimal precoFechamento;
    
    @Column(name = "preco_maximo", precision = 15, scale = 2)
    private BigDecimal precoMaximo;
    
    @Column(name = "preco_minimo", precision = 15, scale = 2)
    private BigDecimal precoMinimo;
    
    @Column(name = "volume")
    private Long volume;
    
    @Column(name = "variacao_percentual", precision = 10, scale = 4)
    private BigDecimal variacaoPercentual;
    
    @Column(name = "criado_em", nullable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
    
    @Column(name = "atualizado_em", nullable = false)
    @Builder.Default
    private LocalDateTime atualizadoEm = LocalDateTime.now();
    
    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}