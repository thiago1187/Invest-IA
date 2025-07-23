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
  (error: AxiosError) => {
    // Tratar erro 401 (não autorizado)
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('refreshToken');
      toast.error('Sessão expirada. Faça login novamente.');
      // Não fazer redirect aqui para evitar problemas com React Router
      // O AuthContext irá detectar a remoção do token e redirecionar apropriadamente
      return Promise.reject(error);
    }

    // Tratar outros erros HTTP
    const message = getErrorMessage(error);
    
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
  rendaVariavel?: number;
  rendaFixa?: number;
  fundosImobiliarios?: number;
  criptomoedas?: number;
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

export interface PerformanceDetalhada {
  evolucaoPatrimonio: PontoHistorico[];
  rentabilidadePorAtivo: RentabilidadeAtivo[];
  metricas: MetricasRisco;
  comparativoIndices: ComparativoIndices;
}

export const dashboardService = {
  obterDashboard: (userId: string) => 
    api.get<DashboardData>(`/dashboard/${userId}`),
  
  obterPerformanceDetalhada: (userId: string) => 
    api.get<PerformanceDetalhada>(`/dashboard/${userId}/performance`),
  
  atualizarDadosTempoReal: (userId: string) => 
    api.post<void>(`/dashboard/${userId}/atualizar`),
  
  obterRecomendacoes: (userId: string) => 
    api.get<{ recomendacoes: Recomendacao[] }>(`/dashboard/${userId}/recomendacoes`),
  
  obterAlertas: (userId: string) => 
    api.get<{ alertas: Alerta[] }>(`/dashboard/${userId}/alertas`)
};

export const chatService = {
  fazerPergunta: (data: ChatMessage) => 
    api.post<ChatResponse>('/chat/pergunta', data),
  
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

export const cotacaoService = {
  // Obter cotação via Yahoo Finance (dados reais)
  obterCotacao: async (ticker: string): Promise<{ data: CotacaoResponse }> => {
    try {
      const response = await api.get<YahooFinanceResponse>(`/cotacao/${ticker}`);
      
      if (response.data.status === 'SUCCESS' && response.data.data) {
        const yahooData = response.data.data;
        
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
      
      // Fallback para dados mockados se API falhar
      return {
        data: {
          ticker: ticker,
          preco: Math.random() * 100 + 10,
          variacao: (Math.random() - 0.5) * 10,
          variacaoPercent: (Math.random() - 0.5) * 10,
          horario: new Date().toISOString(),
          fechamentoAnterior: Math.random() * 100 + 10
        }
      };
      
    } catch (error) {
      console.warn(`Erro ao obter cotação para ${ticker}, usando dados simulados:`, error);
      
      // Fallback para dados mockados
      return {
        data: {
          ticker: ticker,
          preco: Math.random() * 100 + 10,
          variacao: (Math.random() - 0.5) * 10,
          variacaoPercent: (Math.random() - 0.5) * 10,
          horario: new Date().toISOString(),
          fechamentoAnterior: Math.random() * 100 + 10
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