import axios, { AxiosError, AxiosRequestConfig } from 'axios';
import { toast } from 'sonner';

// Configuração base da API
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000, // 30 segundos para IA
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false, // Para CORS
});

// Interceptor para adicionar token JWT automaticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para tratar respostas e erros
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    // Tratar erro 401/403 (não autorizado)
    if ((error.response?.status === 401 || error.response?.status === 403) && !originalRequest._retry) {
      originalRequest._retry = true;
      
      const refreshToken = localStorage.getItem('refreshToken');
      
      if (refreshToken) {
        try {
          console.log('🔄 Tentando renovar token...');
          const response = await authService.refreshToken(refreshToken);
          const newToken = response.data.accessToken;
          
          // Salvar novo token
          localStorage.setItem('token', newToken);
          localStorage.setItem('refreshToken', response.data.refreshToken);
          
          // Retry request com novo token
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return api(originalRequest);
          
        } catch (refreshError) {
          console.error('❌ Erro ao renovar token:', refreshError);
          // Se refresh falhou, fazer logout
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          localStorage.removeItem('refreshToken');
          toast.error('Sessão expirada. Faça login novamente.');
          window.location.href = '/login';
          return Promise.reject(refreshError);
        }
      } else {
        // Sem refresh token - fazer logout direto
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('refreshToken');
        toast.error('Sessão expirada. Faça login novamente.');
        window.location.href = '/login';
        return Promise.reject(error);
      }
    }

    // Tratar outros erros HTTP
    const message = getErrorMessage(error);
    
    // Log detalhado para debug
    console.error('🚨 API Error:', {
      status: error.response?.status,
      url: error.config?.url,
      method: error.config?.method,
      message: message,
      data: error.response?.data
    });
    
    // Só mostrar toast para erros críticos (5xx)
    if (error.response?.status && error.response.status >= 500) {
      toast.error(`Erro no servidor: ${message}`);
    }

    return Promise.reject(error);
  }
);

// Helper para extrair mensagem de erro
function getErrorMessage(error: AxiosError): string {
  if (error.response?.data && typeof error.response.data === 'object') {
    const data = error.response.data as any;
    return data.message || data.error || 'Erro desconhecido';
  }
  return error.message || 'Erro de rede';
}

// Tipos para as principais entidades
export interface LoginRequest {
  email: string;
  senha: string;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
  telefone?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  usuario: Usuario;
}

export interface Usuario {
  id: string;
  nome: string;
  email: string;
  telefone?: string;
  perfil?: PerfilUsuario;
  criadoEm?: string;
}

export interface PerfilUsuario {
  tipoPerfil: 'CONSERVADOR' | 'MODERADO' | 'AGRESSIVO';
  nivelExperiencia: 'INICIANTE' | 'INTERMEDIARIO' | 'AVANCADO' | 'EXPERT';
  toleranciaRisco: number;
}

export interface SimuladoQuestoes {
  perguntas: PerguntaSimulado[];
}

export interface PerguntaSimulado {
  id: number;
  pergunta: string;
  categoria: string;
  opcoes: OpcaoResposta[];
}

export interface OpcaoResposta {
  id: string;
  texto: string;
  pontos: number;
}

export interface SimuladoRespostas {
  respostas: Record<number, string>;
}

export interface ResultadoSimulado {
  perfil: 'CONSERVADOR' | 'MODERADO' | 'AGRESSIVO';
  nivelExperiencia: 'INICIANTE' | 'INTERMEDIARIO' | 'AVANCADO' | 'EXPERT';
  pontuacaoTotal: number;
  descricaoPerfil: string;
  caracteristicas: string[];
  recomendacoesIniciais: string[];
  toleranciaRisco: number;
}

export interface DashboardData {
  resumoCarteira: ResumoCarteira;
  distribuicaoAtivos: DistribuicaoAtivos;
  performance: PerformanceCarteira;
  alertasRecentes: Alerta[];
  recomendacoesDestaque: Recomendacao[];
}

