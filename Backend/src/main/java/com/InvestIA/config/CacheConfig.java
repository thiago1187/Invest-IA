package com.InvestIA.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "stockPrices",     // Cache para preços de ações (TTL: 1 minuto)
                "stockInfo",       // Cache para informações detalhadas (TTL: 5 minutos)
                "openaiResponses"  // Cache para respostas da OpenAI (TTL: 30 minutos)
        ));
        return cacheManager;
    }
}