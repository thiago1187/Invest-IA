package com.InvestIA.entity;

import jakarta.persistence.*;
import lombok.*;
import com.InvestIA.enums.TipoPerfil;
import com.InvestIA.enums.NivelExperiencia;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "perfis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfil {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPerfil tipoPerfil;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelExperiencia nivelExperiencia;
    
    private Integer pontuacaoSimulado;
    
    @Column(columnDefinition = "TEXT")
    private String respostasSimulado; // JSON das respostas
    
    private Double toleranciaRisco; // 0.0 a 1.0
    
    private Integer horizonteInvestimento; // em meses
    
    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
    
    @Column(name = "atualizado_em")
    @Builder.Default
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}