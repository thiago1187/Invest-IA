import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { ProfileAssessment } from "@/components/ProfileAssessment"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { TrendingUp, Brain, Target } from "lucide-react"

export default function Simulado() {
  const [showAssessment, setShowAssessment] = useState(false)
  const navigate = useNavigate()

  const handleStartAssessment = () => {
    setShowAssessment(true)
  }

  const handleCompleteAssessment = (profile: "conservador" | "moderado" | "agressivo") => {
    // Salvar perfil no localStorage ou enviar para API
    localStorage.setItem("userProfile", profile)
    navigate("/dashboard")
  }

  const handleCancelAssessment = () => {
    setShowAssessment(false)
  }

  const handleSkip = () => {
    navigate("/dashboard")
  }

  if (showAssessment) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <div className="w-full max-w-2xl">
          <ProfileAssessment 
            onComplete={handleCompleteAssessment}
            onCancel={handleCancelAssessment}
          />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <div className="w-full max-w-2xl space-y-8">
        {/* Header */}
        <div className="text-center space-y-4">
          <div className="inline-flex items-center space-x-2 mb-4">
            <div className="p-2 rounded-xl bg-gradient-primary">
              <TrendingUp className="h-8 w-8 text-primary-foreground" />
            </div>
            <h1 className="text-3xl font-bold bg-gradient-primary bg-clip-text text-transparent">
              InvestIA
            </h1>
          </div>
          <h2 className="text-2xl font-bold">
            Descubra seu Perfil de Investidor
          </h2>
          <p className="text-muted-foreground text-lg">
            Responda algumas perguntas para que possamos personalizar suas recomendações de investimento
          </p>
        </div>

        {/* Benefits */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card className="border-border/50 bg-gradient-surface shadow-large">
            <CardContent className="p-6 text-center">
              <Brain className="h-8 w-8 text-primary mx-auto mb-3" />
              <h3 className="font-semibold mb-2">Análise Personalizada</h3>
              <p className="text-sm text-muted-foreground">
                Algoritmo avançado para determinar seu perfil ideal
              </p>
            </CardContent>
          </Card>

          <Card className="border-border/50 bg-gradient-surface shadow-large">
            <CardContent className="p-6 text-center">
              <Target className="h-8 w-8 text-secondary mx-auto mb-3" />
              <h3 className="font-semibold mb-2">Recomendações Precisas</h3>
              <p className="text-sm text-muted-foreground">
                Sugestões de investimento baseadas no seu perfil
              </p>
            </CardContent>
          </Card>

          <Card className="border-border/50 bg-gradient-surface shadow-large">
            <CardContent className="p-6 text-center">
              <TrendingUp className="h-8 w-8 text-warning mx-auto mb-3" />
              <h3 className="font-semibold mb-2">Estratégia Otimizada</h3>
              <p className="text-sm text-muted-foreground">
                Portfolio balanceado para seus objetivos
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Action Card */}
        <Card className="border-border/50 bg-gradient-surface shadow-large">
          <CardHeader className="text-center">
            <CardTitle className="text-xl">Questionário de Perfil</CardTitle>
            <CardDescription>
              São apenas 7 perguntas rápidas que levarão cerca de 3 minutos
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              <Button
                variant="outline"
                onClick={handleSkip}
                className="flex-1 sm:flex-none"
              >
                Pular por agora
              </Button>
              <Button
                onClick={handleStartAssessment}
                className="flex-1 sm:flex-none bg-gradient-primary hover:opacity-90"
              >
                Iniciar Questionário
              </Button>
            </div>
            <p className="text-xs text-muted-foreground text-center">
              Você pode refazer este questionário a qualquer momento em seu perfil
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}