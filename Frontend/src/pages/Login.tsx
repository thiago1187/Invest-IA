import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { HeroButton } from "@/components/ui/hero-button"
import { Eye, EyeOff, TrendingUp, Shield, Brain, DollarSign } from "lucide-react"
import { useToast } from "@/hooks/use-toast"

export default function Login() {
  const [showPassword, setShowPassword] = useState(false)
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { toast } = useToast()

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)

    // Simular login
    setTimeout(() => {
      toast({
        title: "Login realizado com sucesso!",
        description: "Bem-vindo de volta ao InvestIA.",
      })
      navigate("/dashboard")
      setIsLoading(false)
    }, 1500)
  }

  return (
    <div className="min-h-screen bg-background flex">
      {/* Left Side - Login Form */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-md space-y-8">
          {/* Logo */}
          <div className="text-center">
            <div className="inline-flex items-center space-x-2 mb-4">
              <div className="p-2 rounded-xl bg-gradient-primary">
                <TrendingUp className="h-8 w-8 text-primary-foreground" />
              </div>
              <h1 className="text-3xl font-bold bg-gradient-primary bg-clip-text text-transparent">
                InvestIA
              </h1>
            </div>
            <p className="text-muted-foreground">
              Sua plataforma inteligente de investimentos
            </p>
          </div>

          {/* Login Form */}
          <Card className="border-border/50 bg-gradient-surface shadow-large">
            <CardHeader className="space-y-1">
              <CardTitle className="text-2xl text-center">Entrar</CardTitle>
              <CardDescription className="text-center">
                Digite suas credenciais para acessar sua conta
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleLogin} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="seu@email.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="bg-surface border-border/50"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">Senha</Label>
                  <div className="relative">
                    <Input
                      id="password"
                      type={showPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
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
                <div className="flex items-center justify-between">
                  <Link
                    to="/esqueci-senha"
                    className="text-sm text-primary hover:text-primary-glow transition-smooth"
                  >
                    Esqueci minha senha
                  </Link>
                </div>
                <HeroButton
                  type="submit"
                  className="w-full"
                  disabled={isLoading}
                >
                  {isLoading ? "Entrando..." : "Entrar"}
                </HeroButton>
              </form>
              <div className="mt-6 text-center">
                <p className="text-sm text-muted-foreground">
                  Não tem uma conta?{" "}
                  <Link
                    to="/cadastro"
                    className="text-primary hover:text-primary-glow transition-smooth font-medium"
                  >
                    Cadastre-se
                  </Link>
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Right Side - Hero Section */}
      <div className="hidden lg:flex flex-1 bg-gradient-primary relative overflow-hidden">
        <div className="absolute inset-0 bg-black/20" />
        <div className="relative z-10 flex flex-col justify-center items-center text-center p-12 text-primary-foreground">
          <div className="max-w-md space-y-6">
            <h2 className="text-4xl font-bold">
              Investimentos Inteligentes
            </h2>
            <p className="text-xl opacity-90">
              Deixe nossa IA guiar suas decisões de investimento com 
              recomendações personalizadas para seu perfil.
            </p>
            
            <div className="grid grid-cols-1 gap-4 mt-8">
              <div className="flex items-center space-x-3 glass-effect p-4 rounded-lg">
                <Brain className="h-8 w-8 text-secondary-glow" />
                <div className="text-left">
                  <h3 className="font-semibold">IA Personalizada</h3>
                  <p className="text-sm opacity-80">Recomendações baseadas no seu perfil</p>
                </div>
              </div>
              <div className="flex items-center space-x-3 glass-effect p-4 rounded-lg">
                <Shield className="h-8 w-8 text-secondary-glow" />
                <div className="text-left">
                  <h3 className="font-semibold">Gestão de Risco</h3>
                  <p className="text-sm opacity-80">Alertas inteligentes em tempo real</p>
                </div>
              </div>
              <div className="flex items-center space-x-3 glass-effect p-4 rounded-lg">
                <DollarSign className="h-8 w-8 text-secondary-glow" />
                <div className="text-left">
                  <h3 className="font-semibold">Diversificação</h3>
                  <p className="text-sm opacity-80">Carteiras otimizadas automaticamente</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}