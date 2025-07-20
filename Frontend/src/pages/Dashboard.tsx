import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { HeroButton } from "@/components/ui/hero-button"
import { InvestmentCard } from "@/components/ui/investment-card"
import { ChatBot } from "@/components/ChatBot"
import { Header } from "@/components/Header"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import { Badge } from "@/components/ui/badge"
import { 
  TrendingUp, 
  TrendingDown, 
  DollarSign, 
  PieChart, 
  Target, 
  AlertTriangle,
  Bell,
  Brain,
  BarChart3,
  Building,
  Coins
} from "lucide-react"
import { 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  PieChart as RechartsPieChart,
  Pie,
  Cell,
  BarChart,
  Bar
} from "recharts"

// Mock data
const portfolioData = [
  { month: 'Jan', valor: 50000 },
  { month: 'Fev', valor: 52000 },
  { month: 'Mar', valor: 48000 },
  { month: 'Abr', valor: 55000 },
  { month: 'Mai', valor: 58000 },
  { month: 'Jun', valor: 62000 },
]

const distributionData = [
  { name: 'Renda Fixa', value: 40, color: '#8B5CF6' },
  { name: 'Ações', value: 35, color: '#3B82F6' },
  { name: 'FIIs', value: 15, color: '#10B981' },
  { name: 'Cripto', value: 10, color: '#F59E0B' },
]

const recommendationsData = [
  { asset: 'VALE3', score: 8.5, type: 'Ação' },
  { asset: 'MXRF11', score: 7.8, type: 'FII' },
  { asset: 'Tesouro IPCA+', score: 9.2, type: 'Renda Fixa' },
  { asset: 'PETR4', score: 6.9, type: 'Ação' },
]

