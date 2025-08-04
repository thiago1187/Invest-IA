# 🚀 **InvestIA - Plataforma de Investimentos Inteligente**

> Uma aplicação completa para gestão de investimentos com IA, dados reais de mercado e simuladores financeiros avançados.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen)
![React](https://img.shields.io/badge/React-18-blue)
![TypeScript](https://img.shields.io/badge/TypeScript-5.5-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## 📖 **Sobre o Projeto**

InvestIA é uma plataforma completa de investimentos que combina análise de portfólio tradicional com inteligência artificial moderna. O projeto oferece gestão de carteiras, cotações em tempo real, simuladores financeiros e um chatbot inteligente para orientação personalizada de investimentos.

### 🎯 **Principais Funcionalidades**

- 📊 **Dashboard Interativo**: Visualização completa do portfólio com gráficos avançados
- 🤖 **ChatBot Nina**: IA conversacional para consultas sobre investimentos
- 🧮 **Simuladores Financeiros**: Calculadoras para planejamento de metas
- 📈 **Análise de Perfil**: Avaliação de tolerância ao risco e objetivos
- 🔔 **Alertas de Preços**: Notificações personalizadas para ativos
- 📱 **Interface Responsiva**: Design moderno e mobile-first

## 🛠️ **Tecnologias Utilizadas**

### **Backend**
- ☕ **Java 17** - Linguagem principal
- 🚀 **Spring Boot 3.5.3** - Framework principal
- 🔐 **Spring Security** - Autenticação JWT
- 💾 **H2 Database** - Banco de dados embarcado
- 📚 **JPA/Hibernate** - ORM para persistência
- 📖 **Swagger/OpenAPI** - Documentação da API

### **Frontend**
- ⚛️ **React 18** - Biblioteca principal
- 🔷 **TypeScript** - Tipagem estática
- ⚡ **Vite** - Build tool moderna
- 🎨 **Tailwind CSS** - Framework CSS
- 🧩 **Shadcn/ui** - Componentes UI premium
- 📊 **Recharts** - Gráficos e visualizações

### **Integrações**
- 🤖 **Groq API** - IA para chatbot Nina (gratuita)
- 🔄 **Axios** - Cliente HTTP para comunicação

## 🚀 **Como Executar o Projeto**

### **Pré-requisitos**
- Java 17+
- Node.js 18+
- Maven 3.8+

### **1. Clone o Repositório**
```bash
git clone https://github.com/seu-usuario/invest-ia.git
cd invest-ia
```

### **2. Configure o Backend**
```bash
cd Backend

# Copie e configure as variáveis de ambiente
cp .env.example .env
# Edite o arquivo .env com suas chaves de API

# Execute o backend
./mvnw spring-boot:run
```

### **3. Configure o Frontend**
```bash
cd Frontend

# Instale as dependências
npm install

# Execute o frontend
npm run dev
```

### **4. Acesse a Aplicação**
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console

## 🔧 **Variáveis de Ambiente**

Crie um arquivo `.env` no diretório `Backend/` baseado no `.env.example`:

```env
# GroqCloud API Key (OBRIGATÓRIA)
GROQ_API_KEY=sua_chave_groq_aqui

# JWT Secret (OBRIGATÓRIA) 
JWT_SECRET_KEY=sua_chave_jwt_segura_aqui

# Configurações opcionais
H2_CONSOLE_ENABLED=true
```

### **🔑 Como Obter as Chaves**
1. **Groq API**: Registre-se em [console.groq.com](https://console.groq.com/keys) (gratuito)
2. **JWT Secret**: Gere com `openssl rand -base64 64`

## 📊 **Funcionalidades Detalhadas**

### **🏠 Dashboard**
- Resumo do portfólio com métricas essenciais
- Gráficos de evolução patrimonial (30/90 dias)
- Distribuição de ativos por tipo e setor
- Comparação com índices de mercado (IBOVESPA, CDI, IPCA)

### **💼 Gestão de Investimentos**
- Cadastro de investimentos com validação
- Acompanhamento de performance em tempo real
- Cálculo automático de lucro/prejuízo
- Suporte a ações brasileiras e internacionais

### **🤖 ChatBot Nina**
- IA conversacional especializada em investimentos
- Análise personalizada do portfólio
- Recomendações baseadas no perfil do usuário
- Resposta a dúvidas sobre mercado financeiro

### **📈 Simuladores**
1. **Tempo para Meta**: Calcula prazo para atingir objetivos
2. **Valor Mensal**: Define aportes necessários
3. **Simulação Completa**: Projeta cenários com aportes iniciais e mensais

### **👤 Perfil de Investidor**
- Questionário de avaliação de risco
- Classificação automática (Conservador/Moderado/Agressivo)
- Recomendações personalizadas baseadas no perfil

## 📈 **Dados de Mercado**

O projeto combina **dados "reais" com simulações** para demonstração:
- **Taxa SELIC**: 14,25% a.a. (real)
- **CDI**: 12,14% a.a. (real)  
- **Ações**: Alguns dados mockados para demonstração
  - ITUB4: R$ 35,31 (+35% no ano)
  - PETR4: R$ 31,95 (-1,33% no ano)
  - VALE3: R$ 53,00 (estável)

> **Nota**: Alguns dados de mercado são simulados pois o projeto utiliza apenas APIs gratuitas (Yahoo Finance basic tier) para manter o custo zero.

## 🔒 **Segurança**

- ✅ Autenticação JWT com refresh tokens
- ✅ Proteção CORS configurada
- ✅ Validação de entrada em todos os endpoints
- ✅ Chaves de API protegidas por variáveis de ambiente
- ✅ Interceptors para tratamento de erros

## 🧪 **Testando a Aplicação**

### **Usuário de Teste**
```
Email: frontend.user@investia.com
Senha: frontend123
```

### **Fluxo Recomendado**
1. Faça login com o usuário de teste
2. Complete a avaliação de perfil
3. Adicione alguns investimentos
4. Explore o dashboard e gráficos
5. Converse com a Nina (ChatBot)
6. Teste os simuladores financeiros

## 📚 **Estrutura do Projeto**

```
InvestIA/
├── Backend/                 # Spring Boot Application
│   ├── src/main/java/
│   │   └── com/InvestIA/
│   │       ├── controller/  # REST Controllers
│   │       ├── service/     # Business Logic
│   │       ├── entity/      # JPA Entities
│   │       ├── repository/  # Data Access
│   │       └── config/      # Configuration
│   └── .env.example         # Environment template
├── Frontend/                # React Application
│   ├── src/
│   │   ├── components/      # React Components
│   │   ├── pages/          # Application Pages
│   │   ├── contexts/       # React Contexts
│   │   └── lib/           # Utilities & API
└── README.md              # Este arquivo
```

## 🚀 **Deploy**

### **Backend (Heroku)**
```bash
# Configure as variáveis no Heroku Dashboard
heroku config:set GROQ_API_KEY=sua_chave
heroku config:set JWT_SECRET_KEY=sua_chave_jwt
```

### **Frontend (Vercel/Netlify)**
```bash
# Build de produção
npm run build
```

## 🤝 **Contribuição**

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 **Licença**

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👨‍💻 **Desenvolvedor**

**Thiago Alves**
- Desenvolvedor Full Stack especializado em Spring Boot e React
- Focado em soluções fintech e aplicações financeiras
- GitHub: [@thiago-alves](https://github.com/thiago-alves)

## 🌟 **Demonstração**

> 🚧 **Em breve**: Screenshots e GIFs demonstrando as principais funcionalidades

---

<div align="center">

**⭐ Se este projeto te ajudou, deixe uma estrela! ⭐**

</div>
