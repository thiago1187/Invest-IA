package com.InvestIA.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String resposta;
    private String conversaId;
    private LocalDateTime timestamp;
    private boolean success;
    private String erro;
}