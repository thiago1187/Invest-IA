import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"
import { Badge } from "@/components/ui/badge"
import { Header } from "@/components/Header"
import { 
  Calculator, 
  Target, 
  TrendingUp, 
  Calendar,
  DollarSign,
  PieChart,
  BarChart3,
  Coins
} from "lucide-react"
import { Checkbox } from "@/components/ui/checkbox"
import { toast } from "sonner"

interface ResultadoSimulacao {
  tempoParaMeta: number
  valorFinal: number
  totalInvestido: number
  rendimentoTotal: number
  valorMensal: number
  detalhamento: {
    ano: number
    valorInvestido: number
    rendimento: number
    saldoTotal: number
  }[]
}

export default function Simuladores() {
  // Estados para Simulador 1: Tempo para atingir meta
  const [valorMeta, setValorMeta] = useState("")
  const [valorMensal1, setValorMensal1] = useState("")
  const [taxaJuros1, setTaxaJuros1] = useState("14.25") // Taxa Selic atual (Julho 2025)
  const [comAporteInicial1, setComAporteInicial1] = useState(false)
  const [aporteInicial1, setAporteInicial1] = useState("")
  const [resultado1, setResultado1] = useState<ResultadoSimulacao | null>(null)
  
  // Estados para Simulador 2: Quanto investir mensalmente
  const [valorMeta2, setValorMeta2] = useState("")
  const [tempoMeta, setTempoMeta] = useState("")
  const [taxaJuros2, setTaxaJuros2] = useState("14.25") // Taxa Selic atual (Julho 2025)
  const [comAporteInicial2, setComAporteInicial2] = useState(false)
  const [aporteInicial2, setAporteInicial2] = useState("")
  const [resultado2, setResultado2] = useState<ResultadoSimulacao | null>(null)
  
  // Estados para Simulador 3: Aporte inicial + mensal
  const [aporteInicial, setAporteInicial] = useState("")
  const [aportesMensais, setAportesMensais] = useState("")
  const [periodo, setPeriodo] = useState("")
  const [taxaJuros3, setTaxaJuros3] = useState("14.25") // Taxa Selic atual (Julho 2025)
  const [resultado3, setResultado3] = useState<ResultadoSimulacao | null>(null)

  const formatCurrency = (value: string) => {
    const number = value.replace(/[^\d]/g, '')
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      minimumFractionDigits: 0
    }).format(parseInt(number) || 0)
  }

  const formatNumber = (value: string) => {
    const number = value.replace(/[^\d,]/g, '')
    return number
  }

  // Simulador 1: Calcular tempo para atingir meta
  const calcularTempoParaMeta = () => {
    if (!valorMeta || !valorMensal1 || !taxaJuros1) {
      toast.error("Preencha todos os campos")
      return
    }
    
    if (comAporteInicial1 && !aporteInicial1) {
      toast.error("Preencha o valor do aporte inicial")
      return
    }
    
    const meta = parseFloat(valorMeta.replace(/[^\d]/g, ''))
    const mensal = parseFloat(valorMensal1.replace(/[^\d]/g, ''))
    const inicial = comAporteInicial1 ? parseFloat(aporteInicial1.replace(/[^\d]/g, '')) : 0
    const taxa = parseFloat(taxaJuros1) / 100 / 12 // Taxa mensal
    
    if (meta <= 0 || mensal <= 0 || taxa <= 0 || (comAporteInicial1 && inicial < 0)) {
      toast.error("Valores devem ser positivos")
      return
    }
    
    // Ajustar meta considerando aporte inicial
    const metaAjustada = meta - inicial
    
    if (metaAjustada <= 0) {
      // Se aporte inicial já cobre a meta
      setResultado1({
        tempoParaMeta: 0,
        valorFinal: inicial,
        totalInvestido: inicial,
        rendimentoTotal: 0,
        valorMensal: mensal,
        detalhamento: [{
          ano: 1,
          valorInvestido: inicial,
          rendimento: 0,
          saldoTotal: inicial
        }]
      })
      return
    }
    
    // Fórmula com aporte inicial: FV = PV*(1+r)^n + PMT*(((1+r)^n - 1)/r)
    // Resolvendo para n quando temos PV (aporte inicial)
    let meses = 0
    if (inicial > 0) {
      // Método iterativo para encontrar n quando há aporte inicial
      for (let n = 1; n <= 1200; n++) { // máximo 100 anos
        const valorFuturoInicial = inicial * Math.pow(1 + taxa, n)
        const valorFuturoMensal = mensal * (Math.pow(1 + taxa, n) - 1) / taxa
        const valorTotal = valorFuturoInicial + valorFuturoMensal
        
        if (valorTotal >= meta) {
          meses = n
          break
        }
      }
    } else {
      // Sem aporte inicial, usar fórmula tradicional
      meses = Math.log(1 + (meta * taxa) / mensal) / Math.log(1 + taxa)
    }
    
    if (meses === 0) {
      toast.error("Não foi possível calcular - verifique os valores inseridos")
      return
    }
    
    const anos = Math.ceil(meses / 12)
    
    // Calcular detalhamento ano a ano usando simulação mês a mês precisa
    const detalhamento = []
    let saldoAcumulado = inicial
    let totalInvestidoAcumulado = inicial
    let mesAtual = 0
    
    for (let ano = 1; ano <= anos; ano++) {
      const mesesNoAno = Math.min(12, meses - mesAtual)
      
      for (let mes = 1; mes <= mesesNoAno; mes++) {
        // Aplicar juros no saldo atual
        saldoAcumulado = saldoAcumulado * (1 + taxa)
        // Adicionar aporte mensal
        saldoAcumulado += mensal
        totalInvestidoAcumulado += mensal
        mesAtual++
        
        if (mesAtual >= meses) break
      }
      
      detalhamento.push({
        ano,
        valorInvestido: totalInvestidoAcumulado,
        rendimento: saldoAcumulado - totalInvestidoAcumulado,
        saldoTotal: saldoAcumulado
      })
      
      if (mesAtual >= meses) break
    }
    
    setResultado1({
      tempoParaMeta: meses,
      valorFinal: saldoAcumulado,
      totalInvestido: totalInvestidoAcumulado,
      rendimentoTotal: saldoAcumulado - totalInvestidoAcumulado,
      valorMensal: mensal,
      detalhamento
    })
  }
  
  // Simulador 2: Calcular valor mensal necessário
  const calcularValorMensal = () => {
    if (!valorMeta2 || !tempoMeta || !taxaJuros2) {
      toast.error("Preencha todos os campos")
      return
    }
    
    if (comAporteInicial2 && !aporteInicial2) {
      toast.error("Preencha o valor do aporte inicial")
      return
    }
    
    const meta = parseFloat(valorMeta2.replace(/[^\d]/g, ''))
    const meses = parseInt(tempoMeta) * 12
    const inicial = comAporteInicial2 ? parseFloat(aporteInicial2.replace(/[^\d]/g, '')) : 0
    const taxa = parseFloat(taxaJuros2) / 100 / 12 // Taxa mensal
    
    if (meta <= 0 || meses <= 0 || taxa <= 0 || (comAporteInicial2 && inicial < 0)) {
      toast.error("Valores devem ser positivos")
      return
    }
    
    let valorMensalNecessario = 0
    
    if (inicial > 0) {
      // Com aporte inicial: FV = PV*(1+r)^n + PMT*(((1+r)^n - 1)/r)
      // Resolvendo para PMT: PMT = (FV - PV*(1+r)^n) / (((1+r)^n - 1)/r)
      const valorFuturoInicial = inicial * Math.pow(1 + taxa, meses)
      const metaRestante = meta - valorFuturoInicial
      
      if (metaRestante <= 0) {
        // Aporte inicial já cobre a meta
        valorMensalNecessario = 0
      } else {
        valorMensalNecessario = metaRestante / (((1 + taxa) ** meses - 1) / taxa)
      }
    } else {
      // Sem aporte inicial: PMT = FV / (((1 + r)^n - 1) / r)
      valorMensalNecessario = meta / (((1 + taxa) ** meses - 1) / taxa)
    }
    
    // Calcular detalhamento usando simulação mês a mês precisa
    const detalhamento = []
    let saldoAcumulado = inicial
    let totalInvestidoAcumulado = inicial
    
    const anosTotal = parseInt(tempoMeta)
    
    for (let ano = 1; ano <= anosTotal; ano++) {
      for (let mes = 1; mes <= 12; mes++) {
        // Aplicar juros no saldo atual
        saldoAcumulado = saldoAcumulado * (1 + taxa)
        // Adicionar aporte mensal
        saldoAcumulado += valorMensalNecessario
        totalInvestidoAcumulado += valorMensalNecessario
      }
      
      detalhamento.push({
        ano,
        valorInvestido: totalInvestidoAcumulado,
        rendimento: saldoAcumulado - totalInvestidoAcumulado,
        saldoTotal: saldoAcumulado
      })
    }
    
    setResultado2({
      tempoParaMeta: meses,
      valorFinal: saldoAcumulado,
      totalInvestido: totalInvestidoAcumulado,
      rendimentoTotal: saldoAcumulado - totalInvestidoAcumulado,
      valorMensal: valorMensalNecessario,
      detalhamento
    })
  }
  
  // Simulador 3: Calcular com aporte inicial + mensal
  const calcularComAporteInicial = () => {
    if (!aporteInicial || !aportesMensais || !periodo || !taxaJuros3) {
      toast.error("Preencha todos os campos")
      return
    }
    
    const inicial = parseFloat(aporteInicial.replace(/[^\d]/g, ''))
    const mensal = parseFloat(aportesMensais.replace(/[^\d]/g, ''))
    const meses = parseInt(periodo) * 12
    const taxa = parseFloat(taxaJuros3) / 100 / 12 // Taxa mensal
    
    if (inicial < 0 || mensal <= 0 || meses <= 0 || taxa <= 0) {
      toast.error("Valores devem ser positivos")
      return
    }
    
    // Fórmula: FV = PV*(1+r)^n + PMT*(((1+r)^n - 1)/r)
    const valorFinalInicial = inicial * ((1 + taxa) ** meses)
    const valorFinalMensal = mensal * (((1 + taxa) ** meses - 1) / taxa)
    const valorFinalTotal = valorFinalInicial + valorFinalMensal
    
    // Calcular detalhamento usando simulação mês a mês precisa
    const detalhamento = []
    let saldoAcumulado = inicial
    let totalInvestidoAcumulado = inicial
    
    const anosTotal = parseInt(periodo)
    
    for (let ano = 1; ano <= anosTotal; ano++) {
      for (let mes = 1; mes <= 12; mes++) {
        // Aplicar juros no saldo atual
        saldoAcumulado = saldoAcumulado * (1 + taxa)
        // Adicionar aporte mensal
        saldoAcumulado += mensal
        totalInvestidoAcumulado += mensal
      }
      
      detalhamento.push({
        ano,
        valorInvestido: totalInvestidoAcumulado,
        rendimento: saldoAcumulado - totalInvestidoAcumulado,
        saldoTotal: saldoAcumulado
      })
    }
    
    setResultado3({
      tempoParaMeta: meses,
      valorFinal: saldoAcumulado,
      totalInvestido: totalInvestidoAcumulado,
      rendimentoTotal: saldoAcumulado - totalInvestidoAcumulado,
      valorMensal: mensal,
      detalhamento
    })
  }

  const renderDetalhamento = (resultado: ResultadoSimulacao) => {
    if (!resultado.detalhamento.length) return null
    
    return (
      <div className="mt-4 space-y-2">
        <h4 className="font-medium text-sm">Evolução ano a ano:</h4>
        <div className="max-h-40 overflow-y-auto">
          {resultado.detalhamento.map((item, index) => (
            <div key={index} className="flex justify-between items-center p-2 bg-background/50 rounded text-xs">
              <span>Ano {item.ano}</span>
              <div className="flex space-x-4">
                <span className="text-muted-foreground">
                  Investido: {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', minimumFractionDigits: 0 }).format(item.valorInvestido)}
                </span>
                <span className="text-success">
                  Total: {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', minimumFractionDigits: 0 }).format(item.saldoTotal)}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-6xl mx-auto space-y-6">
          {/* Header */}
          <div className="text-center space-y-4">
            <h1 className="text-3xl font-bold">Simuladores Financeiros</h1>
            <p className="text-muted-foreground text-lg">
              Calculadoras avançadas para planejamento de investimentos com juros compostos
            </p>
          </div>

          {/* Cards dos Simuladores */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Simulador 1: Tempo para Meta */}
            <Card className="bg-gradient-surface border-border/50">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Calendar className="h-5 w-5 text-primary" />
                  <span>Tempo para Meta</span>
                </CardTitle>
                <CardDescription>
                  Quanto tempo para atingir sua meta?
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Meta de Valor</Label>
                  <Input 
                    value={valorMeta}
                    onChange={(e) => setValorMeta(formatCurrency(e.target.value))}
                    placeholder="R$ 100.000"
                  />
                </div>
                <div className="space-y-2">
                  <Label>Valor Mensal</Label>
                  <Input 
                    value={valorMensal1}
                    onChange={(e) => setValorMensal1(formatCurrency(e.target.value))}
                    placeholder="R$ 1.000"
                  />
                </div>
                
                {/* Checkbox e campo de aporte inicial */}
                <div className="space-y-3">
                  <div className="flex items-center space-x-2">
                    <Checkbox 
                      id="aporte-inicial-1"
                      checked={comAporteInicial1}
                      onCheckedChange={setComAporteInicial1}
                    />
                    <Label htmlFor="aporte-inicial-1" className="text-sm font-medium">
                      Incluir aporte inicial
                    </Label>
                  </div>
                  {comAporteInicial1 && (
                    <div className="space-y-2">
                      <Label>Valor Inicial</Label>
                      <Input 
                        value={aporteInicial1}
                        onChange={(e) => setAporteInicial1(formatCurrency(e.target.value))}
                        placeholder="R$ 10.000"
                      />
                    </div>
                  )}
                </div>
                
                <div className="space-y-2">
                  <Label>Taxa de Juros (% ao ano)</Label>
                  <Input 
                    type="number"
                    value={taxaJuros1}
                    onChange={(e) => setTaxaJuros1(e.target.value)}
                    placeholder="12"
                    step="0.1"
                  />
                </div>
                
                <Button onClick={calcularTempoParaMeta} className="w-full">
                  <Calculator className="mr-2 h-4 w-4" />
                  Calcular Tempo
                </Button>
                
                {resultado1 && (
                  <div className="mt-4 p-4 bg-primary/10 rounded-lg space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-muted-foreground">Tempo necessário:</span>
                      <Badge variant="secondary">
                        {resultado1.tempoParaMeta === 0 ? 'Meta já atingida!' : 
                         `${Math.ceil(resultado1.tempoParaMeta / 12)} anos e ${Math.ceil(resultado1.tempoParaMeta % 12)} meses`}
                      </Badge>
                    </div>
                    
                    <div className="grid grid-cols-2 gap-2 text-xs">
                      <div>
                        <p className="text-muted-foreground">Total Investido</p>
                        <p className="font-bold">
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado1.totalInvestido)}
                        </p>
                        {comAporteInicial1 && parseFloat(aporteInicial1.replace(/[^\d]/g, '')) > 0 && (
                          <p className="text-xs text-muted-foreground">
                            (Inicial: {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(parseFloat(aporteInicial1.replace(/[^\d]/g, '')))} + 
                            Aportes: {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado1.totalInvestido - parseFloat(aporteInicial1.replace(/[^\d]/g, '')))})
                          </p>
                        )}
                      </div>
                      <div>
                        <p className="text-muted-foreground">Rendimento</p>
                        <p className="font-bold text-success">
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado1.rendimentoTotal)}
                        </p>
                      </div>
                    </div>
                    
                    <div className="mt-2">
                      <div className="flex justify-between text-xs mb-1">
                        <span>Progresso</span>
                        <span>{((resultado1.rendimentoTotal / resultado1.valorFinal) * 100).toFixed(1)}% de rendimento</span>
                      </div>
                      <Progress value={(resultado1.rendimentoTotal / resultado1.valorFinal) * 100} />
                    </div>
                    
                    {renderDetalhamento(resultado1)}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Simulador 2: Valor Mensal Necessário */}
            <Card className="bg-gradient-surface border-border/50">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <DollarSign className="h-5 w-5 text-success" />
                  <span>Valor Mensal</span>
                </CardTitle>
                <CardDescription>
                  Quanto investir por mês?
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Meta de Valor</Label>
                  <Input 
                    value={valorMeta2}
                    onChange={(e) => setValorMeta2(formatCurrency(e.target.value))}
                    placeholder="R$ 1.000.000"
                  />
                </div>
                <div className="space-y-2">
                  <Label>Prazo (anos)</Label>
                  <Input 
                    type="number"
                    value={tempoMeta}
                    onChange={(e) => setTempoMeta(e.target.value)}
                    placeholder="10"
                  />
                </div>
                
                {/* Checkbox e campo de aporte inicial */}
                <div className="space-y-3">
                  <div className="flex items-center space-x-2">
                    <Checkbox 
                      id="aporte-inicial-2"
                      checked={comAporteInicial2}
                      onCheckedChange={setComAporteInicial2}
                    />
                    <Label htmlFor="aporte-inicial-2" className="text-sm font-medium">
                      Incluir aporte inicial
                    </Label>
                  </div>
                  {comAporteInicial2 && (
                    <div className="space-y-2">
                      <Label>Valor Inicial</Label>
                      <Input 
                        value={aporteInicial2}
                        onChange={(e) => setAporteInicial2(formatCurrency(e.target.value))}
                        placeholder="R$ 50.000"
                      />
                    </div>
                  )}
                </div>
                
                <div className="space-y-2">
                  <Label>Taxa de Juros (% ao ano)</Label>
                  <Input 
                    type="number"
                    value={taxaJuros2}
                    onChange={(e) => setTaxaJuros2(e.target.value)}
                    placeholder="12"
                    step="0.1"
                  />
                </div>
                
                <Button onClick={calcularValorMensal} className="w-full">
                  <Target className="mr-2 h-4 w-4" />
                  Calcular Valor
                </Button>
                
                {resultado2 && (
                  <div className="mt-4 p-4 bg-success/10 rounded-lg space-y-2">
                    <div className="text-center">
                      <p className="text-sm text-muted-foreground">Valor mensal necessário:</p>
                      <p className="text-xl font-bold text-success">
                        {resultado2.valorMensal === 0 ? 'R$ 0,00' : 
                         new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado2.valorMensal)}
                      </p>
                      {resultado2.valorMensal === 0 && (
                        <p className="text-sm text-primary font-medium">
                          O aporte inicial já cobre sua meta!
                        </p>
                      )}
                    </div>
                    
                    <div className="grid grid-cols-2 gap-2 text-xs">
                      <div>
                        <p className="text-muted-foreground">Total Investido</p>
                        <p className="font-bold">
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado2.totalInvestido)}
                        </p>
                        {comAporteInicial2 && parseFloat(aporteInicial2.replace(/[^\d]/g, '')) > 0 && (
                          <p className="text-xs text-muted-foreground">
                            (Inicial: {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(parseFloat(aporteInicial2.replace(/[^\d]/g, '')))} + 
                            Aportes: {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado2.totalInvestido - parseFloat(aporteInicial2.replace(/[^\d]/g, '')))})
                          </p>
                        )}
                      </div>
                      <div>
                        <p className="text-muted-foreground">Rendimento</p>
                        <p className="font-bold text-success">
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado2.rendimentoTotal)}
                        </p>
                      </div>
                    </div>
                    
                    <div className="mt-2">
                      <div className="flex justify-between text-xs mb-1">
                        <span>Eficiência</span>
                        <span>{resultado2.totalInvestido > 0 ? ((resultado2.rendimentoTotal / resultado2.totalInvestido) * 100).toFixed(1) : '0.0'}% de retorno</span>
                      </div>
                      <Progress value={Math.min(resultado2.totalInvestido > 0 ? ((resultado2.rendimentoTotal / resultado2.totalInvestido) * 100) : 0, 100)} />
                    </div>
                    
                    {renderDetalhamento(resultado2)}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Simulador 3: Aporte Inicial + Mensal */}
            <Card className="bg-gradient-surface border-border/50">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Coins className="h-5 w-5 text-warning" />
                  <span>Aporte Inicial + Mensal</span>
                </CardTitle>
                <CardDescription>
                  Simule com valor inicial
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Aporte Inicial</Label>
                  <Input 
                    value={aporteInicial}
                    onChange={(e) => setAporteInicial(formatCurrency(e.target.value))}
                    placeholder="R$ 10.000"
                  />
                </div>
                <div className="space-y-2">
                  <Label>Aporte Mensal</Label>
                  <Input 
                    value={aportesMensais}
                    onChange={(e) => setAportesMensais(formatCurrency(e.target.value))}
                    placeholder="R$ 1.000"
                  />
                </div>
                <div className="space-y-2">
                  <Label>Período (anos)</Label>
                  <Input 
                    type="number"
                    value={periodo}
                    onChange={(e) => setPeriodo(e.target.value)}
                    placeholder="5"
                  />
                </div>
                <div className="space-y-2">
                  <Label>Taxa de Juros (% ao ano)</Label>
                  <Input 
                    type="number"
                    value={taxaJuros3}
                    onChange={(e) => setTaxaJuros3(e.target.value)}
                    placeholder="12"
                    step="0.1"
                  />
                </div>
                
                <Button onClick={calcularComAporteInicial} className="w-full">
                  <TrendingUp className="mr-2 h-4 w-4" />
                  Simular
                </Button>
                
                {resultado3 && (
                  <div className="mt-4 p-4 bg-warning/10 rounded-lg space-y-2">
                    <div className="text-center">
                      <p className="text-sm text-muted-foreground">Valor final:</p>
                      <p className="text-xl font-bold text-warning">
                        {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado3.valorFinal)}
                      </p>
                    </div>
                    
                    <div className="grid grid-cols-2 gap-2 text-xs">
                      <div>
                        <p className="text-muted-foreground">Total Investido</p>
                        <p className="font-bold">
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado3.totalInvestido)}
                        </p>
                      </div>
                      <div>
                        <p className="text-muted-foreground">Rendimento</p>
                        <p className="font-bold text-success">
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(resultado3.rendimentoTotal)}
                        </p>
                      </div>
                    </div>
                    
                    <div className="mt-2">
                      <div className="flex justify-between text-xs mb-1">
                        <span>Multiplicador</span>
                        <span>{(resultado3.valorFinal / resultado3.totalInvestido).toFixed(2)}x</span>
                      </div>
                      <Progress value={Math.min(((resultado3.rendimentoTotal / resultado3.totalInvestido) * 100), 100)} />
                    </div>
                    
                    {renderDetalhamento(resultado3)}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Informações sobre Juros Compostos */}
          <Card className="bg-gradient-surface border-border/50">
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <BarChart3 className="h-5 w-5" />
                <span>Como Funcionam os Cálculos</span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <h4 className="font-semibold text-primary mb-2">Juros Compostos</h4>
                  <p className="text-muted-foreground">
                    Os rendimentos são reinvestidos automaticamente, gerando juros sobre juros. 
                    A fórmula: M = C × (1 + i)^t
                  </p>
                </div>
                <div>
                  <h4 className="font-semibold text-success mb-2">Aportes Mensais</h4>
                  <p className="text-muted-foreground">
                    Investimentos regulares potencializam o crescimento através da disciplina 
                    e do efeito dos juros compostos ao longo do tempo.
                  </p>
                </div>
                <div>
                  <h4 className="font-semibold text-warning mb-2">Taxa Realista</h4>
                  <p className="text-muted-foreground">
                    Considere taxas atuais: Poupança ~6.17%, CDI ~12.14%, SELIC ~14.25% 
                    (médias históricas, não garantias).
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}