import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { HeroButton } from "@/components/ui/hero-button"
import { InvestmentCard } from "@/components/ui/investment-card"
import { ChatBotAdvanced } from "@/components/ChatBotAdvanced"
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
  Coins,
  Loader2,
  RefreshCw
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
  Bar,
  Area,
  AreaChart,
  ComposedChart,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar
} from "recharts"
import { dashboardService, DashboardData, PerformanceDetalhada } from "@/lib/api"
import { useAuth } from "@/contexts/AuthContext"
import { toast } from "sonner"

// Função para converter distribuição para formato do gráfico
const convertDistribuicaoParaGrafico = (distribuicao: any) => {
  const data = []
  if (distribuicao.rendaVariavel) {
    data.push({ name: 'Renda Variável', value: distribuicao.rendaVariavel, color: '#3B82F6' })
  }
  if (distribuicao.rendaFixa) {
    data.push({ name: 'Renda Fixa', value: distribuicao.rendaFixa, color: '#8B5CF6' })
  }
  if (distribuicao.fundosImobiliarios) {
    data.push({ name: 'FIIs', value: distribuicao.fundosImobiliarios, color: '#10B981' })
  }
  if (distribuicao.criptomoedas) {
    data.push({ name: 'Cripto', value: distribuicao.criptomoedas, color: '#F59E0B' })
  }
  return data
}

