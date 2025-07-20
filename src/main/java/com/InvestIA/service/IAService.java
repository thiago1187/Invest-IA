package com.InvestIA.service;

import com.InvestIA.enums.NivelExperiencia;
import com.InvestIA.enums.TipoPerfil;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IAService {
    
    public String analisarPerfil(TipoPerfil tipoPerfil, NivelExperiencia nivelExp, Map<Integer, String> respostas) {
        // Implementação básica - pode ser expandida com integração de IA real
        return String.format("Análise do perfil %s com nível de experiência %s baseada nas respostas fornecidas.", 
                tipoPerfil.getNome(), nivelExp.getNome());
    }
}