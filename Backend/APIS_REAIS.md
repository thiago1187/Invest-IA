# 🚀 InvestIA - APIs Reais Implementadas

## ✅ O que foi implementado

### 1. **FinanceAPIService** - Dados reais do Yahoo Finance
- Busca cotações em tempo real de ações da B3
- Cache automático para otimizar performance
- Fallback em caso de falha da API

### 2. **IAService** - Integração real com OpenAI
- Análise de perfil personalizada
- Recomendações baseadas em IA
- Respostas contextualizadas para chatbot
- Análise de carteira com insights reais

### 3. **ChatController** - Chatbot funcional
- Endpoints para perguntas livres
- Análise de carteira via IA
- Recomendações personalizadas

### 4. **DashboardService** - Cálculos com dados reais
- Preços atualizados via Yahoo Finance
- Performance calculada com base em dados reais
- Recomendações geradas por IA

## 🔧 Configuração

### 1. OpenAI API Key
```bash
export OPENAI_API_KEY=sua-chave-openai-aqui
```

Ou adicione no `application.properties`:
```properties
openai.api.key=sua-chave-openai-aqui
```

### 2. PostgreSQL
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/investia
spring.datasource.username=investia_user
spring.datasource.password=investia_pass
```

### 3. Executar aplicação
```bash
./mvnw spring-boot:run
```

## 📊 Endpoints principais

### Autenticação
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Cadastro

### Simulado de Perfil (IA Real)
- `GET /api/simulado/questoes` - Obter questionário
- `POST /api/simulado/processar` - Processar respostas com IA

### Dashboard (Dados Reais)
- `GET /api/dashboard/{userId}` - Dashboard completo
- `GET /api/dashboard/{userId}/recomendacoes` - Recomendações IA

### ChatBot (IA Real)
- `POST /api/chat/pergunta` - Fazer pergunta para IA
- `POST /api/chat/analise-carteira` - Análise automática
- `POST /api/chat/recomendacoes` - Recomendações via chat

### Investimentos
- `GET /api/investimentos` - Listar investimentos
- `POST /api/investimentos` - Adicionar investimento

## 🎯 Funcionalidades implementadas

### ✅ Dados Reais
- **Yahoo Finance API**: Cotações em tempo real
- **Cache inteligente**: 1min para preços, 5min para dados detalhados
- **Fallback**: Valores armazenados se API falhar

### ✅ IA Real
- **OpenAI GPT-3.5-turbo**: Análises e recomendações
- **Prompts especializados**: Para mercado brasileiro
- **Cache de respostas**: 30min para otimizar custos
- **Fallback**: Respostas padrão se IA falhar

### ✅ Análises Inteligentes
- **Perfil de risco**: Questionário com análise por IA
- **Recomendações personalizadas**: Baseadas no perfil
- **Análise de carteira**: Diversificação e riscos
- **ChatBot contextual**: Respostas considerando histórico

## 🔄 Fluxo completo

1. **Cadastro/Login** → Autenticação JWT
2. **Simulado** → IA analisa respostas e define perfil
3. **Dashboard** → Dados reais + análises IA
4. **Investimentos** → Preços atualizados via Yahoo Finance
5. **ChatBot** → Consultas com IA contextualizada

## 📝 Dados de exemplo

O sistema inclui script SQL com ativos reais:
- Ações: VALE3, PETR4, ITUB4, BBDC4, etc.
- ETFs: BOVA11, SMAL11, SPY
- FIIs: MXRF11, XPLG11, KNRI11
- Criptomoedas: BTC, ETH, ADA
- Renda Fixa: Tesouro Direto, CDBs

## ⚡ Performance

- **Cache em camadas**: Reduz chamadas às APIs
- **Requests paralelos**: Múltiplas cotações simultâneas
- **Timeout configurável**: 10s para APIs financeiras, 30s para IA
- **Rate limiting**: Respeita limites das APIs

## 🛡️ Segurança

- **JWT Authentication**: Sessões seguras
- **Validação de entrada**: Prevenção de ataques
- **Logs estruturados**: Monitoramento de falhas
- **Secrets management**: Variáveis de ambiente

## 🚀 Próximos passos

Para completar o sistema:

1. **Frontend**: Integrar com endpoints reais
2. **WebSockets**: Cotações em tempo real
3. **Testes**: Unit e integration tests
4. **Deploy**: Docker + CI/CD
5. **Monitoramento**: Logs e métricas

---

**✅ SISTEMA PRONTO PARA USO COM DADOS REAIS E IA!**

Basta configurar as APIs keys e executar. Todos os services estão implementados com integração real.