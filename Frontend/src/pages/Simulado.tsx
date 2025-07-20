import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"
import { TrendingUp, Brain, Target, Loader2, ChevronRight, ChevronLeft } from "lucide-react"
import { useAuth } from "@/contexts/AuthContext"
import { simuladoService, PerguntaSimulado, ResultadoSimulado } from "@/lib/api"
import { toast } from "sonner"
import { useQuery, useMutation } from "@tanstack/react-query"

export default function Simulado() {
  const [currentStep, setCurrentStep] = useState<'intro' | 'questionnaire' | 'result'>('intro')
  const [currentQuestion, setCurrentQuestion] = useState(0)
  const [respostas, setRespostas] = useState<Record<number, string>>({})
  const [resultado, setResultado] = useState<ResultadoSimulado | null>(null)
  const navigate = useNavigate()
  const { user, updateUser } = useAuth()

  // Buscar questões do simulado
  const { data: questoesData, isLoading: loadingQuestoes } = useQuery({
    queryKey: ['simulado-questoes'],
    queryFn: () => simuladoService.obterQuestoes().then(res => res.data),
    enabled: currentStep === 'questionnaire'
  })

  // Mutation para processar respostas
  const processarRespostasMutation = useMutation({
    mutationFn: (respostas: Record<number, string>) => 
      simuladoService.processarRespostas({ respostas }).then(res => res.data),
    onSuccess: (data) => {
      setResultado(data)
      setCurrentStep('result')
      
      // Atualizar perfil do usuário
      if (user) {
        const updatedUser = {
          ...user,
          perfil: {
            tipoPerfil: data.perfil,
            nivelExperiencia: data.nivelExperiencia,
            toleranciaRisco: data.toleranciaRisco
          }
        }
        updateUser(updatedUser)
      }
    },
    onError: (error) => {
      console.error('Erro ao processar respostas:', error)
      toast.error('Erro ao processar respostas do simulado')
    }
  })

  const perguntas = questoesData?.perguntas || []
  const perguntaAtual = perguntas[currentQuestion]
  const totalPerguntas = perguntas.length
  const progresso = totalPerguntas > 0 ? ((currentQuestion + 1) / totalPerguntas) * 100 : 0

  const handleStartAssessment = () => {
    setCurrentStep('questionnaire')
  }

  const handleAnswerSelect = (perguntaId: number, respostaId: string) => {
    setRespostas(prev => ({
      ...prev,
      [perguntaId]: respostaId
    }))
  }

  const handleNextQuestion = () => {
    if (currentQuestion < totalPerguntas - 1) {
      setCurrentQuestion(prev => prev + 1)
    } else {
      // Finalizar questionário
      processarRespostasMutation.mutate(respostas)
    }
  }

  const handlePrevQuestion = () => {
    if (currentQuestion > 0) {
      setCurrentQuestion(prev => prev - 1)
    }
  }

  const handleFinish = () => {
    navigate("/dashboard")
  }

  const canProceed = perguntaAtual && respostas[perguntaAtual.id]

  // Tela de introdução
  if (currentStep === 'intro') {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <div className="w-full max-w-2xl space-y-8">
          <div className="text-center space-y-4">
            <div className="inline-flex items-center space-x-2 mb-4">
              <div className="p-2 rounded-xl bg-gradient-to-r from-primary to-primary/80">
                <Brain className="h-8 w-8 text-primary-foreground" />
              </div>
              <h1 className="text-2xl font-bold">InvestIA</h1>
            </div>
            <h2 className="text-3xl font-bold text-foreground">
              Descubra seu Perfil de Investidor
            </h2>
            <p className="text-muted-foreground text-lg">
              Nossa IA irá analisar suas respostas para criar recomendações personalizadas
            </p>
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <Target className="h-5 w-5" />
                <span>O que você vai descobrir</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="flex items-start space-x-3">
                  <div className="p-1 rounded-full bg-primary/10">
                    <TrendingUp className="h-4 w-4 text-primary" />
                  </div>
                  <div>
                    <h4 className="font-medium">Seu perfil de risco</h4>
                    <p className="text-sm text-muted-foreground">Conservador, moderado ou agressivo</p>
                  </div>
                </div>
                <div className="flex items-start space-x-3">
                  <div className="p-1 rounded-full bg-primary/10">
                    <Brain className="h-4 w-4 text-primary" />
                  </div>
                  <div>
                    <h4 className="font-medium">Análise com IA</h4>
                    <p className="text-sm text-muted-foreground">Recomendações personalizadas</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button 
              onClick={handleStartAssessment} 
              size="lg" 
              className="flex items-center space-x-2"
            >
              <Brain className="h-5 w-5" />
              <span>Iniciar Avaliação</span>
              <ChevronRight className="h-4 w-4" />
            </Button>
            <Button 
              variant="outline" 
              onClick={() => navigate("/dashboard")} 
              size="lg"
            >
              Pular por agora
            </Button>
          </div>
        </div>
      </div>
    )
  }

  // Tela do questionário
  if (currentStep === 'questionnaire') {
    if (loadingQuestoes) {
      return (
        <div className="min-h-screen bg-background flex items-center justify-center">
          <div className="flex flex-col items-center space-y-4">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <p className="text-muted-foreground">Carregando questionário...</p>
          </div>
        </div>
      )
    }

    if (!perguntaAtual) {
      return (
        <div className="min-h-screen bg-background flex items-center justify-center">
          <div className="text-center">
            <p className="text-muted-foreground">Erro ao carregar questionário</p>
            <Button onClick={() => setCurrentStep('intro')} className="mt-4">
              Voltar
            </Button>
          </div>
        </div>
      )
    }

    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <div className="w-full max-w-2xl space-y-6">
          <div className="space-y-2">
            <div className="flex justify-between text-sm text-muted-foreground">
              <span>Pergunta {currentQuestion + 1} de {totalPerguntas}</span>
              <span>{Math.round(progresso)}%</span>
            </div>
            <Progress value={progresso} className="h-2" />
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="text-xl">{perguntaAtual.pergunta}</CardTitle>
              <CardDescription>
                Categoria: {perguntaAtual.categoria}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <RadioGroup
                value={respostas[perguntaAtual.id] || ""}
                onValueChange={(value) => handleAnswerSelect(perguntaAtual.id, value)}
                className="space-y-3"
              >
                {perguntaAtual.opcoes.map((opcao) => (
                  <div key={opcao.id} className="flex items-center space-x-2 p-3 rounded-lg border hover:bg-muted/50 transition-colors">
                    <RadioGroupItem value={opcao.id} id={opcao.id} />
                    <Label htmlFor={opcao.id} className="flex-1 cursor-pointer">
                      {opcao.texto}
                    </Label>
                  </div>
                ))}
              </RadioGroup>
            </CardContent>
          </Card>

          <div className="flex justify-between">
            <Button
              variant="outline"
              onClick={handlePrevQuestion}
              disabled={currentQuestion === 0}
              className="flex items-center space-x-2"
            >
              <ChevronLeft className="h-4 w-4" />
              <span>Anterior</span>
            </Button>
            
            <Button
              onClick={handleNextQuestion}
              disabled={!canProceed || processarRespostasMutation.isPending}
              className="flex items-center space-x-2"
            >
              {processarRespostasMutation.isPending ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>Processando...</span>
                </>
              ) : (
                <>
                  <span>{currentQuestion === totalPerguntas - 1 ? 'Finalizar' : 'Próxima'}</span>
                  <ChevronRight className="h-4 w-4" />
                </>
              )}
            </Button>
          </div>
        </div>
      </div>
    )
  }

  // Tela de resultado
  if (currentStep === 'result' && resultado) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <div className="w-full max-w-2xl space-y-6">
          <div className="text-center space-y-4">
            <div className="inline-flex items-center space-x-2 mb-4">
              <div className="p-2 rounded-xl bg-gradient-to-r from-green-500 to-green-600">
                <Target className="h-8 w-8 text-white" />
              </div>
            </div>
            <h2 className="text-3xl font-bold text-foreground">
              Seu Perfil: {resultado.perfil}
            </h2>
            <p className="text-muted-foreground text-lg">
              Análise completa com IA personalizada
            </p>
          </div>

          <div className="grid gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Resumo do seu perfil</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-muted-foreground">Perfil</p>
                    <p className="font-semibold">{resultado.perfil}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Experiência</p>
                    <p className="font-semibold">{resultado.nivelExperiencia}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Pontuação</p>
                    <p className="font-semibold">{resultado.pontuacaoTotal}/15</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Tolerância ao Risco</p>
                    <p className="font-semibold">{(resultado.toleranciaRisco * 10).toFixed(1)}/10</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Características do seu perfil</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {resultado.caracteristicas.map((caracteristica, index) => (
                    <li key={index} className="flex items-center space-x-2">
                      <div className="w-2 h-2 rounded-full bg-primary" />
                      <span>{caracteristica}</span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Recomendações iniciais</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {resultado.recomendacoesIniciais.map((recomendacao, index) => (
                    <li key={index} className="flex items-center space-x-2">
                      <div className="w-2 h-2 rounded-full bg-green-500" />
                      <span>{recomendacao}</span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          </div>

          <div className="text-center">
            <Button onClick={handleFinish} size="lg" className="flex items-center space-x-2">
              <span>Ir para Dashboard</span>
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    )
  }

  return null
}