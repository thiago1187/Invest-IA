package com.InvestIA.enums;

import lombok.Getter;

@Getter
public enum SetorAtivo {
    TECNOLOGIA("Tecnologia"),
    FINANCEIRO("Financeiro"),
    VAREJO("Varejo"),
    ENERGIA("Energia"),
    SAUDE("Saúde"),
    CONSUMO("Consumo"),
    INDUSTRIAL("Industrial"),
    IMOBILIARIO("Imobiliário"),
    TELECOMUNICACOES("Telecomunicações"),
    UTILIDADE_PUBLICA("Utilidade Pública"),
    MATERIAIS_BASICOS("Materiais Básicos"),
    PETROLEO_GAS("Petróleo e Gás"),
    AGRONEGOCIO("Agronegócio"),
    OUTRO("Outro");
    
    private final String nome;
    
    SetorAtivo(String nome) {
        this.nome = nome;
    }
}