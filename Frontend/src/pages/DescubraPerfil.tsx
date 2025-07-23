import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { HeroButton } from "@/components/ui/hero-button"
import { Progress } from "@/components/ui/progress"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { Header } from "@/components/Header"
import { ArrowLeft, ArrowRight, User, Shield, Zap, CheckCircle, Sparkles } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { simuladoService } from "@/lib/api"
import { toast as sonnerToast } from "sonner"
import { useAuth } from "@/contexts/AuthContext"
import { useNavigate } from "react-router-dom"

interface Question {
  id: number
  pergunta: string
  categoria: string
  opcoes: {
    id: string
    texto: string
    pontos: number
  }[]
}

interface QuestionarioData {
  perguntas: Question[]
}

interface ResultadoPerfil {
  perfil: 'CONSERVADOR' | 'MODERADO' | 'AGRESSIVO'
  nivelExperiencia: 'INICIANTE' | 'INTERMEDIARIO' | 'AVANCADO' | 'EXPERT'
  pontuacaoTotal: number
  descricaoPerfil: string
  caracteristicas: string[]
  recomendacoesIniciais: string[]
  toleranciaRisco: number
}

export default function DescubraPerfil() {
  const [questions, setQuestions] = useState<Question[]>([])
  const [currentQuestion, setCurrentQuestion] = useState(0)
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [isComplete, setIsComplete] = useState(false)
  const [resultado, setResultado] = useState<ResultadoPerfil | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingQuestions, setIsLoadingQuestions] = useState(true)
  const { toast } = useToast()
  const { markProfileAssessmentComplete } = useAuth()
  const navigate = useNavigate()

  // Carregar perguntas do backend
  useEffect(() => {
    const carregarPerguntas = async () => {
      try {
        setIsLoadingQuestions(true)
        const response = await simuladoService.obterQuestoes()
        setQuestions(response.data.perguntas)
      } catch (error) {
        console.error('Erro ao carregar perguntas:', error)
        sonnerToast.error("Erro ao carregar questionário. Usando perguntas padrão.")
        
        // Fallback para perguntas padrão se API falhar
        setQuestions([
          {
            id: 1,
            pergunta: "Qual é a sua idade?",
            categoria: "demografia",
            opcoes: [
              { id: "18-25", texto: "18 a 25 anos", pontos: 3 },
              { id: "26-35", texto: "26 a 35 anos", pontos: 3 },
              { id: "36-50", texto: "36 a 50 anos", pontos: 2 },
              { id: "51-65", texto: "51 a 65 anos", pontos: 1 },
              { id: "65+", texto: "Mais de 65 anos", pontos: 0 }
            ]
          },
          {
            id: 2,
            pergunta: "Qual é a sua experiência com investimentos?",
            categoria: "experiencia",
            opcoes: [
              { id: "nenhuma", texto: "Nenhuma experiência", pontos: 0 },
              { id: "basica", texto: "Básica (poupança, CDB)", pontos: 1 },
              { id: "intermediaria", texto: "Intermediária (ações, fundos)", pontos: 2 },
              { id: "avancada", texto: "Avançada (derivativos, day trade)", pontos: 3 }
            ]
          },
          {
            id: 3,
            pergunta: "Como você reagiria se seus investimentos perdessem 20% do valor em um mês?",
            categoria: "tolerancia_risco",
            opcoes: [
              { id: "venderia-tudo", texto: "Venderia tudo imediatamente", pontos: 0 },
              { id: "ficaria-preocupado", texto: "Ficaria muito preocupado", pontos: 1 },
              { id: "manteria", texto: "Manteria os investimentos", pontos: 2 },
              { id: "compraria-mais", texto: "Aproveitaria para comprar mais", pontos: 3 }
            ]
          },
          {
            id: 4,
            pergunta: "Qual é o seu objetivo principal ao investir?",
            categoria: "objetivo",
            opcoes: [
              { id: "preservar", texto: "Preservar o capital", pontos: 0 },
              { id: "crescimento-estavel", texto: "Crescimento estável", pontos: 1 },
              { id: "crescimento-moderado", texto: "Crescimento moderado", pontos: 2 },
              { id: "crescimento-alto", texto: "Crescimento alto", pontos: 3 }
            ]
          },
          {
            id: 5,
            pergunta: "Qual é o seu prazo de investimento?",
            categoria: "prazo",
            opcoes: [
              { id: "curto", texto: "Menos de 1 ano", pontos: 0 },
              { id: "medio", texto: "1 a 3 anos", pontos: 1 },
              { id: "longo", texto: "3 a 10 anos", pontos: 2 },
              { id: "muito-longo", texto: "Mais de 10 anos", pontos: 3 }
            ]
          }
        ])
      } finally {
        setIsLoadingQuestions(false)
      }
    }

    carregarPerguntas()
  }, [])

  const progress = questions.length > 0 ? ((currentQuestion + 1) / questions.length) * 100 : 0

  const handleAnswer = (value: string) => {
    setAnswers(prev => ({ ...prev, [questions[currentQuestion].id]: value }))
  }

  const handleNext = () => {
    if (!answers[questions[currentQuestion].id]) {
      toast({
        title: "Resposta obrigatória",
        description: "Por favor, selecione uma opção antes de continuar.",
        variant: "destructive"
      })
      return
    }

    if (currentQuestion < questions.length - 1) {
      setCurrentQuestion(prev => prev + 1)
    } else {
      finalizarQuestionario()
    }
  }

  const handlePrevious = () => {
    if (currentQuestion > 0) {
      setCurrentQuestion(prev => prev - 1)
    }
  }

  const finalizarQuestionario = async () => {
    try {
      setIsLoading(true)
      
      // Preparar dados para envio
      const respostasData = {
        respostas: answers
      }
      
      // Enviar para backend
      const response = await simuladoService.processarRespostas(respostasData)
      setResultado(response.data)
      setIsComplete(true)
      
      sonnerToast.success("Perfil analisado com sucesso!", {
        description: "Confira seu resultado detalhado abaixo."
      })
      
    } catch (error) {
      console.error('Erro ao processar respostas:', error)
      
      // Fallback: calcular localmente
      const totalPoints = questions.reduce((sum, question) => {
        const answer = answers[question.id]
        const option = question.opcoes.find(opt => opt.id === answer)
        return sum + (option?.pontos || 0)
      }, 0)
      
      let perfil: 'CONSERVADOR' | 'MODERADO' | 'AGRESSIVO' = 'MODERADO'
      let descricao = ""
      let caracteristicas: string[] = []
      
      if (totalPoints <= 5) {
        perfil = 'CONSERVADOR'
        descricao = "Você prioriza a segurança e preservação do capital."
        caracteristicas = [
          "Foco na preservação do capital",
          "Baixa tolerância ao risco",
          "Preferência por renda fixa",
          "Retornos estáveis e previsíveis"
        ]
      } else if (totalPoints <= 10) {
        perfil = 'MODERADO'
        descricao = "Você busca equilíbrio entre segurança e rentabilidade."
        caracteristicas = [
          "Equilíbrio entre risco e retorno",
          "Diversificação balanceada",
          "Mix de renda fixa e variável",
          "Crescimento consistente"
        ]
      } else {
        perfil = 'AGRESSIVO'
        descricao = "Você busca altos retornos e aceita maiores riscos."
        caracteristicas = [
          "Foco em altos retornos",
          "Alta tolerância ao risco",
          "Preferência por renda variável",
          "Crescimento acelerado"
        ]
      }
      
      setResultado({
        perfil,
        nivelExperiencia: 'INTERMEDIARIO',
        pontuacaoTotal: totalPoints,
        descricaoPerfil: descricao,
        caracteristicas,
        recomendacoesIniciais: [
          "Diversifique seus investimentos",
          "Mantenha uma reserva de emergência",
          "Revise periodicamente sua carteira"
        ],
        toleranciaRisco: Math.round((totalPoints / 15) * 10)
      })
      
      setIsComplete(true)
      
      sonnerToast.error("Erro no servidor, mas resultado calculado localmente")
    } finally {
      setIsLoading(false)
    }
  }

  const getProfileInfo = () => {
    if (!resultado) return null
    
    switch (resultado.perfil) {
      case 'CONSERVADOR':
        return {
          icon: <Shield className="h-12 w-12 text-success" />,
          title: "Perfil Conservador",
          color: "text-success",
          bgColor: "bg-success/10"
        }
      case 'MODERADO':
        return {
          icon: <User className="h-12 w-12 text-secondary" />,
          title: "Perfil Moderado", 
          color: "text-secondary",
          bgColor: "bg-secondary/10"
        }
      case 'AGRESSIVO':
        return {
          icon: <Zap className="h-12 w-12 text-warning" />,
          title: "Perfil Agressivo",
          color: "text-warning", 
          bgColor: "bg-warning/10"
        }
      default:
        return null
    }
  }

  const reiniciarQuestionario = () => {
    setCurrentQuestion(0)
    setAnswers({})
    setIsComplete(false)
    setResultado(null)
    setIsLoading(false)
  }

  const completarTeste = () => {
    // Marcar como completado no localStorage
    markProfileAssessmentComplete()
    
    sonnerToast.success("Perfil configurado!", {
      description: "Agora você pode acessar todas as funcionalidades do InvestIA."
    })
    
    // Redirecionar para dashboard
    navigate('/dashboard')
  }

  if (isLoadingQuestions) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="container mx-auto px-4 py-8">
          <div className="max-w-2xl mx-auto">
            <div className="text-center space-y-4">
              <Sparkles className="h-16 w-16 text-primary mx-auto animate-pulse" />
              <h2 className="text-2xl font-semibold">Carregando questionário...</h2>
              <p className="text-muted-foreground">Preparando suas perguntas personalizadas</p>
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (isComplete && resultado) {
    const profileInfo = getProfileInfo()
    
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="container mx-auto px-4 py-8">
          <div className="max-w-4xl mx-auto space-y-6">
            <div className="text-center space-y-4">
              <h1 className="text-3xl font-bold">Seu Perfil de Investidor</h1>
              <p className="text-muted-foreground">
                Baseado em suas respostas, identificamos seu perfil ideal
              </p>
            </div>

            <Card className={`${profileInfo?.bgColor} border-border/50 shadow-large`}>
              <CardHeader className="text-center space-y-4">
                <div className="flex justify-center">
                  {profileInfo?.icon}
                </div>
                <CardTitle className={`text-2xl ${profileInfo?.color}`}>
                  {profileInfo?.title}
                </CardTitle>
                <CardDescription className="text-base">
                  {resultado.descricaoPerfil}
                </CardDescription>
              </CardHeader>
              
              <CardContent className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-3">
                    <h4 className="font-semibold">Características do seu perfil:</h4>
                    {resultado.caracteristicas.map((caracteristica, index) => (
                      <div key={index} className="flex items-center space-x-3 p-3 bg-background/50 rounded-lg">
                        <CheckCircle className="w-4 h-4 text-primary flex-shrink-0" />
                        <span className="text-sm">{caracteristica}</span>
                      </div>
                    ))}
                  </div>
                  
                  <div className="space-y-3">
                    <h4 className="font-semibold">Recomendações iniciais:</h4>
                    {resultado.recomendacoesIniciais.map((recomendacao, index) => (
                      <div key={index} className="flex items-center space-x-3 p-3 bg-background/50 rounded-lg">
                        <CheckCircle className="w-4 h-4 text-secondary flex-shrink-0" />
                        <span className="text-sm">{recomendacao}</span>
                      </div>
                    ))}
                  </div>
                </div>
                
                <div className="text-center space-y-4">
                  <div className="flex justify-center gap-4 text-sm">
                    <div className="text-center">
                      <p className="text-muted-foreground">Pontuação</p>
                      <p className="font-bold">{resultado.pontuacaoTotal}</p>
                    </div>
                    <div className="text-center">
                      <p className="text-muted-foreground">Tolerância ao Risco</p>
                      <p className="font-bold">{resultado.toleranciaRisco}/10</p>
                    </div>
                    <div className="text-center">
                      <p className="text-muted-foreground">Nível</p>
                      <p className="font-bold">{resultado.nivelExperiencia}</p>
                    </div>
                  </div>
                  
                  <div className="flex gap-3 justify-center">
                    <Button variant="outline" onClick={reiniciarQuestionario}>
                      Refazer Teste
                    </Button>
                    <HeroButton onClick={completarTeste}>
                      Ver Dashboard
                    </HeroButton>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    )
  }

  if (questions.length === 0) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="container mx-auto px-4 py-8">
          <div className="max-w-2xl mx-auto text-center space-y-4">
            <h2 className="text-2xl font-semibold">Erro ao carregar questionário</h2>
            <p className="text-muted-foreground">Tente recarregar a página</p>
            <Button onClick={() => window.location.reload()}>
              Recarregar
            </Button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-3xl mx-auto space-y-6">
          {/* Header */}
          <div className="text-center space-y-4">
            <div className="flex justify-center">
              <Sparkles className="h-12 w-12 text-primary" />
            </div>
            <h1 className="text-3xl font-bold">Descubra seu Perfil de Investidor</h1>
            <p className="text-muted-foreground text-lg">
              Responda algumas perguntas e descubra qual estratégia de investimento combina com você
            </p>
          </div>

          {/* Progress */}
          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span>Progresso do Questionário</span>
              <span>{currentQuestion + 1} de {questions.length}</span>
            </div>
            <Progress value={progress} className="h-2" />
          </div>

          {/* Question */}
          <Card className="bg-gradient-surface border-border/50 shadow-large">
            <CardHeader>
              <CardTitle className="text-xl">
                {questions[currentQuestion]?.pergunta}
              </CardTitle>
              <CardDescription>
                Pergunta {currentQuestion + 1} de {questions.length}
              </CardDescription>
            </CardHeader>
            
            <CardContent className="space-y-6">
              <RadioGroup
                value={answers[questions[currentQuestion]?.id] || ""}
                onValueChange={handleAnswer}
                className="space-y-3"
              >
                {questions[currentQuestion]?.opcoes.map((opcao) => (
                  <div key={opcao.id} className="flex items-center space-x-3">
                    <RadioGroupItem value={opcao.id} id={opcao.id} />
                    <Label 
                      htmlFor={opcao.id}
                      className="flex-1 cursor-pointer p-3 rounded-lg hover:bg-surface transition-smooth"
                    >
                      {opcao.texto}
                    </Label>
                  </div>
                ))}
              </RadioGroup>
              
              <div className="flex justify-between">
                <Button
                  variant="outline"
                  onClick={currentQuestion === 0 ? () => window.history.back() : handlePrevious}
                >
                  <ArrowLeft className="mr-2 h-4 w-4" />
                  {currentQuestion === 0 ? "Voltar" : "Anterior"}
                </Button>
                
                <HeroButton onClick={handleNext} disabled={isLoading}>
                  {isLoading ? "Processando..." : 
                   currentQuestion === questions.length - 1 ? "Finalizar" : "Próximo"}
                  <ArrowRight className="ml-2 h-4 w-4" />
                </HeroButton>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}