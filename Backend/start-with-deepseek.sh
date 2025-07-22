#!/bin/bash

# Script para iniciar o InvestIA com DeepSeek configurado
echo "🤖 Iniciando InvestIA com Nina DeepSeek..."

# Configurar chave DeepSeek
export DEEPSEEK_API_KEY=sk-af369aec7d6d4acf8ca14a483287effc

echo "✅ Chave DeepSeek configurada: ${DEEPSEEK_API_KEY:0:10}..."

# Iniciar o servidor Spring Boot
echo "🚀 Iniciando servidor Spring Boot..."
./mvnw spring-boot:run