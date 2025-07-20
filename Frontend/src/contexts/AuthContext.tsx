import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService, Usuario } from '@/lib/api';
import { toast } from 'sonner';

interface AuthContextType {
  user: Usuario | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<boolean>;
  register: (nome: string, email: string, password: string) => Promise<boolean>;
  logout: () => void;
  updateUser: (user: Usuario) => void;
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
      }
    }
    
    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      const response = await authService.login({ email, password });
      const { token: newToken, usuario, refreshToken } = response.data;

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
      console.error('Erro no login:', error);
      
      const message = error.response?.data?.message || 'Erro ao fazer login';
      toast.error(message);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (nome: string, email: string, password: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      const response = await authService.register({ nome, email, password });
      const { token: newToken, usuario, refreshToken } = response.data;

      // Salvar no localStorage
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(usuario));
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken);
      }

      // Atualizar estado
      setToken(newToken);
      setUser(usuario);

      toast.success('Conta criada com sucesso!');
      return true;
    } catch (error: any) {
      console.error('Erro no cadastro:', error);
      
      const message = error.response?.data?.message || 'Erro ao criar conta';
      toast.error(message);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setToken(null);
    setUser(null);
    toast.success('Logout realizado com sucesso!');
  };

  const updateUser = (updatedUser: Usuario) => {
    setUser(updatedUser);
    localStorage.setItem('user', JSON.stringify(updatedUser));
  };

  const value: AuthContextType = {
    user,
    token,
    isLoading,
    isAuthenticated,
    login,
    register,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthProvider;