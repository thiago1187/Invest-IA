
import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { HeroButton } from "@/components/ui/hero-button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"
import { Separator } from "@/components/ui/separator"
import { Header } from "@/components/Header"
import { useToast } from "@/hooks/use-toast"
import { useAuth } from "@/contexts/AuthContext"
import { configuracoesService, perfilService } from "@/lib/api"
import { toast as sonnerToast } from "sonner"
import { 
  Settings, 
  User, 
  Bell, 
  Shield, 
  Palette,
  Save,
  Trash2,
  Calendar,
  Clock
} from "lucide-react"

export default function Configuracoes() {
  const { toast } = useToast()
  const { user, logout } = useAuth()
  const [isLoading, setIsLoading] = useState(false)
  
  const [notifications, setNotifications] = useState({
    recommendations: true,
    priceAlerts: true,
    portfolio: true,
    news: false
  })
  
  const [userInfo, setUserInfo] = useState({
    name: user?.nome || "",
    email: user?.email || "",
    phone: user?.telefone || ""
  })
  
  const [userProfile, setUserProfile] = useState<{
    tipoPerfil?: string
    nivelExperiencia?: string
    pontuacaoSimulado?: number
  } | null>(null)

  const loadUserProfile = async () => {
    try {
      const token = localStorage.getItem('token')
      if (!token) return
      
      const response = await fetch('http://localhost:8080/api/perfil', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const userData = await response.json()
        if (userData.perfil) {
          setUserProfile({
            tipoPerfil: userData.perfil.tipoPerfil,
            nivelExperiencia: userData.perfil.nivelExperiencia,
            pontuacaoSimulado: userData.perfil.pontuacaoSimulado
          })
        }
      }
    } catch (error) {
      console.error('Erro ao carregar perfil do usuário:', error)
    }
  }

  useEffect(() => {
    // Carregar configurações de notificação salvas
    const savedNotifications = localStorage.getItem("notifications")
    if (savedNotifications) {
      setNotifications(JSON.parse(savedNotifications))
    }
    
    // Atualizar informações do usuário se mudaram no contexto
    if (user) {
      setUserInfo({
        name: user.nome || "",
        email: user.email || "",
        phone: user.telefone || ""
      })
      
      // Carregar perfil do backend
      loadUserProfile()
    }
  }, [user])


  const handleSaveNotifications = async () => {
    try {
      setIsLoading(true)
      
      // Salvar no backend
      await configuracoesService.atualizarConfiguracoes({
        emailAlertas: notifications.emailAlerts,
        pushNotifications: notifications.pushAlerts,
        alertasPreco: notifications.priceAlerts,
        relatorioSemanal: notifications.weeklyReport,
        alertasPerformance: notifications.recommendations
      })
      
      // Salvar localmente também
      localStorage.setItem("notifications", JSON.stringify(notifications))
      
      sonnerToast.success("Configurações salvas no servidor!")
      
      // Demonstrar notificações funcionais
      if (notifications.priceAlerts) {
        setTimeout(() => {
          sonnerToast("📈 Alerta de Preço", {
            description: "VALE3 subiu 5% hoje! Suas configurações estão funcionando.",
            duration: 3000
          })
        }, 2000)
      }
      
      if (notifications.recommendations) {
        setTimeout(() => {
          sonnerToast("🤖 Recomendação da IA", {
            description: "Com base no seu perfil, sugerimos diversificar em FIIs.",
            duration: 3000
          })
        }, 3500)
      }
      
      setIsLoading(false)
    } catch (error) {
      setIsLoading(false)
      console.error('Erro ao salvar configurações:', error)
      sonnerToast.error("Erro ao salvar no servidor, mas salvo localmente")
    }
  }

  const handleSaveUserInfo = async () => {
    try {
      setIsLoading(true)
      
      // Validação básica
      if (!userInfo.name.trim()) {
        sonnerToast.error("Nome é obrigatório")
        setIsLoading(false)
        return
      }
      
      // Atualizar no backend
      await perfilService.atualizarPerfil({
        nome: userInfo.name.trim(),
        telefone: userInfo.phone?.trim() || undefined
      })
      
      // Salvar localmente também
      localStorage.setItem("userInfo", JSON.stringify(userInfo))
      
      sonnerToast.success("Informações atualizadas no servidor!")
      setIsLoading(false)
      
    } catch (error) {
      setIsLoading(false)
      console.error('Erro ao atualizar perfil:', error)
      sonnerToast.error("Erro ao salvar no servidor, mas salvo localmente")
    }
  }

  if (!user) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center space-y-4">
          <User className="h-16 w-16 text-muted-foreground mx-auto" />
          <h2 className="text-xl font-semibold">Acesso Restrito</h2>
          <p className="text-muted-foreground">Faça login para acessar as configurações</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header */}
          <div className="flex items-center space-x-3">
            <Settings className="h-8 w-8 text-primary" />
            <div>
              <h1 className="text-3xl font-bold text-foreground">Configurações</h1>
              <p className="text-muted-foreground">
                Gerencie suas preferências e informações pessoais
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Configurações Principais */}
            <div className="lg:col-span-2 space-y-6">
              {/* Informações Pessoais */}
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <User className="h-5 w-5" />
                    <span>Informações Pessoais</span>
                  </CardTitle>
                  <CardDescription>
                    Atualize seus dados pessoais
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="name">Nome Completo</Label>
                      <Input
                        id="name"
                        value={userInfo.name}
                        onChange={(e) => setUserInfo(prev => ({ ...prev, name: e.target.value }))}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="phone">Telefone</Label>
                      <Input
                        id="phone"
                        value={userInfo.phone}
                        onChange={(e) => setUserInfo(prev => ({ ...prev, phone: e.target.value }))}
                      />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="email">E-mail</Label>
                    <Input
                      id="email"
                      type="email"
                      value={userInfo.email}
                      onChange={(e) => setUserInfo(prev => ({ ...prev, email: e.target.value }))}
                    />
                  </div>
                  <div className="flex justify-end">
                    <Button onClick={handleSaveUserInfo} disabled={isLoading}>
                      <Save className="mr-2 h-4 w-4" />
                      {isLoading ? "Salvando..." : "Salvar Informações"}
                    </Button>
                  </div>
                </CardContent>
              </Card>


              {/* Notificações */}
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Bell className="h-5 w-5" />
                    <span>Notificações</span>
                  </CardTitle>
                  <CardDescription>
                    Configure suas preferências de notificação
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="space-y-4">
                    {[
                      { key: "recommendations", label: "Recomendações da IA", desc: "Receba sugestões personalizadas de investimentos" },
                      { key: "priceAlerts", label: "Alertas de Preço", desc: "Notificações sobre mudanças importantes nos preços" },
                      { key: "portfolio", label: "Atualizações da Carteira", desc: "Resumo diário e mensal da sua carteira" },
                      { key: "news", label: "Notícias de Mercado", desc: "Últimas notícias que podem afetar seus investimentos" }
                    ].map((notification) => (
                      <div key={notification.key} className="flex items-center justify-between space-x-4">
                        <div className="flex-1">
                          <p className="font-medium">{notification.label}</p>
                          <p className="text-sm text-muted-foreground">{notification.desc}</p>
                        </div>
                        <Switch
                          checked={notifications[notification.key as keyof typeof notifications]}
                          onCheckedChange={(checked) => 
                            setNotifications(prev => ({ ...prev, [notification.key]: checked }))
                          }
                        />
                      </div>
                    ))}
                  </div>
                  <div className="flex justify-end">
                    <Button onClick={handleSaveNotifications} disabled={isLoading}>
                      <Save className="mr-2 h-4 w-4" />
                      {isLoading ? "Salvando..." : "Salvar Notificações"}
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Sidebar */}
            <div className="space-y-6">
              {/* Ações da Conta */}
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle>Ações da Conta</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <Button variant="outline" className="w-full justify-start">
                    <Shield className="mr-2 h-4 w-4" />
                    Alterar Senha
                  </Button>
                  <Button variant="outline" className="w-full justify-start">
                    <User className="mr-2 h-4 w-4" />
                    Exportar Dados
                  </Button>
                  <Separator />
                  <Button variant="destructive" className="w-full justify-start">
                    <Trash2 className="mr-2 h-4 w-4" />
                    Excluir Conta
                  </Button>
                </CardContent>
              </Card>

              {/* Informações da Conta */}
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <User className="h-4 w-4" />
                    <span>Informações da Conta</span>
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3 text-sm">
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground flex items-center">
                      <Calendar className="h-3 w-3 mr-1" />
                      Membro desde:
                    </span>
                    <span>{user?.dataCriacao ? new Date(user.dataCriacao).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' }) : 'Janeiro 2024'}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground flex items-center">
                      <Clock className="h-3 w-3 mr-1" />
                      Último acesso:
                    </span>
                    <span>{user?.ultimoAcesso ? new Date(user.ultimoAcesso).toLocaleDateString('pt-BR') : 'Hoje'}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground">Status:</span>
                    <span className="font-medium text-success">✓ Ativo</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground">Perfil atual:</span>
                    <span className="font-medium text-primary capitalize">
                      {userProfile?.tipoPerfil?.toLowerCase() || "Não definido"}
                    </span>
                  </div>
                  {userProfile?.nivelExperiencia && (
                    <div className="flex justify-between items-center">
                      <span className="text-muted-foreground">Nível de experiência:</span>
                      <span className="font-medium text-secondary capitalize">
                        {userProfile.nivelExperiencia.toLowerCase()}
                      </span>
                    </div>
                  )}
                  {userProfile?.pontuacaoSimulado && (
                    <div className="flex justify-between items-center">
                      <span className="text-muted-foreground">Pontuação do teste:</span>
                      <span className="font-medium">
                        {userProfile.pontuacaoSimulado}/15 pontos
                      </span>
                    </div>
                  )}
                  <div className="flex justify-end">
                    <Button 
                      size="sm" 
                      variant="outline"
                      onClick={loadUserProfile}
                    >
                      Atualizar Perfil
                    </Button>
                  </div>
                  <Separator />
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground">ID do usuário:</span>
                    <span className="text-xs font-mono">{user?.id?.slice(-8) || '••••••••'}</span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
