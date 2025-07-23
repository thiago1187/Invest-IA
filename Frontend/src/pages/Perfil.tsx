import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { HeroButton } from "@/components/ui/hero-button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Header } from "@/components/Header"
import { ProfileAssessment } from "@/components/ProfileAssessment"
import { useAuth } from "@/contexts/AuthContext"
import { investimentoService, InvestimentoResponse } from "@/lib/api"
import { 
  User, 
  Calendar, 
  TrendingUp, 
  Target, 
  Brain, 
  Edit,
  CheckCircle,
  FileText,
  DollarSign,
  PieChart,
  Activity
} from "lucide-react"
import { useNavigate } from "react-router-dom"

export default function Perfil() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [userProfile, setUserProfile] = useState<"conservador" | "moderado" | "agressivo">("moderado")
  const [showAssessment, setShowAssessment] = useState(false)
  const [investimentos, setInvestimentos] = useState<InvestimentoResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  
  // Dados reais do usuário
  const userData = {
    name: user?.nome || "Usuário",
    email: user?.email || "usuario@investia.com",
    joinDate: user?.dataCriacao ? new Date(user.dataCriacao).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' }) : "Janeiro 2024",
    totalInvestments: investimentos.reduce((sum, inv) => sum + inv.valorTotalAtual, 0),
    totalInvested: investimentos.reduce((sum, inv) => sum + inv.valorTotalInvestido, 0),
    totalProfit: investimentos.reduce((sum, inv) => sum + inv.lucroPreju, 0),
    monthlyGoal: parseFloat(localStorage.getItem("monthlyInvestmentGoal") || "2000"),
    currentProgress: 0, // Will be calculated
    riskTolerance: userProfile,
    totalAssets: investimentos.length,
    totalRecommendations: parseInt(localStorage.getItem("totalRecommendations") || "12"),
    followedRecommendations: parseInt(localStorage.getItem("followedRecommendations") || "8")
  }

  useEffect(() => {
    const savedProfile = localStorage.getItem("userProfile") as "conservador" | "moderado" | "agressivo"
    if (savedProfile) {
      setUserProfile(savedProfile)
    }
    
    carregarInvestimentos()
  }, [])

  const carregarInvestimentos = async () => {
    try {
      setIsLoading(true)
      const response = await investimentoService.listar()
      setInvestimentos(response.data.content)
    } catch (error) {
      console.error('Erro ao carregar investimentos:', error)
      setInvestimentos([])
    } finally {
      setIsLoading(false)
    }
  }

  const handleAssessmentComplete = (newProfile: "conservador" | "moderado" | "agressivo") => {
    setUserProfile(newProfile)
    setShowAssessment(false)
  }

  // Calcular progresso mensal baseado em investimentos do mês atual
  const calculateMonthlyProgress = () => {
    const currentMonth = new Date().getMonth()
    const currentYear = new Date().getFullYear()
    
    const monthlyInvestments = investimentos.filter(inv => {
      const investDate = new Date(inv.dataUltimaAtualizacao || inv.dataCompra || Date.now())
      return investDate.getMonth() === currentMonth && investDate.getFullYear() === currentYear
    })
    
    const monthlyTotal = monthlyInvestments.reduce((sum, inv) => sum + inv.valorTotalInvestido, 0)
    return userData.monthlyGoal > 0 ? Math.min((monthlyTotal / userData.monthlyGoal) * 100, 100) : 0
  }

  // Atualizar progresso mensal
  userData.currentProgress = calculateMonthlyProgress()

  if (!user) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center space-y-4">
          <User className="h-16 w-16 text-muted-foreground mx-auto" />
          <h2 className="text-xl font-semibold">Acesso Restrito</h2>
          <p className="text-muted-foreground">Faça login para acessar seu perfil</p>
        </div>
      </div>
    )
  }


  const getProfileDescription = (profile: string) => {
    switch (profile) {
      case "conservador":
        return "Você prefere investimentos seguros com menor volatilidade. Foca em preservação de capital."
      case "moderado":
        return "Você busca equilibrio entre segurança e rentabilidade, aceitando riscos moderados."
      case "agressivo":
        return "Você está disposto a assumir riscos maiores em busca de rentabilidade superior."
      default:
        return ""
    }
  }

  const getProfileColor = (profile: string) => {
    switch (profile) {
      case "conservador":
        return "bg-success/10 text-success border-success/20"
      case "moderado":
        return "bg-warning/10 text-warning border-warning/20"
      case "agressivo":
        return "bg-destructive/10 text-destructive border-destructive/20"
      default:
        return "bg-secondary/10 text-secondary border-secondary/20"
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header do Perfil */}
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
            <div className="flex items-center space-x-4">
              <Avatar className="h-20 w-20">
                <AvatarFallback className="bg-primary text-primary-foreground text-xl">
                  {userData.name.split(' ').map(n => n[0]).join('')}
                </AvatarFallback>
              </Avatar>
              <div>
                <h1 className="text-3xl font-bold text-foreground">{userData.name}</h1>
                <p className="text-muted-foreground">{userData.email}</p>
                <p className="text-sm text-muted-foreground flex items-center mt-1">
                  <Calendar className="h-4 w-4 mr-1" />
                  Membro desde {userData.joinDate}
                </p>
              </div>
            </div>
            <HeroButton onClick={() => navigate("/configuracoes")}>
              <Edit className="mr-2 h-4 w-4" />
              Editar Perfil
            </HeroButton>
          </div>

          <Tabs defaultValue="overview" className="space-y-6">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="overview">Visão Geral</TabsTrigger>
              <TabsTrigger value="assessment">Perfil de Investidor</TabsTrigger>
            </TabsList>

            <TabsContent value="overview" className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Informações Principais */}
                <div className="lg:col-span-2 space-y-6">
                  {/* Perfil de Investidor - Resumo */}
                  <Card className="bg-gradient-surface border-border/50">
                    <CardHeader>
                      <CardTitle className="flex items-center space-x-2">
                        <Brain className="h-5 w-5" />
                        <span>Perfil de Investidor</span>
                      </CardTitle>
                      <CardDescription>
                        Baseado no seu questionário de suitability
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium">Perfil Atual:</span>
                        <Badge className={`capitalize ${getProfileColor(userProfile)}`}>
                          {userProfile}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {getProfileDescription(userProfile)}
                      </p>
                      <div className="flex items-center justify-between pt-2">
                        <span className="text-sm text-muted-foreground">
                          Última avaliação: {userData.joinDate}
                        </span>
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => setShowAssessment(true)}
                        >
                          Refazer Questionário
                        </Button>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Metas e Progresso */}
                  <Card className="bg-gradient-surface border-border/50">
                    <CardHeader>
                      <CardTitle className="flex items-center space-x-2">
                        <Target className="h-5 w-5" />
                        <span>Metas e Progresso</span>
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="space-y-2">
                        <div className="flex justify-between text-sm">
                          <span>Meta Mensal de Investimento</span>
                          <span className="font-semibold">
                            {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(userData.monthlyGoal)}
                          </span>
                        </div>
                        <Progress value={userData.currentProgress} className="h-2" />
                        <div className="flex justify-between text-xs text-muted-foreground">
                          <span>{userData.currentProgress.toFixed(1)}% atingido este mês</span>
                          <span>
                            {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format((userData.monthlyGoal * userData.currentProgress / 100))} investidos
                          </span>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Estatísticas da Carteira */}
                  <Card className="bg-gradient-surface border-border/50">
                    <CardHeader>
                      <CardTitle className="flex items-center space-x-2">
                        <Activity className="h-5 w-5" />
                        <span>Estatísticas da Carteira</span>
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      {isLoading ? (
                        <div className="space-y-3">
                          <div className="h-8 bg-muted/50 rounded animate-pulse" />
                          <div className="h-8 bg-muted/50 rounded animate-pulse" />
                          <div className="h-4 bg-muted/50 rounded animate-pulse" />
                        </div>
                      ) : (
                        <>
                          <div className="grid grid-cols-2 gap-4">
                            <div className="text-center">
                              <div className="text-2xl font-bold text-primary">
                                {userData.totalAssets}
                              </div>
                              <p className="text-sm text-muted-foreground">
                                Ativos em Carteira
                              </p>
                            </div>
                            <div className="text-center">
                              <div className={`text-2xl font-bold ${userData.totalProfit >= 0 ? 'text-success' : 'text-destructive'}`}>
                                {userData.totalInvested > 0 ? ((userData.totalProfit / userData.totalInvested) * 100).toFixed(1) : '0.0'}%
                              </div>
                              <p className="text-sm text-muted-foreground">
                                Rentabilidade Total
                              </p>
                            </div>
                          </div>
                          <div className="mt-4 pt-4 border-t">
                            <div className="flex justify-between text-sm">
                              <span>Diversificação</span>
                              <span className="font-semibold text-primary">
                                {userData.totalAssets > 5 ? 'Boa' : userData.totalAssets > 2 ? 'Moderada' : 'Baixa'}
                              </span>
                            </div>
                          </div>
                        </>
                      )}
                    </CardContent>
                  </Card>
                </div>

                {/* Sidebar com Resumo */}
                <div className="space-y-6">
                  {/* Resumo Financeiro */}
                  <Card className="bg-gradient-surface border-border/50">
                    <CardHeader>
                      <CardTitle className="flex items-center space-x-2">
                        <TrendingUp className="h-5 w-5" />
                        <span>Resumo</span>
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="space-y-3">
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">Patrimônio Atual</span>
                          <span className="font-semibold">
                            {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(userData.totalInvestments)}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">Total Investido</span>
                          <span className="font-semibold">
                            {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(userData.totalInvested)}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">Lucro/Prejuízo</span>
                          <span className={`font-semibold ${userData.totalProfit >= 0 ? 'text-success' : 'text-destructive'}`}>
                            {userData.totalProfit >= 0 ? '+' : ''}{new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(userData.totalProfit)}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">Ativos na Carteira</span>
                          <span className="font-semibold">{userData.totalAssets}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">Perfil</span>
                          <Badge variant="outline" className="capitalize">
                            {userProfile}
                          </Badge>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Ações Rápidas */}
                  <Card className="bg-gradient-surface border-border/50">
                    <CardHeader>
                      <CardTitle>Ações Rápidas</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      <Button 
                        className="w-full justify-start" 
                        variant="outline"
                        onClick={() => navigate("/investimentos")}
                      >
                        <TrendingUp className="mr-2 h-4 w-4" />
                        Atualizar Investimentos
                      </Button>
                      <Button 
                        className="w-full justify-start" 
                        variant="outline"
                        onClick={() => setShowAssessment(true)}
                      >
                        <Brain className="mr-2 h-4 w-4" />
                        Refazer Questionário
                      </Button>
                      <HeroButton 
                        className="w-full justify-start"
                        onClick={() => navigate("/dashboard")}
                      >
                        <Target className="mr-2 h-4 w-4" />
                        Ver Dashboard
                      </HeroButton>
                    </CardContent>
                  </Card>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="assessment" className="space-y-6">
              <Card className="bg-gradient-surface border-border/50">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <FileText className="h-5 w-5" />
                    <span>Questionário de Perfil de Investidor</span>
                  </CardTitle>
                  <CardDescription>
                    Descubra ou atualize seu perfil respondendo ao questionário de suitability
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {showAssessment ? (
                    <ProfileAssessment 
                      onComplete={handleAssessmentComplete}
                      onCancel={() => setShowAssessment(false)}
                    />
                  ) : (
                    <div className="text-center space-y-4">
                      <div className="space-y-2">
                        <h3 className="text-lg font-semibold">Seu Perfil Atual</h3>
                        <Badge className={`capitalize text-base px-4 py-2 ${getProfileColor(userProfile)}`}>
                          {userProfile}
                        </Badge>
                        <p className="text-muted-foreground">
                          {getProfileDescription(userProfile)}
                        </p>
                      </div>
                      <div className="pt-4">
                        <HeroButton onClick={() => setShowAssessment(true)}>
                          <Brain className="mr-2 h-4 w-4" />
                          Refazer Questionário
                        </HeroButton>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  )
}