export default function Dashboard() {
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null)
  const [performanceData, setPerformanceData] = useState<PerformanceDetalhada | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [isUpdatingRealTime, setIsUpdatingRealTime] = useState(false)
  const [userProfile, setUserProfile] = useState<"conservador" | "moderado" | "agressivo">("moderado")
  const [autoUpdateEnabled, setAutoUpdateEnabled] = useState(false)
  const { user } = useAuth()
  
  // Carregar dados do dashboard
  useEffect(() => {
    if (user?.id) {
      carregarDashboard()
    }
  }, [user?.id])
  
  const carregarDashboard = async () => {
    if (!user?.id) return
    
    try {
      setIsLoading(true)
      
      // Carregar dados básicos do dashboard
      const [dashboardResponse, performanceResponse] = await Promise.all([
        dashboardService.obterDashboard(user.id),
        dashboardService.obterPerformanceDetalhada(user.id).catch(() => ({ data: null }))
      ])
      
      setDashboardData(dashboardResponse.data)
      if (performanceResponse.data) {
        setPerformanceData(performanceResponse.data)
      }
      
      // Definir perfil baseado nos dados do usuário
      if (user.perfil?.tipoPerfil) {
        setUserProfile(user.perfil.tipoPerfil.toLowerCase() as "conservador" | "moderado" | "agressivo")
      }
    } catch (error: any) {
      console.error('Erro ao carregar dashboard:', error)
      toast.error('Erro ao carregar dados do dashboard')
    } finally {
      setIsLoading(false)
    }
  }
  
  const atualizarDashboard = async () => {
    if (!user?.id) return
    
    try {
      setIsRefreshing(true)
      const [dashboardResponse, performanceResponse] = await Promise.all([
        dashboardService.obterDashboard(user.id),
        dashboardService.obterPerformanceDetalhada(user.id).catch(() => ({ data: null }))
      ])
      
      setDashboardData(dashboardResponse.data)
      if (performanceResponse.data) {
        setPerformanceData(performanceResponse.data)
      }
      toast.success('Dashboard atualizado!')
    } catch (error: any) {
      console.error('Erro ao atualizar dashboard:', error)
      toast.error('Erro ao atualizar dashboard')
    } finally {
      setIsRefreshing(false)
    }
  }
  
  const atualizarTempoReal = async () => {
    if (!user?.id) return
    
    try {
      setIsUpdatingRealTime(true)
      await dashboardService.atualizarDadosTempoReal(user.id)
      await atualizarDashboard()
      toast.success('Dados atualizados em tempo real!')
    } catch (error: any) {
      console.error('Erro ao atualizar tempo real:', error)
      toast.error('Erro ao atualizar dados em tempo real')
    } finally {
      setIsUpdatingRealTime(false)
    }
  }
  
  // Auto-atualização a cada 30 segundos
  useEffect(() => {
    if (autoUpdateEnabled && user?.id) {
      const interval = setInterval(() => {
        atualizarDashboard()
      }, 30000)
      
      return () => clearInterval(interval)
    }
  }, [autoUpdateEnabled, user?.id])
  
  // Fallback data structure para quando não há dados
  const alerts = dashboardData?.alertasRecentes || [
    {
      id: "1",
      tipo: "info",
      titulo: "Bem-vindo ao InvestIA!",
      mensagem: "Configure seus investimentos para receber alertas personalizados.",
      severidade: "info",
      dataHora: new Date().toISOString()
    }
  ]
  
  const recommendationsData = dashboardData?.recomendacoesDestaque || []
  
  // Dados para gráficos
  const portfolioData = dashboardData?.performance?.evolucaoPatrimonio || []
  const distributionData = dashboardData?.distribuicaoAtivos ? 
    convertDistribuicaoParaGrafico(dashboardData.distribuicaoAtivos) : []

  // Se não houver usuário, redirecionar
  if (!user) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-2">Acesso Negado</h2>
          <p className="text-muted-foreground">Faça login para acessar o dashboard</p>
        </div>
      </div>
    )
  }

  const getAlertIcon = (tipo: string) => {
    switch (tipo) {
      case "opportunity":
        return <TrendingUp className="h-4 w-4 text-success" />
      case "warning":
        return <AlertTriangle className="h-4 w-4 text-warning" />
      default:
        return <Bell className="h-4 w-4 text-secondary" />
    }
  }

  const getAlertColor = (tipo: string) => {
    switch (tipo) {
      case "opportunity":
        return "bg-success/10 border-success/20"
      case "warning":
        return "bg-warning/10 border-warning/20"
      default:
        return "bg-secondary/10 border-secondary/20"
    }
  }
  
  if (isLoading) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="container mx-auto px-4 py-8 flex items-center justify-center">
          <div className="text-center">
            <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
            <p className="text-muted-foreground">Carregando dashboard...</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container mx-auto px-4 py-8 pb-24">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header com controles avançados */}
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold">Dashboard Inteligente</h1>
              <p className="text-muted-foreground">Visão geral em tempo real dos seus investimentos</p>
            </div>
            <div className="flex items-center space-x-3">
              {/* Toggle Auto-atualização */}
              <div className="flex items-center space-x-2">
                <span className="text-sm text-muted-foreground">Auto-update</span>
                <Button
                  variant={autoUpdateEnabled ? "default" : "outline"}
                  size="sm"
                  onClick={() => setAutoUpdateEnabled(!autoUpdateEnabled)}
                >
                  {autoUpdateEnabled ? "ON" : "OFF"}
                </Button>
              </div>
              
              {/* Botão Tempo Real */}
              <Button
                variant="secondary"
                onClick={atualizarTempoReal}
                disabled={isUpdatingRealTime}
                className="flex items-center space-x-2"
              >
                <Building className={`h-4 w-4 ${isUpdatingRealTime ? 'animate-pulse' : ''}`} />
                <span>Tempo Real</span>
              </Button>
              
              {/* Botão Atualizar Normal */}
              <Button
                variant="outline"
                onClick={atualizarDashboard}
                disabled={isRefreshing}
                className="flex items-center space-x-2"
              >
                <RefreshCw className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
                <span>Atualizar</span>
              </Button>
            </div>
          </div>
          
          {/* Cards Resumo */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <InvestmentCard
              title="Patrimônio Total"
              value={new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
              }).format(dashboardData?.resumoCarteira?.valorTotal || 0)}
              change={`${dashboardData?.resumoCarteira?.percentualLucroPreju >= 0 ? '+' : ''}${(dashboardData?.resumoCarteira?.percentualLucroPreju || 0).toFixed(2)}%`}
              changeType={dashboardData?.resumoCarteira?.percentualLucroPreju >= 0 ? "positive" : "negative"}
              icon={<DollarSign className="h-5 w-5" />}
            />
            <InvestmentCard
              title="Rentabilidade Mensal"
              value={`${dashboardData?.performance?.rentabilidadeMes >= 0 ? '+' : ''}${(dashboardData?.performance?.rentabilidadeMes || 0).toFixed(2)}%`}
              change={dashboardData?.resumoCarteira?.variacaoMensal >= 0 ? 
                `+${dashboardData.resumoCarteira.variacaoMensal.toFixed(2)}%` : 
                `${dashboardData?.resumoCarteira?.variacaoMensal?.toFixed(2) || '0.00'}%`}
              changeType={dashboardData?.resumoCarteira?.variacaoMensal >= 0 ? "positive" : "negative"}
              icon={<TrendingUp className="h-5 w-5" />}
            />
            <InvestmentCard
              title="Lucro/Prejuízo"
              value={new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
              }).format(dashboardData?.resumoCarteira?.lucroPreju || 0)}
              change={`${dashboardData?.resumoCarteira?.variacaoDiaria >= 0 ? '+' : ''}${(dashboardData?.resumoCarteira?.variacaoDiaria || 0).toFixed(2)}%`}
              changeType={dashboardData?.resumoCarteira?.lucroPreju >= 0 ? "positive" : "negative"}
              icon={<Coins className="h-5 w-5" />}
            />
            <InvestmentCard
              title="Total de Ativos"
              value={dashboardData?.resumoCarteira?.totalAtivos?.toString() || "0"}
              change={`R$ ${new Intl.NumberFormat('pt-BR').format(dashboardData?.resumoCarteira?.valorInvestido || 0)} investido`}
              changeType="neutral"
              icon={<Target className="h-5 w-5" />}
            />
          </div>

          {/* Tabs Content */}
          <Tabs defaultValue="overview" className="space-y-6">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="overview">Visão Geral</TabsTrigger>
              <TabsTrigger value="performance">Performance</TabsTrigger>
              <TabsTrigger value="recommendations">Recomendações</TabsTrigger>
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
                    {portfolioData.length > 0 ? (
                      <ResponsiveContainer width="100%" height={300}>
                        <LineChart data={portfolioData}>
                          <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                          <XAxis dataKey="data" stroke="hsl(var(--muted-foreground))" />
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
                    ) : (
                      <div className="flex items-center justify-center h-[300px]">
                        <p className="text-muted-foreground">Nenhum dado de evolução disponível</p>
                      </div>
                    )}
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
                    {distributionData.length > 0 ? (
                      <>
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
                              <span className="text-sm">{item.name}: {item.value}%</span>
                            </div>
                          ))}
                        </div>
                      </>
                    ) : (
                      <div className="flex items-center justify-center h-[300px]">
                        <p className="text-muted-foreground">Cadastre investimentos para ver a distribuição</p>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </div>

              {/* Resumo Financeiro */}
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle>Resumo Financeiro</CardTitle>
                  <CardDescription>
                    Principais métricas da sua carteira
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                    <div>
                      <p className="text-sm text-muted-foreground">Valor Investido</p>
                      <p className="text-lg font-bold">
                        {new Intl.NumberFormat('pt-BR', {
                          style: 'currency',
                          currency: 'BRL'
                        }).format(dashboardData?.resumoCarteira?.valorInvestido || 0)}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Rentabilidade Anual</p>
                      <p className={`text-lg font-bold ${(dashboardData?.performance?.rentabilidadeAno || 0) >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                        {dashboardData?.performance?.rentabilidadeAno >= 0 ? '+' : ''}{(dashboardData?.performance?.rentabilidadeAno || 0).toFixed(2)}%
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Volatilidade</p>
                      <p className="text-lg font-bold">
                        {(dashboardData?.performance?.volatilidade || 0).toFixed(2)}%
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Performance Detalhada */}
            <TabsContent value="performance" className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Evolução Patrimonial Avançada */}
                <Card className="bg-gradient-surface border-border/50 lg:col-span-2">
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <BarChart3 className="h-5 w-5" />
                      <span>Evolução Patrimonial - 90 Dias</span>
                    </CardTitle>
                    <CardDescription>
                      Histórico detalhado com área preenchida
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    {performanceData?.evolucaoPatrimonio.length > 0 ? (
                      <ResponsiveContainer width="100%" height={350}>
                        <AreaChart data={performanceData.evolucaoPatrimonio.map(p => ({
                          data: new Date(p.data).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' }),
                          valor: Number(p.valor)
                        }))}>
                          <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                          <XAxis dataKey="data" stroke="hsl(var(--muted-foreground))" />
                          <YAxis 
                            stroke="hsl(var(--muted-foreground))"
                            tickFormatter={(value) => new Intl.NumberFormat('pt-BR', {
                              style: 'currency',
                              currency: 'BRL',
                              minimumFractionDigits: 0
                            }).format(value)}
                          />
                          <Tooltip 
                            contentStyle={{
                              backgroundColor: 'hsl(var(--card))',
                              border: '1px solid hsl(var(--border))',
                              borderRadius: '8px'
                            }}
                            formatter={(value: any) => [
                              new Intl.NumberFormat('pt-BR', {
                                style: 'currency',
                                currency: 'BRL'
                              }).format(value),
                              'Patrimônio'
                            ]}
                          />
                          <Area 
                            type="monotone" 
                            dataKey="valor" 
                            stroke="hsl(var(--primary))" 
                            fill="hsl(var(--primary))"
                            fillOpacity={0.3}
                            strokeWidth={3}
                          />
                        </AreaChart>
                      </ResponsiveContainer>
                    ) : (
                      <div className="flex items-center justify-center h-[350px]">
                        <p className="text-muted-foreground">Cadastre investimentos para ver a evolução</p>
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Rentabilidade por Ativo */}
                <Card className="bg-gradient-surface border-border/50">
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <Coins className="h-5 w-5" />
                      <span>Rentabilidade por Ativo</span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    {performanceData?.rentabilidadePorAtivo.length > 0 ? (
                      <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={performanceData.rentabilidadePorAtivo}>
                          <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                          <XAxis dataKey="simbolo" stroke="hsl(var(--muted-foreground))" />
                          <YAxis stroke="hsl(var(--muted-foreground))" />
                          <Tooltip
                            contentStyle={{
                              backgroundColor: 'hsl(var(--card))',
                              border: '1px solid hsl(var(--border))',
                              borderRadius: '8px'
                            }}
                            formatter={(value: any) => [`${Number(value).toFixed(2)}%`, 'Rentabilidade']}
                          />
                          <Bar 
                            dataKey="rentabilidade" 
                            fill="hsl(var(--primary))"
                            radius={[4, 4, 0, 0]}
                          />
                        </BarChart>
                      </ResponsiveContainer>
                    ) : (
                      <div className="flex items-center justify-center h-[300px]">
                        <p className="text-muted-foreground">Nenhum ativo para análise</p>
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Métricas de Risco */}
                <Card className="bg-gradient-surface border-border/50">
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <AlertTriangle className="h-5 w-5" />
                      <span>Métricas de Risco</span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    {performanceData?.metricas ? (
                      <div className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div className="text-center p-4 bg-background rounded-lg">
                            <p className="text-2xl font-bold text-primary">
                              {performanceData.metricas.sharpeRatio?.toFixed(2) || '0.00'}
                            </p>
                            <p className="text-sm text-muted-foreground">Sharpe Ratio</p>
                          </div>
                          <div className="text-center p-4 bg-background rounded-lg">
                            <p className="text-2xl font-bold text-primary">
                              {performanceData.metricas.beta?.toFixed(2) || '0.00'}
                            </p>
                            <p className="text-sm text-muted-foreground">Beta</p>
                          </div>
                          <div className="text-center p-4 bg-background rounded-lg">
                            <p className="text-2xl font-bold text-warning">
                              {performanceData.metricas.volatilidade30d?.toFixed(1) || '0.0'}%
                            </p>
                            <p className="text-sm text-muted-foreground">Volatilidade 30d</p>
                          </div>
                          <div className="text-center p-4 bg-background rounded-lg">
                            <p className="text-2xl font-bold text-secondary">
                              {(performanceData.metricas.correlacaoIbov * 100)?.toFixed(0) || '0'}%
                            </p>
                            <p className="text-sm text-muted-foreground">Correl. IBOV</p>
                          </div>
                        </div>
                      </div>
                    ) : (
                      <div className="flex items-center justify-center h-[200px]">
                        <p className="text-muted-foreground">Dados de risco não disponíveis</p>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </div>
              
              {/* Comparativo com Índices */}
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <TrendingUp className="h-5 w-5" />
                    <span>Performance vs Índices de Mercado</span>
                  </CardTitle>
                  <CardDescription>
                    Comparação da sua carteira com principais indicadores
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {performanceData?.comparativoIndices ? (
                    <ResponsiveContainer width="100%" height={300}>
                      <BarChart data={[
                        { nome: 'Sua Carteira', valor: performanceData.comparativoIndices.carteira, color: '#3B82F6' },
                        { nome: 'IBOVESPA', valor: performanceData.comparativoIndices.ibovespa, color: '#8B5CF6' },
                        { nome: 'IFIX', valor: performanceData.comparativoIndices.ifix, color: '#10B981' },
                        { nome: 'CDI', valor: performanceData.comparativoIndices.cdi, color: '#F59E0B' },
                        { nome: 'IPCA', valor: performanceData.comparativoIndices.ipca, color: '#EF4444' }
                      ]}>
                        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                        <XAxis dataKey="nome" stroke="hsl(var(--muted-foreground))" />
                        <YAxis stroke="hsl(var(--muted-foreground))" />
                        <Tooltip
                          contentStyle={{
                            backgroundColor: 'hsl(var(--card))',
                            border: '1px solid hsl(var(--border))',
                            borderRadius: '8px'
                          }}
                          formatter={(value: any) => [`${Number(value).toFixed(2)}%`, 'Rentabilidade']}
                        />
                        <Bar dataKey="valor" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="flex items-center justify-center h-[300px]">
                      <p className="text-muted-foreground">Dados de comparação não disponíveis</p>
                    </div>
                  )}
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
                    {recommendationsData.length > 0 ? recommendationsData.map((rec, index) => (
                      <div key={rec.id || index} className="flex items-center justify-between p-4 bg-surface rounded-lg">
                        <div className="flex items-center space-x-3">
                          {rec.tipoRecomendacao === 'COMPRA' && <TrendingUp className="h-5 w-5 text-success" />}
                          {rec.tipoRecomendacao === 'VENDA' && <TrendingDown className="h-5 w-5 text-destructive" />}
                          {rec.tipoRecomendacao === 'HOLD' && <DollarSign className="h-5 w-5 text-primary" />}
                          <div>
                            <p className="font-semibold">{rec.ativo?.ticker || 'Ativo'}</p>
                            <p className="text-sm text-muted-foreground">{rec.tipoRecomendacao}</p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <div className="text-right">
                            <p className="text-sm text-muted-foreground">Confiança</p>
                            <p className="font-bold">{rec.confianca}/10</p>
                            <p className="text-xs text-muted-foreground">Alvo: R$ {rec.precoAlvo?.toFixed(2)}</p>
                          </div>
                          <HeroButton size="sm">Ver Detalhes</HeroButton>
                        </div>
                      </div>
                    )) : (
                      <div className="text-center py-8">
                        <Brain className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                        <p className="text-muted-foreground">Cadastre investimentos para receber recomendações personalizadas</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
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
                        className={`p-4 rounded-lg border ${getAlertColor(alert.tipo || 'info')}`}
                      >
                        <div className="flex items-start space-x-3">
                          {getAlertIcon(alert.tipo || 'info')}
                          <div className="flex-1">
                            <h4 className="font-semibold">{alert.titulo}</h4>
                            <p className="text-sm text-muted-foreground mt-1">
                              {alert.mensagem}
                            </p>
                            <p className="text-xs text-muted-foreground mt-2">
                              {new Date(alert.dataHora).toLocaleString('pt-BR')}
                            </p>
                          </div>
                          <Button variant="ghost" size="sm">
                            Ver Mais
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
        <ChatBotAdvanced userProfile={userProfile} />
      </div>
    </div>
  )
}