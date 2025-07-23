
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "@/contexts/AuthContext";
import ProtectedRoute from "@/components/ProtectedRoute";
import Index from "./pages/Index";
import Login from "./pages/Login";
import Cadastro from "./pages/Cadastro";
import Investimentos from "./pages/Investimentos";
import Dashboard from "./pages/Dashboard";
import Perfil from "./pages/Perfil";
import Configuracoes from "./pages/Configuracoes";
import Simuladores from "./pages/Simuladores";
import DescubraPerfil from "./pages/DescubraPerfil";
import NotFound from "./pages/NotFound";

// Componente para redirecionar baseado na autenticação e perfil
const HomeRedirect = () => {
  const { isAuthenticated, isLoading, needsProfileAssessment } = useAuth();
  
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p>Carregando...</p>
        </div>
      </div>
    );
  }
  
  // Se não está autenticado, vai para login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  // Se está autenticado mas precisa fazer teste de perfil, vai para descubra perfil
  if (needsProfileAssessment) {
    return <Navigate to="/descubra-perfil" replace />;
  }
  
  // Se está tudo ok, vai para dashboard
  return <Navigate to="/dashboard" replace />;
};

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 3,
      refetchOnWindowFocus: false,
      staleTime: 5 * 60 * 1000, // 5 minutos
    },
  },
});

// Componente interno para as rotas
const AppRoutes = () => (
  <BrowserRouter>
    <Routes>
      {/* Rotas públicas */}
      <Route path="/login" element={<Login />} />
      <Route path="/cadastro" element={<Cadastro />} />
      <Route path="/termos" element={<div className="min-h-screen flex items-center justify-center"><div className="text-center"><h1 className="text-2xl font-bold mb-4">Termos de Uso</h1><p>Página em construção.</p></div></div>} />
      <Route path="/privacidade" element={<div className="min-h-screen flex items-center justify-center"><div className="text-center"><h1 className="text-2xl font-bold mb-4">Política de Privacidade</h1><p>Página em construção.</p></div></div>} />
      
      {/* Redirecionar / baseado na autenticação */}
      <Route path="/" element={<HomeRedirect />} />
      
      {/* Rotas protegidas */}
      <Route 
        path="/dashboard" 
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/investimentos" 
        element={
          <ProtectedRoute>
            <Investimentos />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/perfil" 
        element={
          <ProtectedRoute>
            <Perfil />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/simuladores" 
        element={
          <ProtectedRoute>
            <Simuladores />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/descubra-perfil" 
        element={
          <ProtectedRoute>
            <DescubraPerfil />
          </ProtectedRoute>
        } 
      />
      {/* Redirect antigo para compatibilidade */}
      <Route path="/simulado" element={<Navigate to="/simuladores" replace />} />
      <Route 
        path="/configuracoes" 
        element={
          <ProtectedRoute>
            <Configuracoes />
          </ProtectedRoute>
        } 
      />
      
      {/* Catch-all route */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  </BrowserRouter>
);

const App = () => (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <AppRoutes />
      </TooltipProvider>
    </AuthProvider>
  </QueryClientProvider>
);

export default App;
