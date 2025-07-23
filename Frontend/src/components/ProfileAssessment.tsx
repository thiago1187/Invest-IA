import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { HeroButton } from "@/components/ui/hero-button"
import { Progress } from "@/components/ui/progress"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { ArrowLeft, ArrowRight, User, Shield, Zap, CheckCircle } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { perfilService } from "@/lib/api"

interface Question {
  id: number
  question: string
  options: {
    value: string
    label: string
    points: number
  }[]
}

const questions: Question[] = [
  {
    id: 1,
    question: "Qual é a sua idade?",
    options: [
      { value: "18-25", label: "18 a 25 anos", points: 3 },
      { value: "26-35", label: "26 a 35 anos", points: 3 },
      { value: "36-50", label: "36 a 50 anos", points: 2 },
      { value: "51-65", label: "51 a 65 anos", points: 1 },
      { value: "65+", label: "Mais de 65 anos", points: 0 }
    ]
  },
  {
    id: 2,
    question: "Qual é a sua renda mensal aproximada?",
    options: [
      { value: "ate-3k", label: "Até R$ 3.000", points: 1 },
      { value: "3k-8k", label: "R$ 3.000 a R$ 8.000", points: 2 },
      { value: "8k-15k", label: "R$ 8.000 a R$ 15.000", points: 3 },
      { value: "15k+", label: "Acima de R$ 15.000", points: 3 }
    ]
  },
  {
    id: 3,
    question: "Você possui reserva de emergência?",
    options: [
      { value: "nao", label: "Não tenho reserva", points: 0 },
      { value: "parcial", label: "Tenho menos de 6 meses de gastos", points: 1 },
      { value: "adequada", label: "Tenho de 6 a 12 meses de gastos", points: 2 },
      { value: "ampla", label: "Tenho mais de 12 meses de gastos", points: 3 }
    ]
  },
  {
    id: 4,
    question: "Qual é o seu objetivo principal ao investir?",
    options: [
      { value: "preservar", label: "Preservar o capital", points: 0 },
      { value: "crescimento-estavel", label: "Crescimento estável", points: 1 },
      { value: "crescimento-moderado", label: "Crescimento moderado", points: 2 },
      { value: "crescimento-alto", label: "Crescimento alto", points: 3 }
    ]
  },
  {
    id: 5,
    question: "Qual é o seu prazo de investimento?",
    options: [
      { value: "curto", label: "Menos de 1 ano", points: 0 },
      { value: "medio", label: "1 a 3 anos", points: 1 },
      { value: "longo", label: "3 a 10 anos", points: 2 },
      { value: "muito-longo", label: "Mais de 10 anos", points: 3 }
    ]
  },
  {
    id: 6,
    question: "Como você reagiria se seus investimentos perdessem 20% do valor em um mês?",
    options: [
      { value: "venderia-tudo", label: "Venderia tudo imediatamente", points: 0 },
      { value: "ficaria-preocupado", label: "Ficaria muito preocupado e consideraria vender", points: 1 },
      { value: "manteria", label: "Manteria os investimentos", points: 2 },
      { value: "compraria-mais", label: "Aproveitaria para comprar mais", points: 3 }
    ]
  },
  {
    id: 7,
    question: "Qual é a sua experiência com investimentos?",
    options: [
      { value: "nenhuma", label: "Nenhuma experiência", points: 0 },
      { value: "basica", label: "Básica (poupança, CDB)", points: 1 },
      { value: "intermediaria", label: "Intermediária (ações, fundos)", points: 2 },
      { value: "avancada", label: "Avançada (derivativos, day trade)", points: 3 }
    ]
  }
]

interface ProfileAssessmentProps {
  onComplete: (profile: "conservador" | "moderado" | "agressivo") => void
  onCancel: () => void
}

