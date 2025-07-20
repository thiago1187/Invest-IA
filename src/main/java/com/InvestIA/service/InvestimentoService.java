package com.InvestIA.service;

import com.InvestIA.dto.investimento.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InvestimentoService {
    
    public Page<InvestimentoResponse> listarPorUsuario(UUID usuarioId, Pageable pageable) {
        // TODO: Implementar lógica
        throw new RuntimeException("Não implementado ainda");
    }
    
    public InvestimentoResponse criar(UUID usuarioId, CriarInvestimentoRequest request) {
        // TODO: Implementar lógica
        throw new RuntimeException("Não implementado ainda");
    }
    
    public InvestimentoResponse atualizar(UUID usuarioId, UUID investimentoId, AtualizarInvestimentoRequest request) {
        // TODO: Implementar lógica
        throw new RuntimeException("Não implementado ainda");
    }
    
    public void remover(UUID usuarioId, UUID investimentoId) {
        // TODO: Implementar lógica
        throw new RuntimeException("Não implementado ainda");
    }
}