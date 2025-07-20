package com.InvestIA.enums;

import lombok.Getter;

@Getter
public enum NivelExperiencia {
    INICIANTE("Iniciante", 0),
    INTERMEDIARIO("Intermediário", 1),
    AVANCADO("Avançado", 2),
    EXPERT("Expert", 3);
    
    private final String nome;
    private final int nivel;
    
    NivelExperiencia(String nome, int nivel) {
        this.nome = nome;
        this.nivel = nivel;
    }
}