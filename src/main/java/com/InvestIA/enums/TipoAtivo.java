package com.InvestIA.enums;

import lombok.Getter;

@Getter
public enum TipoAtivo {
    ACAO("Ação", "ACAO"),
    FII("Fundo Imobiliário", "FII"),
    ETF("ETF", "ETF"),
    BDR("BDR", "BDR"),
    TESOURO_DIRETO("Tesouro Direto", "RENDA_FIXA"),
    CDB("CDB", "RENDA_FIXA"),
    LCI_LCA("LCI/LCA", "RENDA_FIXA"),
    DEBENTURE("Debênture", "RENDA_FIXA"),
    CRIPTO("Criptomoeda", "CRIPTO"),
    FUNDO_MULTIMERCADO("Fundo Multimercado", "FUNDO"),
    FUNDO_RENDA_FIXA("Fundo Renda Fixa", "FUNDO");
    
    private final String nome;
    private final String categoria;
    
    TipoAtivo(String nome, String categoria) {
        this.nome = nome;
        this.categoria = categoria;
    }
}