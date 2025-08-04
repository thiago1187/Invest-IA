import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService, Usuario } from '@/lib/api';
import { toast } from 'sonner';

interface AuthContextType {
  user: Usuario | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  needsProfileAssessment: boolean;
  login: (email: string, senha: string) => Promise<boolean>;
  register: (nome: string, email: string, senha: string, telefone?: string) => Promise<boolean>;
  logout: () => void;
  updateUser: (user: Usuario) => void;
  markProfileAssessmentComplete: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<Usuario | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const isAuthenticated = !!user && !!token;
  
  // Verificar se usuário precisa fazer teste de perfil
  const needsProfileAssessment = isAuthenticated && 
    !localStorage.getItem('profileAssessmentCompleted') && 
    !user?.perfil;

  useEffect(() => {
    // Verificar se há token salvo no localStorage
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');

    if (savedToken && savedUser) {
      try {
        const parsedUser = JSON.parse(savedUser);
        setToken(savedToken);
        setUser(parsedUser);
      } catch (error) {
        console.error('Erro ao parsear dados do usuário:', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('refreshToken');
      }
    }
    
    setIsLoading(false);
  }, []);

  // Escutar mudanças no localStorage (ex: quando API interceptor remove tokens)
  useEffect(() => {
    const checkTokenChanges = () => {
      const savedToken = localStorage.getItem('token');
      const savedUser = localStorage.getItem('user');
      
      // Se não há token mas o contexto ainda tem usuário logado
      if (!savedToken && (token || user)) {
        console.log('🔄 Token removido externamente, fazendo logout...');
        setToken(null);
        setUser(null);
      }
    };
    
    window.addEventListener('storage', checkTokenChanges);
    
    // Verificar periodicamente se o token ainda está presente
    const interval = setInterval(checkTokenChanges, 1000);
    
    return () => {
      window.removeEventListener('storage', checkTokenChanges);
      clearInterval(interval);
    };
  }, [token, user]);

  const login = async (email: string, senha: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      console.log('🔐 Fazendo login com:', { email, senha: senha ? '***' : 'vazio' });
      const response = await authService.login({ email, senha });
      console.log('✅ Resposta do login:', response.data);
      const { accessToken: newToken, usuario, refreshToken } = response.data;

      // Salvar no localStorage
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(usuario));
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken);
      }

      // Atualizar estado
      setToken(newToken);
      setUser(usuario);

      toast.success('Login realizado com sucesso!');
      return true;
    } catch (error: any) {
      console.error('❌ Erro no login:', error);
      console.error('❌ Error response:', error.response?.data);
      console.error('❌ Error status:', error.response?.status);
      
      let message = 'Erro ao fazer login';
      
      if (error.response?.data?.message) {
        message = error.response.data.message;
      } else if (error.response?.status === 401) {
        message = 'Email ou senha incorretos';
      } else if (error.response?.status === 403) {
        message = 'Acesso negado. Verifique suas credenciais';
      } else if (error.response?.status === 404) {
        message = 'Email não encontrado';
      } else if (error.response?.status >= 500) {
        message = 'Erro no servidor. Tente novamente mais tarde';
      } else if (error.message?.includes('Network Error')) {
        message = 'Erro de conexão. Verifique sua internet';
      }
      
      toast.error(message);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (nome: string, email: string, senha: string, telefone?: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      const response = await authService.register({ nome, email, senha, telefone });
      
      // Apenas mostrar sucesso sem fazer login automático
      toast.success('Conta criada com sucesso! Faça login para continuar.');
      return true;
    } catch (error: any) {
      console.error('❌ Erro no cadastro:', error);
      console.error('❌ Error response:', error.response?.data);
      console.error('❌ Error status:', error.response?.status);
      
      let message = 'Erro ao criar conta';
      
      if (error.response?.data?.message) {
        message = error.response.data.message;
      } else if (error.response?.status === 400) {
        message = 'Dados inválidos. Verifique as informações';
      } else if (error.response?.status === 409) {
        message = 'Email já cadastrado. Use outro email';
      } else if (error.response?.status >= 500) {
        message = 'Erro no servidor. Tente novamente mais tarde';
      } else if (error.message?.includes('Network Error')) {
        message = 'Erro de conexão. Verifique sua internet';
      }
      
      toast.error(message);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    // Limpar dados primeiro
    authService.logout();
    setToken(null);
    setUser(null);
    
    // Limpar qualquer outro dado relacionado
    localStorage.removeItem('userProfile');
    localStorage.removeItem('userInvestments');
    localStorage.removeItem('userNotifications');
    localStorage.removeItem('profileAssessmentCompleted');
    
    toast.success('Logout realizado com sucesso!');
  };

  const updateUser = (updatedUser: Usuario) => {
    setUser(updatedUser);
    localStorage.setItem('user', JSON.stringify(updatedUser));
  };

  const markProfileAssessmentComplete = () => {
    localStorage.setItem('profileAssessmentCompleted', 'true');
  };

  const value: AuthContextType = {
    user,
    token,
    isLoading,
    isAuthenticated,
    needsProfileAssessment,
    login,
    register,
    logout,
    updateUser,
    markProfileAssessmentComplete,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthProvider;