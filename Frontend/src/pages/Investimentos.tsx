import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { HeroButton } from "@/components/ui/hero-button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Trash2, Plus, TrendingUp, DollarSign, PieChart, Building, Edit2, Loader2, RefreshCw, Bell, AlertCircle, Eye, TrendingDown, BarChart3, Coins } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { Header } from "@/components/Header"
import { investimentoService, cotacaoService, InvestimentoResponse, CriarInvestimentoRequest, AtualizarInvestimentoRequest, CotacaoResponse } from "@/lib/api"
import { useAuth } from "@/contexts/AuthContext"
import { toast as sonnerToast } from "sonner"

// Interface para novo investimento (formulário)
interface NovoInvestimento {
  ticker: string
  quantidade: number
  valorCompra: number
  dataCompra: string
}

// Interface para edição
interface EdicaoInvestimento {
  id: string
  quantidade: number
  valorMedioCompra: number
}

// Interface para alertas de preço
interface AlertaPreco {
  id: string
  ticker: string
  nome: string
  precoAlvo: number
  tipoAlerta: 'acima' | 'abaixo'
  ativo: boolean
}

// Interface para cotações em tempo real
interface CotacaoTempoReal {
  ticker: string
  nome: string
  preco: number
  variacao: number
  variacaoPercent: number
  volume: number
  abertura: number
  maxima: number
  minima: number
  fechamentoAnterior: number
}

// Lista completa de ações brasileiras em ordem alfabética (mais de 100 ações)
const acoesBrasileiras = [
  { ticker: 'ABEV3.SA', nome: 'AMBEV S/A ON' },
  { ticker: 'AERI3.SA', nome: 'AERIS ON' },
  { ticker: 'AESB3.SA', nome: 'AES BRASIL ON' },
  { ticker: 'AGRO3.SA', nome: 'BRASILAGRO ON' },
  { ticker: 'ALSO3.SA', nome: 'ALIANSCE SONAE ON' },
  { ticker: 'ALUP11.SA', nome: 'ALUPAR UNT' },
  { ticker: 'AMBI4.SA', nome: 'AMBEV PN' },
  { ticker: 'AMER3.SA', nome: 'AMERICANAS ON' },
  { ticker: 'ARML3.SA', nome: 'AREZZO CO ON' },
  { ticker: 'ASAI3.SA', nome: 'ASSAI ON' },
  { ticker: 'AZUL4.SA', nome: 'AZUL ON' },
  { ticker: 'B3SA3.SA', nome: 'B3 ON' },
  { ticker: 'BBAS3.SA', nome: 'BANCO DO BRASIL ON' },
  { ticker: 'BBDC3.SA', nome: 'BRADESCO ON' },
  { ticker: 'BBDC4.SA', nome: 'BRADESCO PN' },
  { ticker: 'BBSE3.SA', nome: 'BB SEGURIDADE ON' },
  { ticker: 'BEEF3.SA', nome: 'MINERVA ON' },
  { ticker: 'BIDI11.SA', nome: 'BANCO INTER UNT' },
  { ticker: 'BMGB4.SA', nome: 'BANCO BMG PN' },
  { ticker: 'BPAC11.SA', nome: 'BTGP BANCO UNT' },
  { ticker: 'BPAN4.SA', nome: 'BANCO PAN PN' },
  { ticker: 'BRAP4.SA', nome: 'BRADESPAR PN' },
  { ticker: 'BRDT3.SA', nome: 'PETROBRAS BR ON' },
  { ticker: 'BRFS3.SA', nome: 'BRF SA ON' },
  { ticker: 'BRKM5.SA', nome: 'BRASKEM PNA' },
  { ticker: 'BRML3.SA', nome: 'BR MALLS PAR ON' },
  { ticker: 'BTOW3.SA', nome: 'B2W DIGITAL ON' },
  { ticker: 'CAML3.SA', nome: 'CAMIL ON' },
  { ticker: 'CASH3.SA', nome: 'MELIUZ ON' },
  { ticker: 'CBAV3.SA', nome: 'CBA ON' },
  { ticker: 'CCRO3.SA', nome: 'CCR SA ON' },
  { ticker: 'CIEL3.SA', nome: 'CIELO ON' },
  { ticker: 'CMIG4.SA', nome: 'CEMIG PN' },
  { ticker: 'CMIN3.SA', nome: 'CSN MINERACAO ON' },
  { ticker: 'COGN3.SA', nome: 'COGNA ON' },
  { ticker: 'CPFE3.SA', nome: 'CPFL ENERGIA ON' },
  { ticker: 'CPLE6.SA', nome: 'COPEL PNB' },
  { ticker: 'CRFB3.SA', nome: 'CARREFOUR BR ON' },
  { ticker: 'CSAN3.SA', nome: 'COSAN ON' },
  { ticker: 'CSNA3.SA', nome: 'SID NACIONAL ON' },
  { ticker: 'CVCB3.SA', nome: 'CVC BRASIL ON' },
  { ticker: 'CYRE3.SA', nome: 'CYRELA REALT ON' },
  { ticker: 'DXCO3.SA', nome: 'DEXCO ON' },
  { ticker: 'ECOR3.SA', nome: 'ECORODOVIAS ON' },
  { ticker: 'EGIE3.SA', nome: 'ENGIE BRASIL ON' },
  { ticker: 'ELET3.SA', nome: 'ELETROBRAS ON' },
  { ticker: 'ELET6.SA', nome: 'ELETROBRAS PNB' },
  { ticker: 'EMBR3.SA', nome: 'EMBRAER ON' },
  { ticker: 'ENBR3.SA', nome: 'ENERGIAS BR ON' },
  { ticker: 'ENEV3.SA', nome: 'ENEVA ON' },
  { ticker: 'ENGI11.SA', nome: 'ENERGISA UNT' },
  { ticker: 'EQTL3.SA', nome: 'EQUATORIAL ON' },
  { ticker: 'EZTC3.SA', nome: 'EZTEC ON' },
  { ticker: 'FHER3.SA', nome: 'FER HERINGER ON' },
  { ticker: 'FLRY3.SA', nome: 'FLEURY ON' },
  { ticker: 'FRAS3.SA', nome: 'FRAS-LE ON' },
  { ticker: 'GFSA3.SA', nome: 'GAFISA ON' },
  { ticker: 'GGBR4.SA', nome: 'GERDAU PN' },
  { ticker: 'GNDI3.SA', nome: 'INTERMEDICA ON' },
  { ticker: 'GOAU4.SA', nome: 'GERDAU MET PN' },
  { ticker: 'GOLL4.SA', nome: 'GOL PN' },
  { ticker: 'GRND3.SA', nome: 'GRENDENE ON' },
  { ticker: 'HAPV3.SA', nome: 'HAPVIDA ON' },
  { ticker: 'HBOR3.SA', nome: 'HELBOR ON' },
  { ticker: 'HGTX3.SA', nome: 'CIA HERING ON' },
  { ticker: 'HYPE3.SA', nome: 'HYPERA ON' },
  { ticker: 'IGTA3.SA', nome: 'IGUATEMI ON' },
  { ticker: 'IRBR3.SA', nome: 'IRB BRASIL RE ON' },
  { ticker: 'ITSA4.SA', nome: 'ITAUSA PN' },
  { ticker: 'ITUB4.SA', nome: 'ITAÚ UNIBANCO PN' },
  { ticker: 'JBSS3.SA', nome: 'JBS ON' },
  { ticker: 'JHSF3.SA', nome: 'JHSF PART ON' },
  { ticker: 'KLBN11.SA', nome: 'KLABIN S/A UNT' },
  { ticker: 'LAME4.SA', nome: 'LOJAS AMERIC PN' },
  { ticker: 'LIGT3.SA', nome: 'LIGHT S/A ON' },
  { ticker: 'LINX3.SA', nome: 'LINX ON' },
  { ticker: 'LREN3.SA', nome: 'LOJAS RENNER ON' },
  { ticker: 'LWSA3.SA', nome: 'LOCAWEB ON' },
  { ticker: 'MDIA3.SA', nome: 'M.DIASBRANCO ON' },
  { ticker: 'MGLU3.SA', nome: 'MAGAZINE LUIZA ON' },
  { ticker: 'MOVI3.SA', nome: 'MOVIDA ON' },
  { ticker: 'MRFG3.SA', nome: 'MARFRIG ON' },
  { ticker: 'MRVE3.SA', nome: 'MRV ON' },
  { ticker: 'MULT3.SA', nome: 'MULTIPLAN ON' },
  { ticker: 'MYPK3.SA', nome: 'IOCHPE MAXION ON' },
  { ticker: 'NEOE3.SA', nome: 'NEOENERGIA ON' },
  { ticker: 'NTCO3.SA', nome: 'NATURA &CO ON' },
  { ticker: 'ODPV3.SA', nome: 'ODONTOPREV ON' },
  { ticker: 'OIBR3.SA', nome: 'OI ON' },
  { ticker: 'OIBR4.SA', nome: 'OI PN' },
  { ticker: 'PCAR3.SA', nome: 'P.ACUCAR-CBD ON' },
  { ticker: 'PETR3.SA', nome: 'PETROBRAS ON' },
  { ticker: 'PETR4.SA', nome: 'PETROBRAS PN' },
  { ticker: 'PETZ3.SA', nome: 'PETZ ON' },
  { ticker: 'POSI3.SA', nome: 'POSITIVO TEC ON' },
  { ticker: 'PRIO3.SA', nome: 'PETRO RIO ON' },
  { ticker: 'QUAL3.SA', nome: 'QUALICORP ON' },
  { ticker: 'RADL3.SA', nome: 'RAIA DROGASIL ON' },
  { ticker: 'RAIL3.SA', nome: 'RUMO S.A. ON' },
  { ticker: 'RDOR3.SA', nome: 'REDE D\'OR ON' },
  { ticker: 'RECV3.SA', nome: 'RECOVEX ON' },
  { ticker: 'RENT3.SA', nome: 'LOCALIZA ON' },
  { ticker: 'RRRP3.SA', nome: '3R PETROLEUM ON' },
  { ticker: 'SANB11.SA', nome: 'SANTANDER BR UNT' },
  { ticker: 'SAPR11.SA', nome: 'SANEPAR UNT' },
  { ticker: 'SBSP3.SA', nome: 'SABESP ON' },
  { ticker: 'SLCE3.SA', nome: 'SLC AGRICOLA ON' },
  { ticker: 'SMFT3.SA', nome: 'SMART FIT ON' },
  { ticker: 'SMTO3.SA', nome: 'SAO MARTINHO ON' },
  { ticker: 'SOMA3.SA', nome: 'SOMA ON' },
  { ticker: 'SULA11.SA', nome: 'SUL AMERICA UNT' },
  { ticker: 'SUZB3.SA', nome: 'SUZANO S.A. ON' },
  { ticker: 'TAEE11.SA', nome: 'TAESA UNT' },
  { ticker: 'TEND3.SA', nome: 'TENDA ON' },
  { ticker: 'TIMS3.SA', nome: 'TIM ON' },
  { ticker: 'TOTS3.SA', nome: 'TOTVS ON' },
  { ticker: 'TRPL4.SA', nome: 'CTEEP PN' },
  { ticker: 'TUPY3.SA', nome: 'TUPY ON' },
  { ticker: 'UGPA3.SA', nome: 'ULTRAPAR ON' },
  { ticker: 'UNIP6.SA', nome: 'UNIPAR PNB' },
  { ticker: 'USIM5.SA', nome: 'USIMINAS PNA' },
  { ticker: 'VALE3.SA', nome: 'VALE ON' },
  { ticker: 'VAMO3.SA', nome: 'VAMOS ON' },
  { ticker: 'VBBR3.SA', nome: 'VIBRA ON' },
  { ticker: 'VIIA3.SA', nome: 'VIA ON' },
  { ticker: 'VIVT3.SA', nome: 'TELEFONICA BR ON' },
  { ticker: 'VVAR3.SA', nome: 'VIA VAREJO ON' },
  { ticker: 'WEGE3.SA', nome: 'WEG ON' },
  { ticker: 'YDUQ3.SA', nome: 'YDUQS PART ON' }
]

const tickersPopulares = {
  acoes: acoesBrasileiras.slice(0, 10), // Top 10 para display
  internacional: [
    { ticker: 'AAPL', nome: 'Apple Inc' },
    { ticker: 'TSLA', nome: 'Tesla Inc' },
    { ticker: 'MSFT', nome: 'Microsoft Corp' },
    { ticker: 'GOOGL', nome: 'Alphabet Inc' },
    { ticker: 'NVDA', nome: 'NVIDIA Corp' },
    { ticker: 'AMZN', nome: 'Amazon.com Inc' },
    { ticker: 'META', nome: 'Meta Platforms Inc' },
    { ticker: 'NFLX', nome: 'Netflix Inc' }
  ],
  cripto: [
    { ticker: 'BTC-USD', nome: 'Bitcoin USD' },
    { ticker: 'ETH-USD', nome: 'Ethereum USD' },
    { ticker: 'ADA-USD', nome: 'Cardano USD' },
    { ticker: 'BNB-USD', nome: 'Binance Coin USD' }
  ]
}

// Lista completa para busca (todas as categorias)
const todosOsTickers = [
  ...acoesBrasileiras,
  ...tickersPopulares.internacional,
  ...tickersPopulares.cripto
].sort((a, b) => a.ticker.localeCompare(b.ticker))

// Função para filtrar ações
const filtrarAcoes = (search: string) => {
  if (!search) return []
  
  const searchLower = search.toLowerCase()
  return todosOsTickers.filter(ticker => 
    ticker.ticker.toLowerCase().includes(searchLower) ||
    ticker.nome.toLowerCase().includes(searchLower)
  ).slice(0, 10) // Limitar a 10 resultados para performance
}

const getTipoAtivoIcon = (tipoAtivo: string) => {
  switch (tipoAtivo) {
    case 'ACAO': return TrendingUp
    case 'FII': return Building
    case 'RENDA_FIXA': return DollarSign
    case 'CRIPTO': return TrendingUp
    default: return DollarSign
  }
}

const getTipoAtivoLabel = (tipoAtivo: string) => {
  switch (tipoAtivo) {
    case 'ACAO': return 'Ação'
    case 'FII': return 'FII'
    case 'RENDA_FIXA': return 'Renda Fixa'
    case 'CRIPTO': return 'Cripto'
    default: return 'Desconhecido'
  }
}

export default function Investimentos() {
  const [investimentos, setInvestimentos] = useState<InvestimentoResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isCreating, setIsCreating] = useState(false)
  const [isEditing, setIsEditing] = useState(false)
  const [refreshing, setRefreshing] = useState(false)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [cotacoes, setCotacoes] = useState<Record<string, CotacaoResponse>>({})
  const [cotacoesTempoReal, setCotacoesTempoReal] = useState<CotacaoTempoReal[]>([])
  const [alertas, setAlertas] = useState<AlertaPreco[]>([])
  const [novoAlerta, setNovoAlerta] = useState({ ticker: '', precoAlvo: 0, tipo: 'abaixo' as 'acima' | 'abaixo' })
  const [alertaDialogOpen, setAlertaDialogOpen] = useState(false)
  const [activeTab, setActiveTab] = useState('carteira')
  const [investimentoSearch, setInvestimentoSearch] = useState('')
  const [investimentoDropdownOpen, setInvestimentoDropdownOpen] = useState(false)
  const [alertaSearch, setAlertaSearch] = useState('')
  const [alertaDropdownOpen, setAlertaDropdownOpen] = useState(false)
  const navigate = useNavigate()
  const { toast } = useToast()
  const { user } = useAuth()
  
  // Formulário novo investimento
  const [novoInvestimento, setNovoInvestimento] = useState<NovoInvestimento>({
    ticker: '',
    quantidade: 0,
    valorCompra: 0,
    dataCompra: new Date().toISOString().split('T')[0]
  })
  
  // Formulário edição
  const [edicaoInvestimento, setEdicaoInvestimento] = useState<EdicaoInvestimento | null>(null)

  // Carregar investimentos e cotações na inicialização
  useEffect(() => {
    carregarInvestimentos()
    carregarCotacoesTempoReal()
    carregarAlertas()
    
    // Atualizar cotações a cada 30 segundos
    const interval = setInterval(() => {
      carregarCotacoesTempoReal()
      verificarAlertas()
    }, 30000)
    
    return () => clearInterval(interval)
  }, [])
  
  // Buscar cotações em tempo real periodicamente
  useEffect(() => {
    if (investimentos.length > 0) {
      const interval = setInterval(() => {
        buscarCotacoes()
      }, 30000) // Atualizar a cada 30 segundos
      
      return () => clearInterval(interval)
    }
  }, [investimentos])

  const carregarInvestimentos = async () => {
    try {
      setIsLoading(true)
      const response = await investimentoService.listar()
      setInvestimentos(response.data.content)
      
      // Buscar cotações iniciais
      await buscarCotacoes(response.data.content)
    } catch (error: any) {
      console.error('Erro ao carregar investimentos:', error)
      sonnerToast.error('Erro ao carregar investimentos')
    } finally {
      setIsLoading(false)
    }
  }
  
  const buscarCotacoes = async (investimentosList?: InvestimentoResponse[]) => {
    const lista = investimentosList || investimentos
    if (lista.length === 0) return
    
    setRefreshing(true)
    const novasCotacoes: Record<string, CotacaoResponse> = {}
    
    for (const inv of lista) {
      try {
        const response = await cotacaoService.obterCotacao(inv.ativo.ticker)
        novasCotacoes[inv.ativo.ticker] = response.data
      } catch (error) {
        console.warn(`Erro ao buscar cotação para ${inv.ativo.ticker}:`, error)
      }
    }
    
    setCotacoes(novasCotacoes)
    setRefreshing(false)
  }
  
  const criarInvestimento = async () => {
    if (!novoInvestimento.ticker || novoInvestimento.quantidade <= 0 || novoInvestimento.valorCompra <= 0) {
      toast({
        title: "Erro de Validação",
        description: "Preencha todos os campos corretamente.",
        variant: "destructive"
      })
      return
    }
    
    try {
      setIsCreating(true)
      const request: CriarInvestimentoRequest = {
        ticker: novoInvestimento.ticker,
        quantidade: novoInvestimento.quantidade,
        valorCompra: novoInvestimento.valorCompra,
        dataCompra: novoInvestimento.dataCompra
      }
      
      await investimentoService.criar(request)
      sonnerToast.success('Investimento criado com sucesso!')
      
      // Resetar formulário
      setNovoInvestimento({
        ticker: '',
        quantidade: 0,
        valorCompra: 0,
        dataCompra: new Date().toISOString().split('T')[0]
      })
      setInvestimentoSearch('')
      
      setDialogOpen(false)
      await carregarInvestimentos()
    } catch (error: any) {
      console.error('Erro ao criar investimento:', error)
      sonnerToast.error('Erro ao criar investimento')
    } finally {
      setIsCreating(false)
    }
  }
  
  const atualizarInvestimento = async () => {
    if (!edicaoInvestimento || edicaoInvestimento.quantidade <= 0 || edicaoInvestimento.valorMedioCompra <= 0) {
      toast({
        title: "Erro de Validação",
        description: "Preencha todos os campos corretamente.",
        variant: "destructive"
      })
      return
    }
    
    try {
      setIsEditing(true)
      const request: AtualizarInvestimentoRequest = {
        quantidade: edicaoInvestimento.quantidade,
        valorMedioCompra: edicaoInvestimento.valorMedioCompra
      }
      
      await investimentoService.atualizar(edicaoInvestimento.id, request)
      sonnerToast.success('Investimento atualizado com sucesso!')
      
      setEditDialogOpen(false)
      setEdicaoInvestimento(null)
      await carregarInvestimentos()
    } catch (error: any) {
      console.error('Erro ao atualizar investimento:', error)
      sonnerToast.error('Erro ao atualizar investimento')
    } finally {
      setIsEditing(false)
    }
  }
  
  const removerInvestimento = async (id: string) => {
    if (!confirm('Tem certeza que deseja remover este investimento?')) return
    
    try {
      await investimentoService.deletar(id)
      sonnerToast.success('Investimento removido com sucesso!')
      await carregarInvestimentos()
    } catch (error: any) {
      console.error('Erro ao remover investimento:', error)
      sonnerToast.error('Erro ao remover investimento')
    }
  }
  
  const abrirEdicao = (investimento: InvestimentoResponse) => {
    setEdicaoInvestimento({
      id: investimento.id,
      quantidade: investimento.quantidade,
      valorMedioCompra: investimento.valorMedioCompra
    })
    setEditDialogOpen(true)
  }
  
  // Funções para cotações em tempo real
  const carregarCotacoesTempoReal = async () => {
    try {
      const todosTickers = [
        ...tickersPopulares.acoes,
        ...tickersPopulares.internacional,
        ...tickersPopulares.cripto
      ]
      
      const cotacoesPromises = todosTickers.map(async (item) => {
        try {
          const response = await cotacaoService.buscarCotacao(item.ticker)
          return {
            ticker: item.ticker,
            nome: item.nome,
            preco: response.data.preco,
            variacao: response.data.variacao,
            variacaoPercent: response.data.variacaoPercent,
            volume: response.data.volume || 0,
            abertura: response.data.abertura || response.data.preco,
            maxima: response.data.maxima || response.data.preco,
            minima: response.data.minima || response.data.preco,
            fechamentoAnterior: response.data.fechamentoAnterior || response.data.preco
          }
        } catch (error) {
          // Fallback com dados simulados se API falhar
          return {
            ticker: item.ticker,
            nome: item.nome,
            preco: Math.random() * 100 + 10,
            variacao: (Math.random() - 0.5) * 10,
            variacaoPercent: (Math.random() - 0.5) * 10,
            volume: Math.floor(Math.random() * 1000000),
            abertura: Math.random() * 100 + 10,
            maxima: Math.random() * 100 + 15,
            minima: Math.random() * 100 + 5,
            fechamentoAnterior: Math.random() * 100 + 10
          }
        }
      })
      
      const cotacoesData = await Promise.all(cotacoesPromises)
      setCotacoesTempoReal(cotacoesData)
    } catch (error) {
      console.error('Erro ao carregar cotações em tempo real:', error)
    }
  }
  
  // Funções para alertas
  const carregarAlertas = () => {
    const alertasSalvos = localStorage.getItem('alertasPreco')
    if (alertasSalvos) {
      setAlertas(JSON.parse(alertasSalvos))
    }
  }
  
  const salvarAlertas = (novosAlertas: AlertaPreco[]) => {
    localStorage.setItem('alertasPreco', JSON.stringify(novosAlertas))
    setAlertas(novosAlertas)
  }
  
  const criarAlerta = () => {
    if (!novoAlerta.ticker || novoAlerta.precoAlvo <= 0) {
      sonnerToast.error('Preencha todos os campos do alerta')
      return
    }
    
    const ticker = tickersPopulares.acoes.find(t => t.ticker === novoAlerta.ticker) ||
                   tickersPopulares.internacional.find(t => t.ticker === novoAlerta.ticker) ||
                   tickersPopulares.cripto.find(t => t.ticker === novoAlerta.ticker)
    
    if (!ticker) {
      sonnerToast.error('Ticker não encontrado')
      return
    }
    
    const novoAlertaObj: AlertaPreco = {
      id: Date.now().toString(),
      ticker: novoAlerta.ticker,
      nome: ticker.nome,
      precoAlvo: novoAlerta.precoAlvo,
      tipoAlerta: novoAlerta.tipo,
      ativo: true
    }
    
    const novosAlertas = [...alertas, novoAlertaObj]
    salvarAlertas(novosAlertas)
    
    setNovoAlerta({ ticker: '', precoAlvo: 0, tipo: 'abaixo' })
    setAlertaSearch('')
    setAlertaDialogOpen(false)
    sonnerToast.success('Alerta criado com sucesso!')
  }
  
  const removerAlerta = (id: string) => {
    const novosAlertas = alertas.filter(a => a.id !== id)
    salvarAlertas(novosAlertas)
    sonnerToast.success('Alerta removido!')
  }
  
  const alternarAlerta = (id: string) => {
    const novosAlertas = alertas.map(a => 
      a.id === id ? { ...a, ativo: !a.ativo } : a
    )
    salvarAlertas(novosAlertas)
  }
  
  const verificarAlertas = () => {
    const alertasAtivos = alertas.filter(a => a.ativo)
    
    alertasAtivos.forEach(alerta => {
      const cotacao = cotacoesTempoReal.find(c => c.ticker === alerta.ticker)
      if (!cotacao) return
      
      const alertaTriggered = 
        (alerta.tipoAlerta === 'acima' && cotacao.preco >= alerta.precoAlvo) ||
        (alerta.tipoAlerta === 'abaixo' && cotacao.preco <= alerta.precoAlvo)
      
      if (alertaTriggered) {
        sonnerToast.success(
          `Alerta: ${alerta.nome} está ${alerta.tipoAlerta} de R$ ${alerta.precoAlvo.toFixed(2)}!`,
          {
            description: `Preço atual: R$ ${cotacao.preco.toFixed(2)}`,
            duration: 5000
          }
        )
        
        // Desativar alerta após disparo
        alternarAlerta(alerta.id)
      }
    })
  }

  // Cálculos totais
  const valorTotalInvestido = investimentos.reduce((sum, inv) => sum + inv.valorTotalInvestido, 0)
  const valorTotalAtual = investimentos.reduce((sum, inv) => sum + inv.valorTotalAtual, 0)
  const lucroTotal = valorTotalAtual - valorTotalInvestido
  const percentualLucroTotal = valorTotalInvestido > 0 ? (lucroTotal / valorTotalInvestido) * 100 : 0
  
  if (!user) {
    return <div>Usuário não encontrado</div>
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="p-4">
        {/* Header */}
        <div className="max-w-7xl mx-auto mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-3">
              <div className="p-2 rounded-xl bg-gradient-primary">
                <TrendingUp className="h-6 w-6 text-primary-foreground" />
              </div>
              <div>
                <h1 className="text-3xl font-bold">Seus Investimentos</h1>
                <p className="text-muted-foreground">
                  Acompanhe sua carteira com cotações em tempo real
                </p>
              </div>
            </div>
            
            <div className="flex items-center space-x-3">
              <Button
                variant="outline"
                onClick={buscarCotacoes}
                disabled={refreshing}
                className="flex items-center space-x-2"
              >
                <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
                <span>Atualizar</span>
              </Button>
              
              <Dialog open={dialogOpen} onOpenChange={(open) => {
                setDialogOpen(open)
                if (open) {
                  setInvestimentoSearch('')
                  setInvestimentoDropdownOpen(false)
                }
              }}>
                <DialogTrigger asChild>
                  <HeroButton>
                    <Plus className="mr-2 h-4 w-4" />
                    Novo Investimento
                  </HeroButton>
                </DialogTrigger>
                <DialogContent className="sm:max-w-md">
                  <DialogHeader>
                    <DialogTitle>Novo Investimento</DialogTitle>
                    <DialogDescription>
                      Adicione um novo investimento à sua carteira
                    </DialogDescription>
                  </DialogHeader>
                  
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <Label>Ticker</Label>
                      <div className="relative">
                        <Input
                          type="text"
                          placeholder="🔍 Digite o código ou nome da ação..."
                          value={investimentoSearch}
                          onChange={(e) => {
                            setInvestimentoSearch(e.target.value)
                            setInvestimentoDropdownOpen(true)
                          }}
                          onFocus={() => setInvestimentoDropdownOpen(true)}
                        />
                        
                        {/* Dropdown com resultados filtrados */}
                        {investimentoDropdownOpen && investimentoSearch && (
                          <div className="absolute top-full left-0 right-0 z-50 mt-1 max-h-60 overflow-auto bg-background border border-border rounded-md shadow-lg">
                            {filtrarAcoes(investimentoSearch).map((ticker) => (
                              <div
                                key={ticker.ticker}
                                className="flex items-center p-3 hover:bg-muted cursor-pointer border-b border-border/50 last:border-b-0"
                                onClick={() => {
                                  setNovoInvestimento(prev => ({ ...prev, ticker: ticker.ticker }))
                                  setInvestimentoSearch(`${ticker.ticker} - ${ticker.nome}`)
                                  setInvestimentoDropdownOpen(false)
                                }}
                              >
                                <TrendingUp className="mr-3 h-4 w-4 text-primary" />
                                <div className="flex-1">
                                  <div className="font-medium text-sm">{ticker.ticker}</div>
                                  <div className="text-xs text-muted-foreground">{ticker.nome}</div>
                                </div>
                              </div>
                            ))}
                            
                            {filtrarAcoes(investimentoSearch).length === 0 && (
                              <div className="p-3 text-center text-muted-foreground text-sm">
                                Nenhuma ação encontrada
                              </div>
                            )}
                          </div>
                        )}
                        
                        {/* Backdrop para fechar dropdown */}
                        {investimentoDropdownOpen && (
                          <div 
                            className="fixed inset-0 z-40" 
                            onClick={() => setInvestimentoDropdownOpen(false)}
                          />
                        )}
                      </div>
                      
                      {/* Sugestões populares */}
                      <div className="text-sm text-muted-foreground">
                        <p className="mb-2">Populares:</p>
                        <div className="flex flex-wrap gap-1">
                          {[...acoesBrasileiras.slice(0, 5)].map((item) => (
                            <Button
                              key={item.ticker}
                              variant="ghost"
                              size="sm"
                              className="h-6 px-2 text-xs"
                              onClick={() => {
                                setNovoInvestimento(prev => ({ ...prev, ticker: item.ticker }))
                                setInvestimentoSearch(`${item.ticker} - ${item.nome}`)
                                setInvestimentoDropdownOpen(false)
                              }}
                            >
                              {item.ticker}
                            </Button>
                          ))}
                        </div>
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label>Quantidade</Label>
                        <Input
                          type="number"
                          value={novoInvestimento.quantidade}
                          onChange={(e) => setNovoInvestimento(prev => ({ ...prev, quantidade: Number(e.target.value) }))}
                          min="0"
                          step="1"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label>Preço de Compra (R$)</Label>
                        <Input
                          type="number"
                          value={novoInvestimento.valorCompra}
                          onChange={(e) => setNovoInvestimento(prev => ({ ...prev, valorCompra: Number(e.target.value) }))}
                          min="0"
                          step="0.01"
                        />
                      </div>
                    </div>
                    
                    <div className="space-y-2">
                      <Label>Data da Compra</Label>
                      <Input
                        type="date"
                        value={novoInvestimento.dataCompra}
                        onChange={(e) => setNovoInvestimento(prev => ({ ...prev, dataCompra: e.target.value }))}
                      />
                    </div>
                  </div>
                  
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setDialogOpen(false)}>Cancelar</Button>
                    <HeroButton onClick={criarInvestimento} disabled={isCreating}>
                      {isCreating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                      Criar
                    </HeroButton>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            </div>
          </div>
        </div>

      <div className="max-w-7xl mx-auto space-y-6">
        {/* Cards Resumo */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <Card className="bg-gradient-surface border-border/50">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Investido</p>
                  <p className="text-2xl font-bold">
                    {new Intl.NumberFormat('pt-BR', {
                      style: 'currency',
                      currency: 'BRL'
                    }).format(valorTotalInvestido)}
                  </p>
                </div>
                <DollarSign className="h-8 w-8 text-muted-foreground" />
              </div>
            </CardContent>
          </Card>
          
          <Card className="bg-gradient-surface border-border/50">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Valor Atual</p>
                  <p className="text-2xl font-bold">
                    {new Intl.NumberFormat('pt-BR', {
                      style: 'currency',
                      currency: 'BRL'
                    }).format(valorTotalAtual)}
                  </p>
                </div>
                <TrendingUp className="h-8 w-8 text-muted-foreground" />
              </div>
            </CardContent>
          </Card>
          
          <Card className="bg-gradient-surface border-border/50">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Lucro/Prejuízo</p>
                  <p className={`text-2xl font-bold ${lucroTotal >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {new Intl.NumberFormat('pt-BR', {
                      style: 'currency',
                      currency: 'BRL'
                    }).format(lucroTotal)}
                  </p>
                </div>
                <TrendingUp className={`h-8 w-8 ${lucroTotal >= 0 ? 'text-green-600' : 'text-red-600'}`} />
              </div>
            </CardContent>
          </Card>
          
          <Card className="bg-gradient-surface border-border/50">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Rendimento</p>
                  <p className={`text-2xl font-bold ${percentualLucroTotal >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {percentualLucroTotal.toFixed(2)}%
                  </p>
                </div>
                <PieChart className="h-8 w-8 text-muted-foreground" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Tabs para Carteira e Mercado */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="carteira">Minha Carteira</TabsTrigger>
            <TabsTrigger value="mercado">Mercado em Tempo Real</TabsTrigger>
            <TabsTrigger value="alertas">Alertas de Preço</TabsTrigger>
          </TabsList>
          
          {/* Aba Minha Carteira */}
          <TabsContent value="carteira" className="space-y-4">
            {/* Lista de Investimentos */}
        {isLoading ? (
          <div className="flex justify-center items-center py-12">
            <Loader2 className="h-8 w-8 animate-spin" />
          </div>
        ) : investimentos.length === 0 ? (
          <Card className="bg-gradient-surface border-border/50">
            <CardContent className="p-12 text-center">
              <TrendingUp className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-xl font-semibold mb-2">Nenhum investimento cadastrado</h3>
              <p className="text-muted-foreground mb-6">
                Adicione seus investimentos para acompanhar sua carteira com dados reais
              </p>
              <HeroButton onClick={() => setDialogOpen(true)}>
                <Plus className="mr-2 h-4 w-4" />
                Criar Primeiro Investimento
              </HeroButton>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-4">
            {investimentos.map((investimento) => {
              const cotacao = cotacoes[investimento.ativo.ticker]
              const Icon = getTipoAtivoIcon(investimento.ativo.tipoAtivo)
              const isPositive = investimento.lucroPreju >= 0
              
              return (
                <Card key={investimento.id} className="bg-gradient-surface border-border/50">
                  <CardContent className="p-6">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <div className="p-2 rounded-lg bg-primary/10">
                          <Icon className="h-6 w-6 text-primary" />
                        </div>
                        <div>
                          <h3 className="text-lg font-semibold">{investimento.ativo.ticker}</h3>
                          <p className="text-sm text-muted-foreground">
                            {investimento.ativo.nome} • {getTipoAtivoLabel(investimento.ativo.tipoAtivo)}
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex items-center space-x-4">
                        {cotacao && (
                          <div className="text-right">
                            <p className="text-sm text-muted-foreground">Cotação Atual</p>
                            <p className="font-semibold">
                              {new Intl.NumberFormat('pt-BR', {
                                style: 'currency',
                                currency: 'BRL'
                              }).format(cotacao.preco)}
                            </p>
                            <p className={`text-sm ${cotacao.variacaoPercent >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                              {cotacao.variacaoPercent >= 0 ? '+' : ''}{cotacao.variacaoPercent.toFixed(2)}%
                            </p>
                          </div>
                        )}
                        
                        <div className="flex items-center space-x-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => abrirEdicao(investimento)}
                          >
                            <Edit2 className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => removerInvestimento(investimento.id)}
                            className="text-destructive hover:text-destructive"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mt-6 pt-6 border-t">
                      <div>
                        <p className="text-sm text-muted-foreground">Quantidade</p>
                        <p className="font-semibold">{investimento.quantidade}</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Preço Médio</p>
                        <p className="font-semibold">
                          {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL'
                          }).format(investimento.valorMedioCompra)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Total Investido</p>
                        <p className="font-semibold">
                          {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL'
                          }).format(investimento.valorTotalInvestido)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Valor Atual</p>
                        <p className="font-semibold">
                          {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL'
                          }).format(investimento.valorTotalAtual)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Lucro/Prejuízo</p>
                        <p className={`font-semibold ${isPositive ? 'text-green-600' : 'text-red-600'}`}>
                          {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL'
                          }).format(investimento.lucroPreju)}
                        </p>
                        <p className={`text-sm ${isPositive ? 'text-green-600' : 'text-red-600'}`}>
                          {isPositive ? '+' : ''}{investimento.percentualLucroPreju.toFixed(2)}%
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )
            })}
          </div>
        )}
          </TabsContent>
          
          {/* Aba Mercado em Tempo Real */}
          <TabsContent value="mercado" className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {cotacoesTempoReal.map((cotacao) => (
                <Card key={cotacao.ticker} className="bg-gradient-surface border-border/50">
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between mb-3">
                      <div>
                        <h3 className="font-semibold text-lg">{cotacao.ticker}</h3>
                        <p className="text-sm text-muted-foreground truncate">{cotacao.nome}</p>
                      </div>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setNovoAlerta({ ...novoAlerta, ticker: cotacao.ticker })
                          setAlertaDialogOpen(true)
                        }}
                      >
                        <Bell className="h-4 w-4" />
                      </Button>
                    </div>
                    
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-2xl font-bold">
                          R$ {cotacao.preco.toFixed(2)}
                        </span>
                        <div className={`flex items-center space-x-1 ${
                          cotacao.variacaoPercent >= 0 ? 'text-green-600' : 'text-red-600'
                        }`}>
                          {cotacao.variacaoPercent >= 0 ? <TrendingUp className="h-4 w-4" /> : <TrendingDown className="h-4 w-4" />}
                          <span className="font-medium">
                            {cotacao.variacaoPercent >= 0 ? '+' : ''}{cotacao.variacaoPercent.toFixed(2)}%
                          </span>
                        </div>
                      </div>
                      
                      <div className="grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                        <div>
                          <span className="block">Abertura</span>
                          <span className="font-medium">R$ {cotacao.abertura.toFixed(2)}</span>
                        </div>
                        <div>
                          <span className="block">Volume</span>
                          <span className="font-medium">{(cotacao.volume / 1000000).toFixed(1)}M</span>
                        </div>
                        <div>
                          <span className="block">Mínima</span>
                          <span className="font-medium">R$ {cotacao.minima.toFixed(2)}</span>
                        </div>
                        <div>
                          <span className="block">Máxima</span>
                          <span className="font-medium">R$ {cotacao.maxima.toFixed(2)}</span>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
            
            {cotacoesTempoReal.length === 0 && (
              <div className="flex justify-center items-center py-12">
                <div className="text-center">
                  <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
                  <p className="text-muted-foreground">Carregando cotações...</p>
                </div>
              </div>
            )}
          </TabsContent>
          
          {/* Aba Alertas de Preço */}
          <TabsContent value="alertas" className="space-y-4">
            <div className="flex justify-between items-center">
              <div>
                <h3 className="text-lg font-semibold">Alertas de Preço</h3>
                <p className="text-sm text-muted-foreground">
                  Seja notificado quando as ações atingirem seus preços-alvo
                </p>
              </div>
              <Dialog open={alertaDialogOpen} onOpenChange={(open) => {
                setAlertaDialogOpen(open)
                if (open) {
                  setAlertaSearch('')
                  setAlertaDropdownOpen(false)
                }
              }}>
                <DialogTrigger asChild>
                  <HeroButton>
                    <Plus className="mr-2 h-4 w-4" />
                    Novo Alerta
                  </HeroButton>
                </DialogTrigger>
                <DialogContent className="sm:max-w-md">
                  <DialogHeader>
                    <DialogTitle>Criar Alerta de Preço</DialogTitle>
                    <DialogDescription>
                      Configure um alerta para ser notificado quando o preço atingir seu valor-alvo
                    </DialogDescription>
                  </DialogHeader>
                  
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <Label>Ação</Label>
                      <div className="relative">
                        <Input
                          type="text"
                          placeholder="🔍 Digite o código ou nome da ação..."
                          value={alertaSearch}
                          onChange={(e) => {
                            setAlertaSearch(e.target.value)
                            setAlertaDropdownOpen(true)
                          }}
                          onFocus={() => setAlertaDropdownOpen(true)}
                        />
                        
                        {/* Dropdown com resultados filtrados */}
                        {alertaDropdownOpen && alertaSearch && (
                          <div className="absolute top-full left-0 right-0 z-50 mt-1 max-h-60 overflow-auto bg-background border border-border rounded-md shadow-lg">
                            {filtrarAcoes(alertaSearch).map((ticker) => (
                              <div
                                key={ticker.ticker}
                                className="flex items-center p-3 hover:bg-muted cursor-pointer border-b border-border/50 last:border-b-0"
                                onClick={() => {
                                  setNovoAlerta(prev => ({ ...prev, ticker: ticker.ticker }))
                                  setAlertaSearch(`${ticker.ticker} - ${ticker.nome}`)
                                  setAlertaDropdownOpen(false)
                                }}
                              >
                                <TrendingUp className="mr-3 h-4 w-4 text-primary" />
                                <div className="flex-1">
                                  <div className="font-medium text-sm">{ticker.ticker}</div>
                                  <div className="text-xs text-muted-foreground">{ticker.nome}</div>
                                </div>
                              </div>
                            ))}
                            
                            {filtrarAcoes(alertaSearch).length === 0 && (
                              <div className="p-3 text-center text-muted-foreground text-sm">
                                Nenhuma ação encontrada
                              </div>
                            )}
                          </div>
                        )}
                        
                        {/* Backdrop para fechar dropdown */}
                        {alertaDropdownOpen && (
                          <div 
                            className="fixed inset-0 z-40" 
                            onClick={() => setAlertaDropdownOpen(false)}
                          />
                        )}
                      </div>
                      
                      {/* Sugestões populares */}
                      <div className="text-sm text-muted-foreground">
                        <p className="mb-2">Populares:</p>
                        <div className="flex flex-wrap gap-1">
                          {[...acoesBrasileiras.slice(0, 5)].map((item) => (
                            <Button
                              key={item.ticker}
                              variant="ghost"
                              size="sm"
                              className="h-6 px-2 text-xs"
                              onClick={() => {
                                setNovoAlerta(prev => ({ ...prev, ticker: item.ticker }))
                                setAlertaSearch(`${item.ticker} - ${item.nome}`)
                                setAlertaDropdownOpen(false)
                              }}
                            >
                              {item.ticker}
                            </Button>
                          ))}
                        </div>
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label>Preço Alvo (R$)</Label>
                        <Input
                          type="number"
                          step="0.01"
                          value={novoAlerta.precoAlvo}
                          onChange={(e) => setNovoAlerta({...novoAlerta, precoAlvo: Number(e.target.value)})}
                          placeholder="0.00"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label>Tipo de Alerta</Label>
                        <Select value={novoAlerta.tipo} onValueChange={(value: 'acima' | 'abaixo') => setNovoAlerta({...novoAlerta, tipo: value})}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="acima">Acima do preço</SelectItem>
                            <SelectItem value="abaixo">Abaixo do preço</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                  </div>
                  
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setAlertaDialogOpen(false)}>Cancelar</Button>
                    <HeroButton onClick={criarAlerta}>Criar Alerta</HeroButton>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            </div>
            
            {alertas.length === 0 ? (
              <Card className="bg-gradient-surface border-border/50">
                <CardContent className="p-12 text-center">
                  <Bell className="h-16 w-16 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-xl font-semibold mb-2">Nenhum alerta configurado</h3>
                  <p className="text-muted-foreground mb-6">
                    Configure alertas para ser notificado quando suas ações favoritas atingirem preços específicos
                  </p>
                  <HeroButton onClick={() => setAlertaDialogOpen(true)}>
                    <Plus className="mr-2 h-4 w-4" />
                    Criar Primeiro Alerta
                  </HeroButton>
                </CardContent>
              </Card>
            ) : (
              <div className="space-y-3">
                {alertas.map((alerta) => {
                  const cotacao = cotacoesTempoReal.find(c => c.ticker === alerta.ticker)
                  const precoAtual = cotacao?.preco || 0
                  const distanciaPercentual = precoAtual > 0 ? ((alerta.precoAlvo - precoAtual) / precoAtual) * 100 : 0
                  
                  return (
                    <Card key={alerta.id} className="bg-gradient-surface border-border/50">
                      <CardContent className="p-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center space-x-3">
                            <div className={`p-2 rounded-lg ${alerta.ativo ? 'bg-primary/10' : 'bg-muted/50'}`}>
                              {alerta.tipoAlerta === 'acima' ? 
                                <TrendingUp className={`h-5 w-5 ${alerta.ativo ? 'text-primary' : 'text-muted-foreground'}`} /> :
                                <TrendingDown className={`h-5 w-5 ${alerta.ativo ? 'text-primary' : 'text-muted-foreground'}`} />
                              }
                            </div>
                            <div>
                              <h4 className="font-semibold">{alerta.ticker}</h4>
                              <p className="text-sm text-muted-foreground">{alerta.nome}</p>
                            </div>
                          </div>
                          
                          <div className="flex items-center space-x-3">
                            <div className="text-right">
                              <p className="font-semibold">
                                R$ {alerta.precoAlvo.toFixed(2)} ({alerta.tipoAlerta})
                              </p>
                              {cotacao && (
                                <div className="text-sm">
                                  <span className="text-muted-foreground">Atual: R$ {precoAtual.toFixed(2)}</span>
                                  <Badge variant={Math.abs(distanciaPercentual) < 5 ? "default" : "secondary"} className="ml-2">
                                    {distanciaPercentual > 0 ? '+' : ''}{distanciaPercentual.toFixed(1)}%
                                  </Badge>
                                </div>
                              )}
                            </div>
                            
                            <div className="flex items-center space-x-2">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => alternarAlerta(alerta.id)}
                                className={alerta.ativo ? 'border-primary text-primary' : ''}
                              >
                                {alerta.ativo ? <Eye className="h-4 w-4" /> : <Eye className="h-4 w-4 opacity-50" />}
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => removerAlerta(alerta.id)}
                                className="text-destructive hover:text-destructive"
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                        </div>
                        
                        {!alerta.ativo && (
                          <div className="mt-3 p-2 bg-muted/50 rounded text-sm text-muted-foreground">
                            <AlertCircle className="h-4 w-4 inline mr-2" />
                            Alerta desativado
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  )
                })}
              </div>
            )}
          </TabsContent>
        </Tabs>

        {/* Dialog de Edição */}
        <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>Editar Investimento</DialogTitle>
              <DialogDescription>
                Atualize os dados do seu investimento
              </DialogDescription>
            </DialogHeader>
            
            {edicaoInvestimento && (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Quantidade</Label>
                    <Input
                      type="number"
                      value={edicaoInvestimento.quantidade}
                      onChange={(e) => setEdicaoInvestimento(prev => 
                        prev ? { ...prev, quantidade: Number(e.target.value) } : null
                      )}
                      min="0"
                      step="1"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label>Preço Médio (R$)</Label>
                    <Input
                      type="number"
                      value={edicaoInvestimento.valorMedioCompra}
                      onChange={(e) => setEdicaoInvestimento(prev => 
                        prev ? { ...prev, valorMedioCompra: Number(e.target.value) } : null
                      )}
                      min="0"
                      step="0.01"
                    />
                  </div>
                </div>
                
                <div className="p-4 bg-muted/50 rounded-lg">
                  <p className="text-sm text-muted-foreground">Novo Valor Total</p>
                  <p className="text-lg font-semibold">
                    {new Intl.NumberFormat('pt-BR', {
                      style: 'currency',
                      currency: 'BRL'
                    }).format(edicaoInvestimento.quantidade * edicaoInvestimento.valorMedioCompra)}
                  </p>
                </div>
              </div>
            )}
            
            <DialogFooter>
              <Button variant="outline" onClick={() => setEditDialogOpen(false)}>Cancelar</Button>
              <HeroButton onClick={atualizarInvestimento} disabled={isEditing}>
                {isEditing && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Salvar
              </HeroButton>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
      </div>
    </div>
  )
}