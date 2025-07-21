import { useState, useEffect } from "react"
import { Link, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { HeroButton } from "@/components/ui/hero-button"
import { Checkbox } from "@/components/ui/checkbox"
import { Eye, EyeOff, TrendingUp, CheckCircle2 } from "lucide-react"
import { useAuth } from "@/contexts/AuthContext"
import { toast } from "sonner"

export default function Cadastro() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [formData, setFormData] = useState({
    nome: "",
    email: "",
    telefone: "",
    senha: "",
    confirmSenha: "",
    termos: false
  })
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { register, isAuthenticated } = useAuth()

  // Redirecionar se já estiver logado
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/dashboard", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const handleInputChange = (field: string, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (formData.senha !== formData.confirmSenha) {
      toast.error("As senhas não coincidem.");
      return
    }

    if (!formData.termos) {
      toast.error("Você deve aceitar os termos de uso.");
      return
    }

    if (formData.senha.length < 6) {
      toast.error("A senha deve ter pelo menos 6 caracteres.");
      return
    }

    setIsLoading(true)

    try {
      const success = await register(formData.nome, formData.email, formData.senha, formData.telefone);
      
      if (success) {
        // Após o cadastro bem-sucedido, redirecionar para login
        navigate("/login", { replace: true });
      }
    } catch (error) {
      console.error('Erro no cadastro:', error);
    } finally {
      setIsLoading(false);
    }
  }

  const benefits = [
    "Recomendações personalizadas de IA",
    "Análise de risco em tempo real",
    "Simuladores de metas e carteiras",
    "Diversificação automática",
    "Alertas inteligentes"
  ];

  return (
    <div className="min-h-screen bg-background flex">
      {/* Left Side - Benefits */}
      <div className="hidden lg:flex flex-1 bg-gradient-primary relative overflow-hidden">
        <div className="absolute inset-0 bg-black/20" />
        <div className="relative z-10 flex flex-col justify-center items-center text-center p-12 text-primary-foreground">
          <div className="max-w-md space-y-6">
            <div className="inline-flex items-center space-x-2 mb-6">
              <div className="p-3 rounded-xl bg-white/20">
                <TrendingUp className="h-10 w-10" />
              </div>
              <h1 className="text-4xl font-bold">InvestIA</h1>
            </div>
            
            <h2 className="text-3xl font-bold">
              Junte-se aos investidores inteligentes
            </h2>
            <p className="text-xl opacity-90">
              Transforme sua forma de investir com nossa plataforma de IA
            </p>
            
            <div className="space-y-3 mt-8">
              {benefits.map((benefit, index) => (
                <div key={index} className="flex items-center space-x-3 glass-effect p-3 rounded-lg">
                  <CheckCircle2 className="h-5 w-5 text-secondary-glow flex-shrink-0" />
                  <span className="text-left">{benefit}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Right Side - Cadastro Form */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-md space-y-8">
          {/* Mobile Logo */}
          <div className="lg:hidden text-center">
            <div className="inline-flex items-center space-x-2 mb-4">
              <div className="p-2 rounded-xl bg-gradient-primary">
                <TrendingUp className="h-8 w-8 text-primary-foreground" />
              </div>
              <h1 className="text-3xl font-bold bg-gradient-primary bg-clip-text text-transparent">
                InvestIA
              </h1>
            </div>
          </div>

          {/* Cadastro Form */}
          <Card className="border-border/50 bg-gradient-surface shadow-large">
            <CardHeader className="space-y-1">
              <CardTitle className="text-2xl text-center">Criar Conta</CardTitle>
              <CardDescription className="text-center">
                Preencha os dados para começar a investir com IA
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="nome">Nome Completo</Label>
                    <Input
                      id="nome"
                      type="text"
                      placeholder="Seu nome completo"
                      value={formData.nome}
                      onChange={(e) => handleInputChange("nome", e.target.value)}
                      required
                      className="bg-surface border-border/50"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="seu@email.com"
                      value={formData.email}
                      onChange={(e) => handleInputChange("email", e.target.value)}
                      required
                      className="bg-surface border-border/50"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="telefone">Telefone</Label>
                    <Input
                      id="telefone"
                      type="tel"
                      placeholder="(11) 99999-9999"
                      value={formData.telefone}
                      onChange={(e) => handleInputChange("telefone", e.target.value)}
                      required
                      className="bg-surface border-border/50"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="senha">Senha</Label>
                    <div className="relative">
                      <Input
                        id="senha"
                        type={showPassword ? "text" : "password"}
                        placeholder="••••••••"
                        value={formData.senha}
                        onChange={(e) => handleInputChange("senha", e.target.value)}
                        required
                        className="bg-surface border-border/50 pr-10"
                      />
                      <button
                        type="button"
                        className="absolute inset-y-0 right-0 pr-3 flex items-center"
                        onClick={() => setShowPassword(!showPassword)}
                      >
                        {showPassword ? (
                          <EyeOff className="h-4 w-4 text-muted-foreground" />
                        ) : (
                          <Eye className="h-4 w-4 text-muted-foreground" />
                        )}
                      </button>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="confirmSenha">Confirmar Senha</Label>
                    <div className="relative">
                      <Input
                        id="confirmSenha"
                        type={showConfirmPassword ? "text" : "password"}
                        placeholder="••••••••"
                        value={formData.confirmSenha}
                        onChange={(e) => handleInputChange("confirmSenha", e.target.value)}
                        required
                        className="bg-surface border-border/50 pr-10"
                      />
                      <button
                        type="button"
                        className="absolute inset-y-0 right-0 pr-3 flex items-center"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      >
                        {showConfirmPassword ? (
                          <EyeOff className="h-4 w-4 text-muted-foreground" />
                        ) : (
                          <Eye className="h-4 w-4 text-muted-foreground" />
                        )}
                      </button>
                    </div>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="termos"
                    checked={formData.termos}
                    onCheckedChange={(checked) => handleInputChange("termos", !!checked)}
                  />
                  <Label htmlFor="termos" className="text-sm">
                    Aceito os{" "}
                    <Link to="/termos" className="text-primary hover:text-primary-glow transition-smooth">
                      termos de uso
                    </Link>{" "}
                    e{" "}
                    <Link to="/privacidade" className="text-primary hover:text-primary-glow transition-smooth">
                      política de privacidade
                    </Link>
                  </Label>
                </div>

                <HeroButton
                  type="submit"
                  className="w-full"
                  disabled={isLoading}
                >
                  {isLoading ? "Criando conta..." : "Criar Conta"}
                </HeroButton>
              </form>
              
              <div className="mt-6 text-center">
                <p className="text-sm text-muted-foreground">
                  Já tem uma conta?{" "}
                  <Link
                    to="/login"
                    className="text-primary hover:text-primary-glow transition-smooth font-medium"
                  >
                    Faça login
                  </Link>
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}