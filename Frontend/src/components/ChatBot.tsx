import { useState, useRef, useEffect } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { 
  Bot, Send, Minimize2, Maximize2, History, TrendingUp, 
  ThumbsUp, ThumbsDown, BarChart3, MessageCircle, Sparkles,
  Clock, Star, FileText, Lightbulb 
} from "lucide-react"
import { cn } from "@/lib/utils"
import { chatService } from "@/lib/api"
import { toast } from "sonner"

interface Message {
  id: string
  content: string
  isBot: boolean
  timestamp: Date
  rating?: number
  tipo?: string
  contexto?: any
}

interface HistoricoConversa {
  id: string
  pergunta: string
  resposta: string
  tipo: string
  criadoEm: string
  avaliacaoUsuario?: number
  tempoRespostaMs?: number
}

interface EstatisticasChat {
  totalConversas: number
  mediaAvaliacao: number
  tiposMaisUsados: string[]
}

interface ChatBotProps {
  userProfile?: "conservador" | "moderado" | "agressivo"
  isExpanded?: boolean
}

const ninaPersonality = {
  conservador: {
    greeting: "Olá! Sou a Nina, sua assistente de investimentos. Vou te ajudar com estratégias seguras e estáveis. Como posso te ajudar hoje?",
    tone: "calma e reasseguradora"
  },
  moderado: {
    greeting: "Oi! Nina aqui! 😊 Estou aqui para te ajudar a equilibrar seus investimentos. Que tal explorarmos algumas oportunidades?",
    tone: "equilibrada e amigável"
  },
  agressivo: {
    greeting: "E aí! Nina na área! 🚀 Pronto para domar o mercado? Vamos conversar sobre oportunidades ousadas!",
    tone: "ousada e energética"
  }
}

// Mensagem padrão quando a API falha
const fallbackResponse = "Desculpe, tive um problema para processar sua pergunta. Tente novamente em alguns instantes."

export function ChatBot({ userProfile = "moderado", isExpanded = false }: ChatBotProps) {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: "1",
      content: ninaPersonality[userProfile].greeting,
      isBot: true,
      timestamp: new Date()
    }
  ])
  const [inputValue, setInputValue] = useState("")
  const [isMinimized, setIsMinimized] = useState(true)
  
  // Desabilitar scroll da página quando chat estiver aberto
  useEffect(() => {
    if (!isMinimized) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = 'unset'
    }
    
    // Cleanup quando componente desmontar
    return () => {
      document.body.style.overflow = 'unset'
    }
  }, [isMinimized])
  const [isTyping, setIsTyping] = useState(false)
  const [activeTab, setActiveTab] = useState("chat")
  const [historico, setHistorico] = useState<HistoricoConversa[]>([])
  const [estatisticas, setEstatisticas] = useState<EstatisticasChat | null>(null)
  const [isExpandedMode, setIsExpandedMode] = useState(isExpanded)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const carregarHistorico = async () => {
    try {
      const response = await chatService.obterHistorico?.(10)
      if (response?.data) {
        setHistorico(response.data)
      }
    } catch (error) {
      console.error('Erro ao carregar histórico:', error)
    }
  }

  const carregarEstatisticas = async () => {
    try {
      const response = await chatService.obterEstatisticas?.()
      if (response?.data) {
        setEstatisticas(response.data)
      }
    } catch (error) {
      console.error('Erro ao carregar estatísticas:', error)
    }
  }

  const avaliarResposta = async (messageId: string, conversaId: string, rating: number) => {
    try {
      await chatService.avaliarResposta?.(conversaId, rating)
      
      setMessages(prev => prev.map(msg => 
        msg.id === messageId ? { ...msg, rating } : msg
      ))
      
      toast.success(`Avaliação ${rating === 1 ? 'positiva' : 'negativa'} enviada!`)
    } catch (error) {
      console.error('Erro ao avaliar resposta:', error)
      toast.error('Erro ao enviar avaliação')
    }
  }

  const handleQuickAction = async (action: string) => {
    setIsTyping(true)
    
    try {
      let response
      const timestamp = new Date()
      
      switch (action) {
        case 'analise':
          response = await chatService.analisarCarteira()
          break
        case 'recomendacoes':
          response = await chatService.obterRecomendacoes()
          break
        default:
          return
      }
      
      const actionLabels = {
        'analise': 'Análise da Carteira',
        'recomendacoes': 'Recomendações Personalizadas'
      }
      
      const userMessage: Message = {
        id: Date.now().toString(),
        content: actionLabels[action as keyof typeof actionLabels],
        isBot: false,
        timestamp,
        tipo: action
      }
      
      const botResponse: Message = {
        id: (Date.now() + 1).toString(),
        content: response.data.resposta || fallbackResponse,
        isBot: true,
        timestamp: new Date(),
        tipo: action
      }
      
      setMessages(prev => [...prev, userMessage, botResponse])
    } catch (error: any) {
      console.error('Erro na ação rápida:', error)
      
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        content: error.response?.data?.erro || fallbackResponse,
        isBot: true,
        timestamp: new Date()
      }
      
      setMessages(prev => [...prev, errorMessage])
      toast.error('Erro ao executar ação')
    } finally {
      setIsTyping(false)
    }
  }

  useEffect(() => {
    if (activeTab === 'historico' && historico.length === 0) {
      carregarHistorico()
    }
    if (activeTab === 'stats' && !estatisticas) {
      carregarEstatisticas()
    }
  }, [activeTab])

  const handleSendMessage = async () => {
    if (!inputValue.trim()) return

    const userMessage: Message = {
      id: Date.now().toString(),
      content: inputValue,
      isBot: false,
      timestamp: new Date()
    }

    const pergunta = inputValue
    setMessages(prev => [...prev, userMessage])
    setInputValue("")
    setIsTyping(true)

    try {
      // Chamar a API real do backend
      const response = await chatService.fazerPergunta({ pergunta })
      
      const botResponse: Message = {
        id: (Date.now() + 1).toString(),
        content: response.data.resposta || fallbackResponse,
        isBot: true,
        timestamp: new Date()
      }
      
      setMessages(prev => [...prev, botResponse])
    } catch (error: any) {
      console.error('Erro no chatbot:', error)
      
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        content: error.response?.data?.erro || fallbackResponse,
        isBot: true,
        timestamp: new Date()
      }
      
      setMessages(prev => [...prev, errorMessage])
      toast.error('Erro ao conversar com a Nina')
    } finally {
      setIsTyping(false)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleSendMessage()
    }
  }

  if (isMinimized) {
    return (
      <div className="fixed bottom-6 right-6 z-50">
        <Button
          onClick={() => setIsMinimized(false)}
          className="rounded-full h-14 w-14 bg-gradient-primary shadow-glow"
          size="icon"
        >
          <Bot className="h-6 w-6" />
        </Button>
      </div>
    )
  }

  return (
    <Card className={cn(
      "fixed z-50 flex flex-col bg-gradient-surface border-border/50 shadow-large",
      isExpandedMode 
        ? "top-4 left-4 right-4 bottom-4 w-auto h-auto" 
        : "bottom-6 right-6 w-80 h-[500px]"
    )}>
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-border/50">
        <div className="flex items-center space-x-3">
          <Avatar className="h-8 w-8 bg-gradient-primary">
            <AvatarFallback className="text-primary-foreground font-semibold">
              N
            </AvatarFallback>
          </Avatar>
          <div>
            <h3 className="font-semibold text-foreground flex items-center gap-2">
              Nina <Sparkles className="h-4 w-4 text-yellow-500" />
            </h3>
            <p className="text-xs text-muted-foreground">IA Personalizada</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsExpandedMode(!isExpandedMode)}
            className="h-8 w-8"
          >
            <Maximize2 className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsMinimized(true)}
            className="h-8 w-8"
          >
            <Minimize2 className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {/* Tabs Navigation */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="chat" className="flex items-center gap-2">
            <MessageCircle className="h-4 w-4" />
            Chat
          </TabsTrigger>
          <TabsTrigger value="historico" className="flex items-center gap-2">
            <History className="h-4 w-4" />
            Histórico
          </TabsTrigger>
          <TabsTrigger value="stats" className="flex items-center gap-2">
            <BarChart3 className="h-4 w-4" />
            Stats
          </TabsTrigger>
        </TabsList>

        {/* Chat Tab */}
        <TabsContent value="chat" className="flex-1 flex flex-col m-0">
          {/* Quick Actions */}
          <div className="p-3 border-b border-border/50">
            <div className="flex flex-wrap gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleQuickAction('analise')}
                className="text-xs"
              >
                <TrendingUp className="h-3 w-3 mr-1" />
                Analisar Carteira
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleQuickAction('recomendacoes')}
                className="text-xs"
              >
                <Lightbulb className="h-3 w-3 mr-1" />
                Recomendações
              </Button>
            </div>
          </div>

          {/* Messages */}
          <div className="flex-1 p-4 space-y-4 overflow-y-auto max-h-[350px]">
            {messages.map((message) => (
              <div
                key={message.id}
                className={cn(
                  "flex flex-col",
                  message.isBot ? "items-start" : "items-end"
                )}
              >
                <div
                  className={cn(
                    "max-w-[85%] rounded-lg p-3 text-sm",
                    message.isBot
                      ? "bg-surface text-foreground"
                      : "bg-gradient-primary text-primary-foreground"
                  )}
                >
                  {message.content}
                  
                  {/* Message metadata */}
                  <div className="flex items-center justify-between mt-2 text-xs opacity-70">
                    <span className="flex items-center gap-1">
                      <Clock className="h-3 w-3" />
                      {message.timestamp.toLocaleTimeString('pt-BR', { 
                        hour: '2-digit', 
                        minute: '2-digit' 
                      })}
                    </span>
                    
                    {message.tipo && (
                      <Badge variant="secondary" className="text-xs">
                        {message.tipo}
                      </Badge>
                    )}
                  </div>
                </div>
                
                {/* Rating buttons for bot messages */}
                {message.isBot && message.id !== "1" && (
                  <div className="flex items-center gap-1 mt-1">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => avaliarResposta(message.id, message.id, 5)}
                      className={cn(
                        "h-6 w-6 p-0",
                        message.rating === 5 && "text-green-500"
                      )}
                    >
                      <ThumbsUp className="h-3 w-3" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => avaliarResposta(message.id, message.id, 1)}
                      className={cn(
                        "h-6 w-6 p-0",
                        message.rating === 1 && "text-red-500"
                      )}
                    >
                      <ThumbsDown className="h-3 w-3" />
                    </Button>
                  </div>
                )}
              </div>
            ))}
            
            {isTyping && (
              <div className="flex justify-start">
                <div className="bg-surface text-foreground rounded-lg p-3 text-sm">
                  <div className="flex space-x-1">
                    <div className="w-2 h-2 bg-muted-foreground rounded-full animate-bounce"></div>
                    <div className="w-2 h-2 bg-muted-foreground rounded-full animate-bounce" style={{ animationDelay: "0.1s" }}></div>
                    <div className="w-2 h-2 bg-muted-foreground rounded-full animate-bounce" style={{ animationDelay: "0.2s" }}></div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div className="p-4 border-t border-border/50">
            <div className="flex space-x-2">
              <Input
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Digite sua pergunta..."
                className="flex-1 bg-surface border-border/50"
              />
              <Button
                onClick={handleSendMessage}
                size="icon"
                className="bg-gradient-primary"
                disabled={!inputValue.trim() || isTyping}
              >
                <Send className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </TabsContent>

        {/* Histórico Tab */}
        <TabsContent value="historico" className="flex-1 p-4 m-0">
          <div className="overflow-y-auto max-h-[350px]">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h4 className="font-semibold text-sm">Conversas Anteriores</h4>
              <Button variant="outline" size="sm" onClick={carregarHistorico}>
                Atualizar
              </Button>
            </div>
            
            {historico.length === 0 ? (
              <p className="text-muted-foreground text-sm text-center py-8">
                Nenhuma conversa anterior encontrada
              </p>
            ) : (
              historico.map((conv) => (
                <Card key={conv.id} className="p-3">
                  <div className="space-y-2">
                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                      <Clock className="h-3 w-3" />
                      {new Date(conv.criadoEm).toLocaleString('pt-BR')}
                      <Badge variant="outline" className="text-xs">
                        {conv.tipo}
                      </Badge>
                      {conv.avaliacaoUsuario && (
                        <div className="flex items-center gap-1">
                          <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
                          <span>{conv.avaliacaoUsuario}</span>
                        </div>
                      )}
                    </div>
                    <p className="text-sm font-medium">{conv.pergunta}</p>
                    <p className="text-xs text-muted-foreground line-clamp-2">
                      {conv.resposta.substring(0, 100)}...
                    </p>
                  </div>
                </Card>
              ))
            )}
          </div>
          </div>
        </TabsContent>

        {/* Estatísticas Tab */}
        <TabsContent value="stats" className="flex-1 p-4 m-0">
          <div className="overflow-y-auto max-h-[350px]">
          <div className="space-y-4">
            <h4 className="font-semibold text-sm">Estatísticas de Uso</h4>
            
            {!estatisticas ? (
              <p className="text-muted-foreground text-sm text-center py-8">
                Carregando estatísticas...
              </p>
            ) : (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <Card className="p-4 text-center">
                    <div className="text-2xl font-bold text-primary">
                      {estatisticas.totalConversas}
                    </div>
                    <div className="text-xs text-muted-foreground">
                      Total de Conversas
                    </div>
                  </Card>
                  <Card className="p-4 text-center">
                    <div className="text-2xl font-bold text-primary flex items-center justify-center gap-1">
                      {estatisticas.mediaAvaliacao.toFixed(1)}
                      <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                    </div>
                    <div className="text-xs text-muted-foreground">
                      Média de Avaliação
                    </div>
                  </Card>
                </div>
                
                <Card className="p-4">
                  <h5 className="font-medium text-sm mb-2">Tópicos Mais Usados</h5>
                  <div className="flex flex-wrap gap-2">
                    {estatisticas.tiposMaisUsados.map((tipo) => (
                      <Badge key={tipo} variant="secondary">
                        {tipo}
                      </Badge>
                    ))}
                  </div>
                </Card>
              </div>
            )}
          </div>
          </div>
        </TabsContent>
      </Tabs>
    </Card>
  )
}