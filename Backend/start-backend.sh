#!/bin/bash

# Script para iniciar o backend InvestIA com todas as variáveis de ambiente

echo "🚀 Iniciando InvestIA Backend..."

# Definir variáveis de ambiente
export JWT_SECRET_KEY="env7M5oxleLq3vHNu1m+k7zJh/e78vf7C1v+vKgXaun2/nOoAqPorzLusLkl7uaYRWIpsrzPPbVkL0gREHkYRw=="
export GROQ_API_KEY="gsk_R7yBxKj3g7PqC9Hk8WqN2vA5F4T1Xm6Y9Zp3LcD8EfG2MnB7QuV1Ws5RtY4Ux8K"
export H2_CONSOLE_ENABLED=true

echo "✅ Variáveis de ambiente configuradas"
echo "📊 H2 Console: http://localhost:8080/h2-console"
echo "🤖 IA Nina: Habilitada com Groq API"
echo ""

# Iniciar aplicação
./mvnw spring-boot:run