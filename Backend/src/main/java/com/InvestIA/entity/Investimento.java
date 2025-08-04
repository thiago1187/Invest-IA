package com.InvestIA.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "investimentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investimento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;
    
    private Integer quantidade;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal valorMedioCompra;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal valorAtual;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal valorTotalInvestido;
    
    private LocalDate dataCompra;
    
    @Builder.Default
    private boolean ativoStatus = true;
    
    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
    
    @Column(name = "atualizado_em")
    @Builder.Default
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}