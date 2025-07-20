package com.InvestIA.enums;

import lombok.Getter;

@Getter
public enum TipoPerfil {
    CONSERVADOR("Conservador", "Prioriza segurança e preservação de capital"),
    MODERADO("Moderado", "Busca equilíbrio entre segurança e rentabilidade"),
    AGRESSIVO("Agressivo", "Aceita maiores riscos em busca de maiores retornos");
    
    private final String nome;
    private final String descricao;
    
    TipoPerfil(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
}