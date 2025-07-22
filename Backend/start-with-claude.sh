#!/bin/bash

# Script para iniciar o InvestIA com Claude (seu Claude Pro!)
echo "🤖 Iniciando InvestIA com Nina Claude..."

# Verificar se chave Claude foi fornecida
if [ -z "$1" ]; then
    echo "❌ Erro: Forneça sua chave Claude API"
    echo "Uso: ./start-with-claude.sh sk-ant-api03-sua-chave-aqui"
    echo ""
    echo "Para obter sua chave:"
    echo "1. Acesse: https://console.anthropic.com/"
    echo "2. Vá em 'API Keys'"
    echo "3. Crie uma nova chave (começa com 'sk-ant-')"
    exit 1
fi

# Configurar chave Claude
export CLAUDE_API_KEY=$1

echo "✅ Chave Claude configurada: ${CLAUDE_API_KEY:0:15}..."

# Iniciar o servidor Spring Boot
echo "🚀 Iniciando servidor Spring Boot com Claude..."
./mvnw spring-boot:run