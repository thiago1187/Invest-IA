package com.InvestIA.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceAPIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${yahoo.finance.api.url}")
    private String yahooFinanceUrl;

    @Value("${yahoo.finance.api.timeout}")
    private long timeout;

    @Cacheable(value = "stockPrices", key = "#symbol")
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        // Sempre usar dados simulados para evitar problemas com Yahoo Finance
        log.info("Usando dados simulados para {} (Yahoo Finance desabilitado)", symbol);
        return Optional.of(getMockPrice(symbol));
    }

    private Optional<BigDecimal> parseCurrentPrice(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode chart = root.path("chart");
            
            if (chart.isArray() && chart.size() > 0) {
                JsonNode result = chart.get(0).path("result");
                
                if (result.isArray() && result.size() > 0) {
                    JsonNode meta = result.get(0).path("meta");
                    JsonNode regularMarketPrice = meta.path("regularMarketPrice");
                    
                    if (!regularMarketPrice.isMissingNode()) {
                        return Optional.of(BigDecimal.valueOf(regularMarketPrice.asDouble()));
                    }
                }
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao processar resposta da API: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Cacheable(value = "stockInfo", key = "#symbol")
    public Optional<StockInfo> getStockInfo(String symbol) {
        // Sempre usar dados simulados para evitar problemas com Yahoo Finance
        log.info("Usando dados simulados para {} (Yahoo Finance desabilitado)", symbol);
        return Optional.of(getMockStockInfo(symbol));
    }

    private Optional<StockInfo> parseStockInfo(String jsonResponse, String symbol) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode chart = root.path("chart");
            
            if (chart.isArray() && chart.size() > 0) {
                JsonNode result = chart.get(0).path("result");
                
                if (result.isArray() && result.size() > 0) {
                    JsonNode meta = result.get(0).path("meta");
                    
                    BigDecimal currentPrice = BigDecimal.valueOf(meta.path("regularMarketPrice").asDouble());
                    BigDecimal previousClose = BigDecimal.valueOf(meta.path("previousClose").asDouble());
                    String currency = meta.path("currency").asText("BRL");
                    String exchangeName = meta.path("exchangeName").asText("");
                    
                    BigDecimal change = currentPrice.subtract(previousClose);
                    BigDecimal changePercent = change.divide(previousClose, 4, RoundingMode.HALF_UP)
                                                   .multiply(BigDecimal.valueOf(100));
                    
                    return Optional.of(StockInfo.builder()
                            .symbol(symbol)
                            .currentPrice(currentPrice)
                            .previousClose(previousClose)
                            .change(change)
                            .changePercent(changePercent)
                            .currency(currency)
                            .exchangeName(exchangeName)
                            .build());
                }
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao processar informações do ativo: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static class StockInfo {
        private String symbol;
        private BigDecimal currentPrice;
        private BigDecimal previousClose;
        private BigDecimal change;
        private BigDecimal changePercent;
        private String currency;
        private String exchangeName;
        private Long volume; // Volume de negociação

        public static StockInfoBuilder builder() {
            return new StockInfoBuilder();
        }

        public static class StockInfoBuilder {
            private String symbol;
            private BigDecimal currentPrice;
            private BigDecimal previousClose;
            private BigDecimal change;
            private BigDecimal changePercent;
            private String currency;
            private String exchangeName;
            private Long volume;

            public StockInfoBuilder symbol(String symbol) {
                this.symbol = symbol;
                return this;
            }

            public StockInfoBuilder currentPrice(BigDecimal currentPrice) {
                this.currentPrice = currentPrice;
                return this;
            }

            public StockInfoBuilder previousClose(BigDecimal previousClose) {
                this.previousClose = previousClose;
                return this;
            }

            public StockInfoBuilder change(BigDecimal change) {
                this.change = change;
                return this;
            }

            public StockInfoBuilder changePercent(BigDecimal changePercent) {
                this.changePercent = changePercent;
                return this;
            }

            public StockInfoBuilder currency(String currency) {
                this.currency = currency;
                return this;
            }

            public StockInfoBuilder exchangeName(String exchangeName) {
                this.exchangeName = exchangeName;
                return this;
            }

            public StockInfoBuilder volume(Long volume) {
                this.volume = volume;
                return this;
            }

            public StockInfo build() {
                StockInfo stockInfo = new StockInfo();
                stockInfo.symbol = this.symbol;
                stockInfo.currentPrice = this.currentPrice;
                stockInfo.previousClose = this.previousClose;
                stockInfo.change = this.change;
                stockInfo.changePercent = this.changePercent;
                stockInfo.currency = this.currency;
                stockInfo.exchangeName = this.exchangeName;
                stockInfo.volume = this.volume;
                return stockInfo;
            }
        }

        public String getSymbol() { return symbol; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public BigDecimal getPreviousClose() { return previousClose; }
        public BigDecimal getChange() { return change; }
        public BigDecimal getChangePercent() { return changePercent; }
        public String getCurrency() { return currency; }
        public String getExchangeName() { return exchangeName; }
        public Long getVolume() { return volume; }
    }
    
    // DADOS EXATOS FORNECIDOS PELO USUÁRIO - NÃO ALTERAR
    private BigDecimal getMockPrice(String symbol) {
        switch (symbol.toUpperCase()) {
            // DADOS CORRETOS FORNECIDOS PELO USUÁRIO (COM E SEM .SA)
            case "PETR4":
            case "PETR4.SA": return BigDecimal.valueOf(32.21); // Petrobras PN
            case "VALE3":
            case "VALE3.SA": return BigDecimal.valueOf(53.75); // Vale ON  
            case "BBAS3":
            case "BBAS3.SA": return BigDecimal.valueOf(18.35); // Banco do Brasil ON
            case "ABEV3":
            case "ABEV3.SA": return BigDecimal.valueOf(12.29); // Ambev ON
            case "CSNA3":
            case "CSNA3.SA": return BigDecimal.valueOf(7.62);  // CSN ON
            case "GGBR4":
            case "GGBR4.SA": return BigDecimal.valueOf(16.05); // Gerdau PN
            case "ITUB4":
            case "ITUB4.SA": return BigDecimal.valueOf(34.93); // Itaú Unibanco PN
            case "ITSA4":
            case "ITSA4.SA": return BigDecimal.valueOf(10.34); // Itaúsa PN
            case "BOVA11":
            case "BOVA11.SA": return BigDecimal.valueOf(129.57); // iShares Ibovespa ETF
            
            // Outras ações importantes do mercado brasileiro
            case "BBDC4.SA": return BigDecimal.valueOf(12.84); // Bradesco PN
            case "RENT3.SA": return BigDecimal.valueOf(72.45); // Localiza ON
            case "MGLU3.SA": return BigDecimal.valueOf(6.82);  // Magazine Luiza ON
            case "JBSS3.SA": return BigDecimal.valueOf(32.89); // JBS ON
            case "SUZB3.SA": return BigDecimal.valueOf(54.12); // Suzano ON
            case "LREN3.SA": return BigDecimal.valueOf(23.67); // Lojas Renner ON
            case "RADL3.SA": return BigDecimal.valueOf(48.91); // Raia Drogasil ON
            case "EMBR3.SA": return BigDecimal.valueOf(42.33); // Embraer ON
            case "HAPV3.SA": return BigDecimal.valueOf(6.78);  // Hapvida ON
            case "SANB11.SA": return BigDecimal.valueOf(38.45); // Santander Units
            case "BEEF3.SA": return BigDecimal.valueOf(8.94);  // Minerva ON
            case "VIVT3.SA": return BigDecimal.valueOf(44.12); // Telefônica Brasil ON
            case "ELET3.SA": return BigDecimal.valueOf(39.87); // Eletrobras ON
            case "CSAN3.SA": return BigDecimal.valueOf(19.23); // Cosan ON
            
            // Ações internacionais
            case "AAPL": return BigDecimal.valueOf(227.85);  // Apple
            case "TSLA": return BigDecimal.valueOf(248.78);  // Tesla
            case "GOOGL": return BigDecimal.valueOf(176.32); // Alphabet
            case "MSFT": return BigDecimal.valueOf(422.54);  // Microsoft
            case "NVDA": return BigDecimal.valueOf(875.30);  // NVIDIA
            
            case "TESTE.SA": return BigDecimal.valueOf(25.00); // Para teste específico
            default: return BigDecimal.valueOf(35.00); // Valor padrão
        }
    }
    
    private StockInfo getMockStockInfo(String symbol) {
        BigDecimal currentPrice = getMockPrice(symbol);
        
        // VARIAÇÕES EXATAS FORNECIDAS PELO USUÁRIO - NÃO ALTERAR
        BigDecimal changePercent;
        switch (symbol.toUpperCase()) {
            // DADOS CORRETOS FORNECIDOS PELO USUÁRIO (COM E SEM .SA)
            case "PETR4":
            case "PETR4.SA": changePercent = BigDecimal.valueOf(-1.32); break; // Petrobras PN
            case "VALE3":
            case "VALE3.SA": changePercent = BigDecimal.valueOf(0.54); break;  // Vale ON
            case "BBAS3":
            case "BBAS3.SA": changePercent = BigDecimal.valueOf(-6.85); break; // Banco do Brasil ON
            case "ABEV3":
            case "ABEV3.SA": changePercent = BigDecimal.valueOf(-1.36); break; // Ambev ON
            case "CSNA3":
            case "CSNA3.SA": changePercent = BigDecimal.valueOf(-4.99); break; // CSN ON
            case "GGBR4":
            case "GGBR4.SA": changePercent = BigDecimal.valueOf(-4.69); break; // Gerdau PN
            case "ITUB4":
            case "ITUB4.SA": changePercent = BigDecimal.valueOf(-0.60); break; // Itaú Unibanco PN
            case "ITSA4":
            case "ITSA4.SA": changePercent = BigDecimal.valueOf(-0.10); break; // Itaúsa PN
            case "BOVA11":
            case "BOVA11.SA": changePercent = BigDecimal.valueOf(-0.24); break; // iShares Ibovespa ETF
            
            // Outras ações importantes (variações estimadas realistas)
            case "BBDC4.SA": changePercent = BigDecimal.valueOf(-1.24); break; // Bradesco PN
            case "RENT3.SA": changePercent = BigDecimal.valueOf(1.42); break;  // Localiza ON
            case "MGLU3.SA": changePercent = BigDecimal.valueOf(-3.21); break; // Magazine Luiza ON
            case "JBSS3.SA": changePercent = BigDecimal.valueOf(0.67); break;  // JBS ON
            case "SUZB3.SA": changePercent = BigDecimal.valueOf(-0.89); break; // Suzano ON
            case "LREN3.SA": changePercent = BigDecimal.valueOf(1.12); break;  // Lojas Renner ON
            case "RADL3.SA": changePercent = BigDecimal.valueOf(0.34); break;  // Raia Drogasil ON
            case "EMBR3.SA": changePercent = BigDecimal.valueOf(-1.56); break; // Embraer ON
            case "HAPV3.SA": changePercent = BigDecimal.valueOf(-2.87); break; // Hapvida ON
            case "SANB11.SA": changePercent = BigDecimal.valueOf(-0.78); break; // Santander Units
            case "BEEF3.SA": changePercent = BigDecimal.valueOf(-1.99); break; // Minerva ON
            case "VIVT3.SA": changePercent = BigDecimal.valueOf(0.23); break;  // Telefônica Brasil ON
            case "ELET3.SA": changePercent = BigDecimal.valueOf(2.45); break;  // Eletrobras ON
            case "CSAN3.SA": changePercent = BigDecimal.valueOf(-0.56); break; // Cosan ON
            
            // Ações internacionais (variações estimadas)
            case "AAPL": changePercent = BigDecimal.valueOf(0.87); break;   // Apple
            case "TSLA": changePercent = BigDecimal.valueOf(-2.34); break;  // Tesla
            case "GOOGL": changePercent = BigDecimal.valueOf(1.23); break;  // Alphabet
            case "MSFT": changePercent = BigDecimal.valueOf(0.45); break;   // Microsoft
            case "NVDA": changePercent = BigDecimal.valueOf(3.21); break;   // NVIDIA
            
            default: changePercent = BigDecimal.valueOf(0.5); break; // +0.5% padrão
        }
        
        BigDecimal previousClose = currentPrice.divide(
            BigDecimal.ONE.add(changePercent.divide(BigDecimal.valueOf(100))), 
            4, RoundingMode.HALF_UP
        );
        BigDecimal change = currentPrice.subtract(previousClose);
        
        String currency = symbol.contains(".SA") ? "BRL" : "USD";
        String exchangeName = symbol.contains(".SA") ? "SAO" : "NASDAQ";
        
        // Volumes realistas específicos por ativo
        Long volume;
        switch (symbol.toUpperCase()) {
            // Principais ações com volumes realistas
            case "PETR4.SA": volume = 1_850_000L; break; // 1.85M (muito líquida)
            case "VALE3.SA": volume = 1_200_000L; break; // 1.2M
            case "BBAS3.SA": volume = 850_000L; break;   // 850K
            case "ABEV3.SA": volume = 960_000L; break;   // 960K
            case "CSNA3.SA": volume = 420_000L; break;   // 420K
            case "GGBR4.SA": volume = 380_000L; break;   // 380K
            case "ITUB4.SA": volume = 2_300_000L; break; // 2.3M (mais líquida)
            case "ITSA4.SA": volume = 890_000L; break;   // 890K
            case "BOVA11.SA": volume = 650_000L; break;  // 650K (ETF)
            
            // Outras ações importantes
            case "BBDC4.SA": volume = 1_450_000L; break; // 1.45M (banco líquido)
            case "WEGE3.SA": volume = 680_000L; break;   // 680K
            case "RENT3.SA": volume = 320_000L; break;   // 320K
            case "MGLU3.SA": volume = 1_100_000L; break; // 1.1M (alta volatilidade)
            case "JBSS3.SA": volume = 540_000L; break;   // 540K
            case "SUZB3.SA": volume = 280_000L; break;   // 280K
            case "LREN3.SA": volume = 450_000L; break;   // 450K
            case "RADL3.SA": volume = 290_000L; break;   // 290K
            case "EMBR3.SA": volume = 380_000L; break;   // 380K
            case "HAPV3.SA": volume = 220_000L; break;   // 220K
            case "SANB11.SA": volume = 750_000L; break;  // 750K
            case "BEEF3.SA": volume = 180_000L; break;   // 180K
            case "VIVT3.SA": volume = 340_000L; break;   // 340K
            case "ELET3.SA": volume = 920_000L; break;   // 920K
            case "CSAN3.SA": volume = 260_000L; break;   // 260K
            
            // Ações internacionais (volumes em milhões)
            case "AAPL": volume = 45_000_000L; break;    // 45M (muito líquida)
            case "TSLA": volume = 28_000_000L; break;    // 28M
            case "GOOGL": volume = 15_000_000L; break;   // 15M
            case "MSFT": volume = 22_000_000L; break;    // 22M
            case "NVDA": volume = 35_000_000L; break;    // 35M (alta volatilidade)
            
            default: volume = 500_000L; break;           // 500K padrão
        }
        
        return StockInfo.builder()
                .symbol(symbol)
                .currentPrice(currentPrice)
                .previousClose(previousClose)
                .change(change)
                .changePercent(changePercent)
                .currency(currency)
                .exchangeName(exchangeName)
                .volume(volume)
                .build();
    }
}