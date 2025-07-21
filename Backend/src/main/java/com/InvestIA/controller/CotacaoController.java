package com.InvestIA.controller;

import com.InvestIA.service.FinanceAPIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cotacao")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Cotações", description = "Cotações em tempo real via Yahoo Finance")
public class CotacaoController {
    
    private final FinanceAPIService financeAPIService;
    
    @GetMapping("/teste/petr4")
    @Operation(summary = "Testar cotação PETR4.SA")
    public ResponseEntity<Map<String, Object>> testarPETR4() {
        try {
            Optional<FinanceAPIService.StockInfo> stockInfo = financeAPIService.getStockInfo("PETR4.SA");
            Optional<BigDecimal> currentPrice = financeAPIService.getCurrentPrice("PETR4.SA");
            
            return ResponseEntity.ok(Map.of(
                "symbol", "PETR4.SA",
                "stockInfo", stockInfo.orElse(null),
                "currentPrice", currentPrice.orElse(null),
                "status", stockInfo.isPresent() ? "SUCCESS" : "NO_DATA",
                "message", stockInfo.isPresent() ? "Dados obtidos com sucesso!" : "Não foi possível obter dados"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "symbol", "PETR4.SA",
                "error", e.getMessage(),
                "status", "ERROR"
            ));
        }
    }
    
    @GetMapping("/{symbol}")
    @Operation(summary = "Obter cotação atual de um ativo")
    public ResponseEntity<Map<String, Object>> obterCotacao(@PathVariable String symbol) {
        try {
            Optional<FinanceAPIService.StockInfo> stockInfo = financeAPIService.getStockInfo(symbol);
            
            if (stockInfo.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "symbol", symbol,
                    "data", stockInfo.get(),
                    "status", "SUCCESS"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "symbol", symbol,
                    "message", "Ativo não encontrado ou sem dados disponíveis",
                    "status", "NO_DATA"
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "symbol", symbol,
                "error", e.getMessage(),
                "status", "ERROR"
            ));
        }
    }
    
    @GetMapping("/teste/multi")
    @Operation(summary = "Testar múltiplos ativos brasileiros e internacionais")
    public ResponseEntity<Map<String, Object>> testarMultiplosAtivos() {
        String[] symbols = {"PETR4.SA", "VALE3.SA", "BBAS3.SA", "AAPL", "TSLA"};
        Map<String, Object> results = new java.util.HashMap<>();
        
        for (String symbol : symbols) {
            try {
                Optional<FinanceAPIService.StockInfo> stockInfo = financeAPIService.getStockInfo(symbol);
                results.put(symbol, Map.of(
                    "success", stockInfo.isPresent(),
                    "data", stockInfo.orElse(null)
                ));
            } catch (Exception e) {
                results.put(symbol, Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "results", results,
            "status", "COMPLETED"
        ));
    }
}