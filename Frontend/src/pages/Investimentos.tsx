import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { HeroButton } from "@/components/ui/hero-button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Trash2, Plus, TrendingUp, DollarSign, PieChart, Building } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { Header } from "@/components/Header"

interface Investment {
  id: string
  tipo: string
  nome: string
  valor: string
}

const tiposInvestimento = [
  { value: "acoes", label: "Ações", icon: TrendingUp },
  { value: "fiis", label: "Fundos Imobiliários", icon: Building },
  { value: "renda-fixa", label: "Renda Fixa", icon: DollarSign },
  { value: "fundos", label: "Fundos de Investimento", icon: PieChart },
  { value: "cripto", label: "Criptomoedas", icon: TrendingUp },
]

export default function Investimentos() {
  const [investments, setInvestments] = useState<Investment[]>([
    { id: "1", tipo: "", nome: "", valor: "" }
  ])
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { toast } = useToast()

  const addInvestment = () => {
    const newInvestment: Investment = {
      id: Date.now().toString(),
      tipo: "",
      nome: "",
      valor: ""
    }
    setInvestments(prev => [...prev, newInvestment])
  }

  const removeInvestment = (id: string) => {
    if (investments.length > 1) {
      setInvestments(prev => prev.filter(inv => inv.id !== id))
    }
  }

  const updateInvestment = (id: string, field: keyof Investment, value: string) => {
    setInvestments(prev => prev.map(inv => 
      inv.id === id ? { ...inv, [field]: value } : inv
    ))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Validar investimentos
    const validInvestments = investments.filter(inv => 
      inv.tipo && inv.nome && inv.valor
    )

    if (validInvestments.length === 0) {
      toast({
        title: "Erro",
        description: "Adicione pelo menos um investimento válido.",
        variant: "destructive"
      })
      return
    }

    setIsLoading(true)

    // Simular salvamento
    setTimeout(() => {
      // Salvar investimentos (simulado)
      localStorage.setItem("userInvestments", JSON.stringify(validInvestments))
      
      toast({
        title: "Investimentos cadastrados!",
        description: "Agora você pode acessar seu dashboard personalizado.",
      })
      
      navigate("/dashboard")
      setIsLoading(false)
    }, 1500)
  }

  const handleSkip = () => {
    navigate("/dashboard")
  }

  const getTypeIcon = (tipo: string) => {
    const typeData = tiposInvestimento.find(t => t.value === tipo)
    if (typeData) {
      const Icon = typeData.icon
      return <Icon className="h-4 w-4" />
    }
    return <DollarSign className="h-4 w-4" />
  }

  const totalValue = investments
    .filter(inv => inv.valor)
    .reduce((sum, inv) => sum + parseFloat(inv.valor.replace(/[^0-9,]/g, '').replace(',', '.') || '0'), 0)

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="p-4">
        {/* Header */}
        <div className="max-w-4xl mx-auto mb-8">
          <div className="flex items-center space-x-3 mb-6">
            <div className="p-2 rounded-xl bg-gradient-primary">
              <TrendingUp className="h-6 w-6 text-primary-foreground" />
            </div>
            <div>
              <h1 className="text-3xl font-bold">Seus Investimentos</h1>
              <p className="text-muted-foreground">
                Cadastre seus investimentos atuais para recomendações personalizadas
              </p>
            </div>
          </div>
        </div>

      <div className="max-w-4xl mx-auto space-y-6">
        {/* Summary Card */}
        <Card className="bg-gradient-surface border-border/50">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Valor Total Cadastrado</p>
                <p className="text-3xl font-bold text-foreground">
                  {new Intl.NumberFormat('pt-BR', {
                    style: 'currency',
                    currency: 'BRL'
                  }).format(totalValue)}
                </p>
              </div>
              <div className="text-right">
                <p className="text-sm text-muted-foreground">Investimentos</p>
                <p className="text-2xl font-semibold text-foreground">
                  {investments.filter(inv => inv.tipo && inv.nome && inv.valor).length}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Form */}
        <Card className="bg-gradient-surface border-border/50 shadow-large">
          <CardHeader>
            <CardTitle>Cadastrar Investimentos</CardTitle>
            <CardDescription>
              Adicione seus investimentos atuais. Você pode pular esta etapa e adicionar depois.
            </CardDescription>
          </CardHeader>

          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-4">
                {investments.map((investment, index) => (
                  <div key={investment.id} className="p-4 border border-border/50 rounded-lg bg-surface">
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-lg font-semibold">Investimento {index + 1}</h3>
                      {investments.length > 1 && (
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon"
                          onClick={() => removeInvestment(investment.id)}
                          className="text-destructive hover:text-destructive"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      )}
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor={`tipo-${investment.id}`}>Tipo</Label>
                        <Select
                          value={investment.tipo}
                          onValueChange={(value) => updateInvestment(investment.id, "tipo", value)}
                        >
                          <SelectTrigger className="bg-background border-border/50">
                            <SelectValue placeholder="Selecione o tipo" />
                          </SelectTrigger>
                          <SelectContent>
                            {tiposInvestimento.map((tipo) => (
                              <SelectItem key={tipo.value} value={tipo.value}>
                                <div className="flex items-center space-x-2">
                                  <tipo.icon className="h-4 w-4" />
                                  <span>{tipo.label}</span>
                                </div>
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor={`nome-${investment.id}`}>Nome/Código</Label>
                        <Input
                          id={`nome-${investment.id}`}
                          placeholder="Ex: VALE3, Tesouro Selic"
                          value={investment.nome}
                          onChange={(e) => updateInvestment(investment.id, "nome", e.target.value)}
                          className="bg-background border-border/50"
                        />
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor={`valor-${investment.id}`}>Valor Investido</Label>
                        <div className="relative">
                          <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                          <Input
                            id={`valor-${investment.id}`}
                            placeholder="0,00"
                            value={investment.valor}
                            onChange={(e) => updateInvestment(investment.id, "valor", e.target.value)}
                            className="bg-background border-border/50 pl-10"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <Button
                type="button"
                variant="outline"
                onClick={addInvestment}
                className="w-full"
              >
                <Plus className="mr-2 h-4 w-4" />
                Adicionar Investimento
              </Button>

              <div className="flex flex-col sm:flex-row gap-4 pt-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleSkip}
                  className="flex-1"
                >
                  Pular por agora
                </Button>
                
                <HeroButton
                  type="submit"
                  className="flex-1"
                  disabled={isLoading}
                >
                  {isLoading ? "Salvando..." : "Salvar e Continuar"}
                </HeroButton>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Investment Types Info */}
        <Card className="bg-gradient-surface border-border/50">
          <CardHeader>
            <CardTitle className="text-lg">Tipos de Investimento</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {tiposInvestimento.map((tipo) => (
                <div key={tipo.value} className="flex items-center space-x-3 p-3 bg-surface rounded-lg">
                  <tipo.icon className="h-5 w-5 text-primary" />
                  <span className="text-sm font-medium">{tipo.label}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
      </div>
    </div>
  )
}