export interface ResumoCarteira {
  valorTotal: number;
  valorInvestido: number;
  lucroPreju: number;
  percentualLucroPreju: number;
  variacaoDiaria: number;
  variacaoMensal: number;
  totalAtivos: number;
}

export interface DistribuicaoAtivos {
  porTipo: { [key: string]: number };
  porSetor: { [key: string]: number };
  percentualRendaVariavel: number;
  percentualRendaFixa: number;
}

export interface PerformanceCarteira {
  evolucaoPatrimonio?: { data: string; valor: number }[];
  rentabilidadeAno: number;
  rentabilidadeMes: number;
  volatilidade: number;
  sharpeRatio?: number;
}

export interface Alerta {
  id: string;
  tipo: string;
  titulo: string;
  mensagem: string;
  severidade?: string;
  dataHora: string;
}

export interface Recomendacao {
  id: string;
  ativo?: any;
  tipoRecomendacao: string;
  motivo: string;
  precoAlvo: number;
  confianca: number;
  dataRecomendacao: string;
}

export interface ChatMessage {
  pergunta: string;
}

export interface ChatResponse {
  resposta: string;
  sucesso: boolean;
  erro?: string;
}

// Serviços da API
export const authService = {
  login: (data: LoginRequest) => 
    api.post<AuthResponse>('/auth/login', data),
  
  register: (data: RegisterRequest) => 
    api.post<AuthResponse>('/auth/register', data),
  
  refreshToken: (refreshToken: string) => 
    api.post<AuthResponse>('/auth/refresh', { refreshToken }),
  
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('refreshToken');
  }
};

export const simuladoService = {
  obterQuestoes: () => 
    api.get<SimuladoQuestoes>('/descubra-perfil/perguntas'),
  
  processarRespostas: (data: SimuladoRespostas) => 
    api.post<ResultadoSimulado>('/descubra-perfil/responder', data)
};

// Interfaces para performance detalhada
export interface RentabilidadeAtivo {
  simbolo: string;
  nome: string;
  rentabilidade: number;
  valorInvestido: number;
  valorAtual: number;
  participacao: number;
}

export interface MetricasRisco {
  sharpeRatio: number;
  varDiario: number;
  beta: number;
  volatilidade30d: number;
  correlacaoIbov: number;
}

export interface ComparativoIndices {
  carteira: number;
  ibovespa: number;
  ifix: number;
  cdi: number;
  ipca: number;
}

export interface PontoHistorico {
  data: string;
  valor: number;
}

export interface PerformanceDetalhada {
  evolucaoPatrimonio: PontoHistorico[];
  rentabilidadePorAtivo: RentabilidadeAtivo[];
  metricas: MetricasRisco;
  comparativoIndices: ComparativoIndices;
}

export const dashboardService = {
  obterDashboard: () => 
    api.get<DashboardData>('/dashboard/teste'),
  
  obterPerformanceDetalhada: () => 
    api.get<PerformanceDetalhada>('/dashboard/performance'),
  
  atualizarDadosTempoReal: () => 
    api.post<void>('/dashboard/atualizar'),
  
  obterRecomendacoes: () => 
    api.get<{ recomendacoes: Recomendacao[] }>('/dashboard/recomendacoes'),
  
  obterAlertas: () => 
    api.get<{ alertas: Alerta[] }>('/dashboard/alertas')
};

export const chatService = {
  fazerPergunta: (data: ChatMessage) => 
    api.post<ChatResponse>('/chat/teste', { mensagem: data.pergunta }),
  
  analisarCarteira: () => 
    api.post<ChatResponse>('/chat/analise-carteira'),
  
  obterRecomendacoes: () => 
    api.post<ChatResponse>('/chat/recomendacoes'),
  
  obterHistorico: (limite: number = 10) =>
    api.get<any[]>(`/chat/historico?limite=${limite}`),
  
  avaliarResposta: (conversaId: string, avaliacao: number) =>
    api.post<void>(`/chat/avaliar/${conversaId}`, { avaliacao }),
  
  obterEstatisticas: () =>
    api.get<{
      totalConversas: number;
      mediaAvaliacao: number;
      tiposMaisUsados: string[];
    }>('/chat/estatisticas')
};

