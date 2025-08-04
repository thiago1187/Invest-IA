package com.InvestIA.entity;

import jakarta.persistence.*;
import lombok.*;
import com.InvestIA.enums.TipoAtivo;
import com.InvestIA.enums.SetorAtivo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ativos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ativo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String ticker;
    
    @Column(nullable = false, unique = true)
    private String simbolo;
    
    @Column(nullable = false)
    private String nome;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtivo tipoAtivo;
    
    @Enumerated(EnumType.STRING)
    private SetorAtivo setor;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal precoAtual;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal variacaoDiaria;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal variacaoMensal;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal variacaoAnual;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    private Double risco; // 0.0 a 1.0
    
    @Builder.Default
    private boolean status = true;
    
    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;
    
    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}