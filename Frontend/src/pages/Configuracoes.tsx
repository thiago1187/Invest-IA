
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
import { 
  Settings, 
  User, 
  Bell, 
  Shield, 
  Palette,
  Save,
  Trash2
} from "lucide-react"

export default function Configuracoes() {
  const { toast } = useToast()
  const [userProfile, setUserProfile] = useState<"conservador" | "moderado" | "agressivo">("moderado")
  const [notifications, setNotifications] = useState({
    recommendations: true,
    priceAlerts: true,
    portfolio: true,
    news: false
  })
  
  const [userInfo, setUserInfo] = useState({
    name: "João Silva",
    email: "joao.silva@email.com",
    phone: "(11) 99999-9999"
  })

  useEffect(() => {
    const savedProfile = localStorage.getItem("userProfile") as "conservador" | "moderado" | "agressivo"
    if (savedProfile) {
      setUserProfile(savedProfile)
    }
  }, [])

  const handleSaveProfile = () => {
    localStorage.setItem("userProfile", userProfile)
    toast({
      title: "Perfil atualizado!",
      description: "Suas configurações foram salvas com sucesso.",
    })
  }

  const handleSaveNotifications = () => {
    localStorage.setItem("notifications", JSON.stringify(notifications))
    toast({
      title: "Configurações salvas!",
      description: "Suas preferências de notificação foram atualizadas.",
    })
  }

  const handleSaveUserInfo = () => {
    localStorage.setItem("userInfo", JSON.stringify(userInfo))
    toast({
      title: "Informações atualizadas!",
      description: "Seus dados pessoais foram salvos.",
    })
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
                    <Button onClick={handleSaveUserInfo}>
                      <Save className="mr-2 h-4 w-4" />
                      Salvar Informações
                    </Button>
                  </div>
                </CardContent>
              </Card>

              {/* Perfil de Investidor */}
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Shield className="h-5 w-5" />
                    <span>Perfil de Investidor</span>
                  </CardTitle>
                  <CardDescription>
                    Configure seu perfil de risco
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-3">
                    {[
                      { value: "conservador", label: "Conservador", desc: "Prioriza segurança e baixo risco" },
                      { value: "moderado", label: "Moderado", desc: "Equilibra risco e rentabilidade" },
                      { value: "agressivo", label: "Agressivo", desc: "Busca alta rentabilidade com maior risco" }
                    ].map((option) => (
                      <div
                        key={option.value}
                        className={`p-4 rounded-lg border cursor-pointer transition-colors ${
                          userProfile === option.value
                            ? "border-primary bg-primary/5"
                            : "border-border hover:border-primary/50"
                        }`}
                        onClick={() => setUserProfile(option.value as any)}
                      >
                        <div className="flex items-center space-x-3">
                          <div className={`w-4 h-4 rounded-full border-2 ${
                            userProfile === option.value
                              ? "border-primary bg-primary"
                              : "border-muted-foreground"
                          }`} />
                          <div>
                            <p className="font-medium">{option.label}</p>
                            <p className="text-sm text-muted-foreground">{option.desc}</p>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                  <div className="flex justify-end">
                    <HeroButton onClick={handleSaveProfile}>
                      <Save className="mr-2 h-4 w-4" />
                      Salvar Perfil
                    </HeroButton>
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
                    <Button onClick={handleSaveNotifications}>
                      <Save className="mr-2 h-4 w-4" />
                      Salvar Notificações
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
                  <CardTitle>Informações da Conta</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3 text-sm">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Membro desde:</span>
                    <span>Janeiro 2024</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Último acesso:</span>
                    <span>Hoje</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Plano:</span>
                    <span className="font-medium text-primary">Premium</span>
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