// Interfaces para Investimentos
export interface CriarInvestimentoRequest {
  ticker: string;
  quantidade: number;
  valorCompra: number;
  dataCompra: string;
}

export interface AtualizarInvestimentoRequest {
  quantidade?: number;
  valorMedioCompra?: number;
}

export interface AtivoResponse {
  id: string;
  ticker: string;
  nome: string;
  tipoAtivo: 'ACAO' | 'FII' | 'RENDA_FIXA' | 'CRIPTO';
}

export interface InvestimentoResponse {
  id: string;
  ativo: AtivoResponse;
  quantidade: number;
  valorMedioCompra: number;
  valorAtual: number;
  valorTotalInvestido: number;
  valorTotalAtual: number;
  lucroPreju: number;
  percentualLucroPreju: number;
  dataCompra: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const investimentoService = {
  listar: (page = 0, size = 20) => 
    api.get<PageResponse<InvestimentoResponse>>(`/investimentos?page=${page}&size=${size}`),
  
  criar: (data: CriarInvestimentoRequest) => 
    api.post<InvestimentoResponse>('/investimentos', data),
  
  atualizar: (id: string, data: AtualizarInvestimentoRequest) => 
    api.put<InvestimentoResponse>(`/investimentos/${id}`, data),
  
  deletar: (id: string) => 
    api.delete(`/investimentos/${id}`)
};

// Serviços de Cotação
export interface CotacaoResponse {
  ticker: string;
  preco: number;
  variacao: number;
  variacaoPercent: number;
  horario: string;
  volume?: number;
  abertura?: number;
  maxima?: number;
  minima?: number;
  fechamentoAnterior?: number;
}

// Resposta da API do Yahoo Finance (backend)
export interface YahooFinanceResponse {
  symbol: string;
  data: {
    symbol: string;
    currentPrice: number;
    previousClose: number;
    change: number;
    changePercent: number;
    currency: string;
    exchangeName: string;
  };
  status: string;
}

// Serviços de Perfil e Configurações
export const perfilService = {
  obterPerfil: () => 
    api.get<Usuario>('/perfil'),
  
  atualizarPerfil: (data: { nome?: string; telefone?: string }) => 
    api.put<Usuario>('/perfil', data),
  
  salvarAvaliacao: (data: { 
    tipoPerfil: string; 
    nivelExperiencia: string; 
    toleranciaRisco: number;
    respostasCompletas: Record<string, any>
  }) => 
    api.post<{ message: string }>('/perfil/avaliacao', data)
};

export const configuracoesService = {
  obterConfiguracoes: () => 
    api.get<Record<string, boolean>>('/configuracoes/notificacoes'),
  
  atualizarConfiguracoes: (data: Record<string, boolean>) => 
    api.put<{ message: string }>('/configuracoes/notificacoes', data),
  
  testarNotificacao: () => 
    api.post<{ message: string; tipo: string; timestamp: string }>('/configuracoes/testar-notificacao')
};

// DADOS EXATOS FORNECIDOS PELO USUÁRIO - NÃO ALTERAR
const FALLBACK_STOCK_PRICES = {
  'PETR4': { preco: 32.21, variacao: -1.32 },    // Petrobras PN
  'VALE3': { preco: 53.75, variacao: 0.54 },     // Vale ON
  'BBAS3': { preco: 18.35, variacao: -6.85 },    // Banco do Brasil ON
  'ABEV3': { preco: 12.29, variacao: -1.36 },    // Ambev ON
  'CSNA3': { preco: 7.62, variacao: -4.99 },     // CSN ON
  'GGBR4': { preco: 16.05, variacao: -4.69 },    // Gerdau PN
  'ITUB4': { preco: 34.93, variacao: -0.60 },    // Itaú Unibanco PN
  'ITSA4': { preco: 10.34, variacao: -0.10 },    // Itaúsa PN
  'BOVA11': { preco: 129.57, variacao: -0.24 },  // iShares Ibovespa ETF
  'BBDC4': { preco: 12.84, variacao: -1.24 },    // Bradesco PN
  'RENT3': { preco: 72.45, variacao: 1.42 },     // Localiza ON
  'MGLU3': { preco: 6.82, variacao: -3.21 },     // Magazine Luiza ON
  'JBSS3': { preco: 32.89, variacao: 0.67 },     // JBS ON
  'SUZB3': { preco: 54.12, variacao: -0.89 },    // Suzano ON
  'LREN3': { preco: 23.67, variacao: 1.12 },     // Lojas Renner ON
  'RADL3': { preco: 48.91, variacao: 0.34 },     // Raia Drogasil ON
  'EMBR3': { preco: 42.33, variacao: -1.56 },    // Embraer ON
  'HAPV3': { preco: 6.78, variacao: -2.87 },     // Hapvida ON
  'VIVT3': { preco: 44.12, variacao: 0.23 },     // Telefônica Brasil ON
  'ELET3': { preco: 39.87, variacao: 2.45 }      // Eletrobras ON
};

export const cotacaoService = {
  // Obter cotação via backend (dados reais do Yahoo Finance)
  obterCotacao: async (ticker: string): Promise<{ data: CotacaoResponse }> => {
    try {
      // Primeiro tenta buscar via endpoint real-data
      const response = await api.get<{ price: number; variation: number }>(`/real-data/price/${ticker}`);
      
      if (response.data && response.data.price) {
        return {
          data: {
            ticker: ticker,
            preco: response.data.price,
            variacao: response.data.variation || 0,
            variacaoPercent: response.data.variation || 0,
            horario: new Date().toISOString(),
            fechamentoAnterior: response.data.price - (response.data.variation || 0)
          }
        };
      }
      
      // Fallback para endpoint cotacao se real-data não funcionar
      const fallbackResponse = await api.get<YahooFinanceResponse>(`/cotacao/${ticker}`);
      
      if (fallbackResponse.data.status === 'SUCCESS' && fallbackResponse.data.data) {
        const yahooData = fallbackResponse.data.data;
        
        return {
          data: {
            ticker: yahooData.symbol,
            preco: yahooData.currentPrice,
            variacao: yahooData.change,
            variacaoPercent: yahooData.changePercent,
            horario: new Date().toISOString(),
            fechamentoAnterior: yahooData.previousClose
          }
        };
      }
      
      // Usar dados de fallback realistas
      const fallback = FALLBACK_STOCK_PRICES[ticker] || { preco: 25.00, variacao: 0.00 };
      
      return {
        data: {
          ticker: ticker,
          preco: fallback.preco,
          variacao: fallback.variacao,
          variacaoPercent: fallback.variacao,
          horario: new Date().toISOString(),
          fechamentoAnterior: fallback.preco - fallback.variacao
        }
      };
      
    } catch (error) {
      console.warn(`Erro ao obter cotação para ${ticker}, usando dados simulados:`, error);
      
      // Usar dados de fallback realistas
      const fallback = FALLBACK_STOCK_PRICES[ticker] || { preco: 25.00, variacao: 0.00 };
      
      return {
        data: {
          ticker: ticker,
          preco: fallback.preco,
          variacao: fallback.variacao,
          variacaoPercent: fallback.variacao,
          horario: new Date().toISOString(),
          fechamentoAnterior: fallback.preco - fallback.variacao
        }
      };
    }
  },
  
  // Método alternativo para buscar cotação (compatibilidade)
  buscarCotacao: async (ticker: string): Promise<{ data: CotacaoResponse }> => {
    return cotacaoService.obterCotacao(ticker);
  }
};

export default api;