export function ProfileAssessment({ onComplete, onCancel }: ProfileAssessmentProps) {
  const [currentQuestion, setCurrentQuestion] = useState(0)
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [isComplete, setIsComplete] = useState(false)
  const [profile, setProfile] = useState<"conservador" | "moderado" | "agressivo" | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const { toast } = useToast()

  const progress = ((currentQuestion + 1) / questions.length) * 100

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
      calculateProfile()
    }
  }

  const handlePrevious = () => {
    if (currentQuestion > 0) {
      setCurrentQuestion(prev => prev - 1)
    }
  }

  const calculateProfile = () => {
    const totalPoints = questions.reduce((sum, question) => {
      const answer = answers[question.id]
      const option = question.options.find(opt => opt.value === answer)
      return sum + (option?.points || 0)
    }, 0)

    let calculatedProfile: "conservador" | "moderado" | "agressivo"
    
    if (totalPoints <= 7) {
      calculatedProfile = "conservador"
    } else if (totalPoints <= 14) {
      calculatedProfile = "moderado"
    } else {
      calculatedProfile = "agressivo"
    }

    setProfile(calculatedProfile)
    setIsComplete(true)
  }

  const getProfileInfo = () => {
    switch (profile) {
      case "conservador":
        return {
          icon: <Shield className="h-12 w-12 text-success" />,
          title: "Perfil Conservador",
          description: "Você prioriza a segurança e preservação do capital. Prefere investimentos com menor risco e retorno mais previsível.",
          characteristics: [
            "Foco na preservação do capital",
            "Baixa tolerância ao risco",
            "Investimentos em renda fixa",
            "Retornos estáveis e previsíveis"
          ]
        }
      case "moderado":
        return {
          icon: <User className="h-12 w-12 text-secondary" />,
          title: "Perfil Moderado",
          description: "Você busca equilibrio entre segurança e rentabilidade. Aceita alguns riscos para obter retornos maiores.",
          characteristics: [
            "Equilíbrio entre risco e retorno",
            "Diversificação balanceada",
            "Mix de renda fixa e variável",
            "Crescimento consistente"
          ]
        }
      case "agressivo":
        return {
          icon: <Zap className="h-12 w-12 text-warning" />,
          title: "Perfil Agressivo",
          description: "Você busca altos retornos e está disposto a assumir maiores riscos. Foca no crescimento de longo prazo.",
          characteristics: [
            "Foco em altos retornos",
            "Alta tolerância ao risco",
            "Investimentos em renda variável",
            "Crescimento acelerado"
          ]
        }
      default:
        return null
    }
  }

  const handleFinish = async () => {
    if (profile) {
      try {
        setIsLoading(true)
        
        // Mapear perfil para formato do backend
        const perfilMapeado = {
          conservador: "CONSERVADOR",
          moderado: "MODERADO", 
          agressivo: "AGRESSIVO"
        }[profile] || "MODERADO"
        
        // Calcular nível de experiência baseado nas respostas
        const experienciaResposta = answers[7] // Questão sobre experiência
        const nivelExperiencia = {
          "nenhuma": "INICIANTE",
          "basica": "INICIANTE", 
          "intermediaria": "INTERMEDIARIO",
          "avancada": "AVANCADO"
        }[experienciaResposta] || "INICIANTE"
        
        // Calcular tolerância ao risco (0-10)
        const totalPoints = questions.reduce((sum, question) => {
          const answer = answers[question.id]
          const option = question.options.find(opt => opt.value === answer)
          return sum + (option?.points || 0)
        }, 0)
        
        const toleranciaRisco = Math.round((totalPoints / 21) * 10) // Normalizar para 0-10
        
        // Salvar no backend
        await perfilService.salvarAvaliacao({
          tipoPerfil: perfilMapeado,
          nivelExperiencia: nivelExperiencia,
          toleranciaRisco: toleranciaRisco,
          respostasCompletas: answers
        })
        
        // Salvar localmente também para compatibilidade
        localStorage.setItem("userProfile", profile)
        
        onComplete(profile)
        
        toast({
          title: "Perfil atualizado com sucesso!",
          description: "Seu perfil de investidor foi definido no servidor.",
        })
        
      } catch (error) {
        console.error('Erro ao salvar avaliação no backend:', error)
        
        // Fallback: salvar apenas localmente
        localStorage.setItem("userProfile", profile)
        onComplete(profile)
        
        toast({
          title: "Perfil salvo localmente",
          description: "Não foi possível salvar no servidor, mas foi salvo localmente.",
          variant: "destructive"
        })
      } finally {
        setIsLoading(false)
      }
    }
  }

  if (isComplete && profile) {
    const profileInfo = getProfileInfo()
    
    return (
      <Card className="bg-gradient-surface border-border/50 shadow-large">
        <CardHeader className="text-center space-y-4">
          <div className="flex justify-center">
            {profileInfo?.icon}
          </div>
          <CardTitle className="text-2xl">{profileInfo?.title}</CardTitle>
          <CardDescription className="text-base">
            {profileInfo?.description}
          </CardDescription>
        </CardHeader>
        
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {profileInfo?.characteristics.map((characteristic, index) => (
              <div key={index} className="flex items-center space-x-3 p-3 bg-surface rounded-lg">
                <CheckCircle className="w-4 h-4 text-primary flex-shrink-0" />
                <span className="text-sm">{characteristic}</span>
              </div>
            ))}
          </div>
          
          <div className="flex gap-3 justify-center">
            <Button variant="outline" onClick={onCancel}>
              Voltar
            </Button>
            <HeroButton onClick={handleFinish} disabled={isLoading}>
              {isLoading ? "Salvando..." : "Confirmar Perfil"}
            </HeroButton>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-6">
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
            {questions[currentQuestion].question}
          </CardTitle>
        </CardHeader>
        
        <CardContent className="space-y-6">
          <RadioGroup
            value={answers[questions[currentQuestion].id] || ""}
            onValueChange={handleAnswer}
            className="space-y-3"
          >
            {questions[currentQuestion].options.map((option) => (
              <div key={option.value} className="flex items-center space-x-3">
                <RadioGroupItem value={option.value} id={option.value} />
                <Label 
                  htmlFor={option.value}
                  className="flex-1 cursor-pointer p-3 rounded-lg hover:bg-surface transition-smooth"
                >
                  {option.label}
                </Label>
              </div>
            ))}
          </RadioGroup>
          
          <div className="flex justify-between">
            <Button
              variant="outline"
              onClick={currentQuestion === 0 ? onCancel : handlePrevious}
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              {currentQuestion === 0 ? "Cancelar" : "Anterior"}
            </Button>
            
            <HeroButton onClick={handleNext}>
              {currentQuestion === questions.length - 1 ? "Finalizar" : "Próximo"}
              <ArrowRight className="ml-2 h-4 w-4" />
            </HeroButton>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}