package com.InvestIA.dto.simulado;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimuladoRespostasRequest {
    @NotNull(message = "Respostas são obrigatórias")
    private Map<Integer, String> respostas; // perguntaId -> opcaoId
}