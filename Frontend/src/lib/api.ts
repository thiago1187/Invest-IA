import axios, { AxiosError, AxiosRequestConfig } from 'axios';
import { toast } from 'sonner';

// Configuração base da API
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000, // 30 segundos para IA
  headers: {
    'Content-Type': 'application/json',
  },
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
      window.location.href = '/login';
      toast.error('Sessão expirada. Faça login novamente.');
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
  password: string;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  usuario: Usuario;
}

export interface Usuario {
  id: string;
  nome: string;
  email: string;
  perfil?: PerfilUsuario;
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
    api.get<SimuladoQuestoes>('/simulado/questoes'),
  
  processarRespostas: (data: SimuladoRespostas) => 
    api.post<ResultadoSimulado>('/simulado/processar', data)
};

export const dashboardService = {
  obterDashboard: (userId: string) => 
    api.get<DashboardData>(`/dashboard/${userId}`),
  
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
    api.post<ChatResponse>('/chat/recomendacoes')
};

export const investimentoService = {
  listar: () => 
    api.get('/investimentos'),
  
  criar: (data: any) => 
    api.post('/investimentos', data),
  
  atualizar: (id: string, data: any) => 
    api.put(`/investimentos/${id}`, data),
  
  deletar: (id: string) => 
    api.delete(`/investimentos/${id}`)
};

export default api;