export default function Dashboard() {
  const [userProfile, setUserProfile] = useState<"conservador" | "moderado" | "agressivo">("moderado")
  const [alerts, setAlerts] = useState([
    {
      id: 1,
      type: "opportunity",
      title: "Oportunidade de Compra",
      message: "VALE3 apresenta uma queda de 5% e está próxima do suporte técnico.",
      time: "2h atrás"
    },
    {
      id: 2,
      type: "warning",
      title: "Concentração de Risco",
      message: "Sua carteira está muito concentrada em ações. Considere diversificar.",
      time: "1 dia atrás"
    },
    {
      id: 3,
      type: "info",
      title: "Meta Alcançada",
      message: "Parabéns! Você atingiu 75% da sua meta de R$ 100.000.",
      time: "3 dias atrás"
    }
  ])

  useEffect(() => {
    // Carregar perfil do usuário
    const savedProfile = localStorage.getItem("userProfile") as "conservador" | "moderado" | "agressivo"
    if (savedProfile) {
      setUserProfile(savedProfile)
    }
  }, [])

  const getAlertIcon = (type: string) => {
    switch (type) {
      case "opportunity":
        return <TrendingUp className="h-4 w-4 text-success" />
      case "warning":
        return <AlertTriangle className="h-4 w-4 text-warning" />
      default:
        return <Bell className="h-4 w-4 text-secondary" />
    }
  }

  const getAlertColor = (type: string) => {
    switch (type) {
      case "opportunity":
        return "bg-success/10 border-success/20"
      case "warning":
        return "bg-warning/10 border-warning/20"
      default:
        return "bg-secondary/10 border-secondary/20"
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container mx-auto px-4 py-8 pb-24">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Cards Resumo */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <InvestmentCard
              title="Patrimônio Total"
              value="R$ 62.450"
              change="+12.5%"
              changeType="positive"
              icon={<DollarSign className="h-5 w-5" />}
            />
            <InvestmentCard
              title="Rentabilidade Mensal"
              value="R$ 1.850"
              change="+8.2%"
              changeType="positive"
              icon={<TrendingUp className="h-5 w-5" />}
            />
            <InvestmentCard
              title="Dividendos Recebidos"
              value="R$ 420"
              change="+15.3%"
              changeType="positive"
              icon={<Coins className="h-5 w-5" />}
            />
            <InvestmentCard
              title="Meta Anual"
              value="75%"
              change="R$ 25.000 restantes"
              changeType="neutral"
              icon={<Target className="h-5 w-5" />}
            />
          </div>

          {/* Tabs Content */}
          <Tabs defaultValue="overview" className="space-y-6">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="overview">Visão Geral</TabsTrigger>
              <TabsTrigger value="recommendations">Recomendações</TabsTrigger>
              <TabsTrigger value="simulator">Simulador</TabsTrigger>
              <TabsTrigger value="alerts">Alertas</TabsTrigger>
            </TabsList>

            {/* Visão Geral */}
            <TabsContent value="overview" className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Evolução da Carteira */}
                <Card className="bg-gradient-surface border-border/50">
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <BarChart3 className="h-5 w-5" />
                      <span>Evolução da Carteira</span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width="100%" height={300}>
                      <LineChart data={portfolioData}>
                        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                        <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" />
                        <YAxis stroke="hsl(var(--muted-foreground))" />
                        <Tooltip 
                          contentStyle={{
                            backgroundColor: 'hsl(var(--card))',
                            border: '1px solid hsl(var(--border))',
                            borderRadius: '8px'
                          }}
                        />
                        <Line 
                          type="monotone" 
                          dataKey="valor" 
                          stroke="hsl(var(--primary))" 
                          strokeWidth={3}
                          dot={{ fill: 'hsl(var(--primary))', strokeWidth: 2, r: 4 }}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>

                {/* Distribuição da Carteira */}
                <Card className="bg-gradient-surface border-border/50">
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <PieChart className="h-5 w-5" />
                      <span>Distribuição da Carteira</span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width="100%" height={300}>
                      <RechartsPieChart>
                        <Pie
                          data={distributionData}
                          cx="50%"
                          cy="50%"
                          outerRadius={80}
                          dataKey="value"
                          label={({ name, value }) => `${name}: ${value}%`}
                        >
                          {distributionData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={entry.color} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </RechartsPieChart>
                    </ResponsiveContainer>
                    <div className="grid grid-cols-2 gap-2 mt-4">
                      {distributionData.map((item) => (
                        <div key={item.name} className="flex items-center space-x-2">
                          <div 
                            className="w-3 h-3 rounded-full" 
                            style={{ backgroundColor: item.color }}
                          />
                          <span className="text-sm">{item.name}</span>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Meta Progress */}
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle>Progresso da Meta Anual</CardTitle>
                  <CardDescription>
                    Meta: R$ 100.000 até dezembro de 2024
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex justify-between text-sm">
                      <span>R$ 75.000</span>
                      <span>R$ 100.000</span>
                    </div>
                    <Progress value={75} className="h-3" />
                    <div className="flex justify-between text-sm text-muted-foreground">
                      <span>75% concluído</span>
                      <span>R$ 25.000 restantes</span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Recomendações */}
            <TabsContent value="recommendations" className="space-y-6">
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Brain className="h-5 w-5" />
                    <span>Recomendações da IA</span>
                  </CardTitle>
                  <CardDescription>
                    Baseado no seu perfil {userProfile} e análise de mercado
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {recommendationsData.map((rec, index) => (
                      <div key={index} className="flex items-center justify-between p-4 bg-surface rounded-lg">
                        <div className="flex items-center space-x-3">
                          {rec.type === 'Ação' && <TrendingUp className="h-5 w-5 text-secondary" />}
                          {rec.type === 'FII' && <Building className="h-5 w-5 text-success" />}
                          {rec.type === 'Renda Fixa' && <DollarSign className="h-5 w-5 text-primary" />}
                          <div>
                            <p className="font-semibold">{rec.asset}</p>
                            <p className="text-sm text-muted-foreground">{rec.type}</p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <div className="text-right">
                            <p className="text-sm text-muted-foreground">Score IA</p>
                            <p className="font-bold">{rec.score}/10</p>
                          </div>
                          <HeroButton size="sm">Analisar</HeroButton>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Simulador */}
            <TabsContent value="simulator" className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card className="bg-gradient-surface border-border/50">
                  <CardHeader>
                    <CardTitle>Simulador de Metas</CardTitle>
                    <CardDescription>
                      Calcule quanto investir para atingir seus objetivos
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <label className="text-sm font-medium">Meta de Valor</label>
                      <input 
                        type="text" 
                        placeholder="R$ 100.000" 
                        className="w-full p-3 rounded-lg bg-background border border-border/50"
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-medium">Prazo (anos)</label>
                      <input 
                        type="number" 
                        placeholder="5" 
                        className="w-full p-3 rounded-lg bg-background border border-border/50"
                      />
                    </div>
                    <HeroButton className="w-full">Calcular</HeroButton>
                    
                    <div className="mt-4 p-4 bg-primary/10 rounded-lg">
                      <p className="text-sm text-muted-foreground">Resultado do cálculo:</p>
                      <p className="text-lg font-bold">R$ 1.200/mês</p>
                      <p className="text-sm">Com rentabilidade média de 12% a.a.</p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-surface border-border/50">
                  <CardHeader>
                    <CardTitle>Simulador de Carteira</CardTitle>
                    <CardDescription>
                      Teste diferentes alocações de investimento
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-3">
                      <div className="flex justify-between items-center">
                        <span>Renda Fixa</span>
                        <span>40%</span>
                      </div>
                      <Progress value={40} />
                      
                      <div className="flex justify-between items-center">
                        <span>Ações</span>
                        <span>35%</span>
                      </div>
                      <Progress value={35} />
                      
                      <div className="flex justify-between items-center">
                        <span>FIIs</span>
                        <span>25%</span>
                      </div>
                      <Progress value={25} />
                    </div>
                    
                    <HeroButton className="w-full">Simular Resultado</HeroButton>
                    
                    <div className="mt-4 p-4 bg-success/10 rounded-lg">
                      <p className="text-sm text-muted-foreground">Rentabilidade estimada:</p>
                      <p className="text-lg font-bold text-success">+11.5% a.a.</p>
                      <p className="text-sm">Risco: Moderado</p>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>

            {/* Alertas */}
            <TabsContent value="alerts" className="space-y-6">
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Bell className="h-5 w-5" />
                    <span>Alertas Inteligentes</span>
                  </CardTitle>
                  <CardDescription>
                    Notificações personalizadas baseadas em IA
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {alerts.map((alert) => (
                      <div 
                        key={alert.id} 
                        className={`p-4 rounded-lg border ${getAlertColor(alert.type)}`}
                      >
                        <div className="flex items-start space-x-3">
                          {getAlertIcon(alert.type)}
                          <div className="flex-1">
                            <h4 className="font-semibold">{alert.title}</h4>
                            <p className="text-sm text-muted-foreground mt-1">
                              {alert.message}
                            </p>
                            <p className="text-xs text-muted-foreground mt-2">
                              {alert.time}
                            </p>
                          </div>
                          <Button variant="ghost" size="sm">
                            Ação
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>

        {/* ChatBot */}
        <ChatBot userProfile={userProfile} />
      </div>
    </div>
  )
}
