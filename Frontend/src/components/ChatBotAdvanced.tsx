import { useState, useRef, useEffect } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Bot, Send, History, ThumbsUp, ThumbsDown, TrendingUp, PieChart, MessageCircle, Star, Clock, Lightbulb, Minimize2 } from "lucide-react"
import { cn } from "@/lib/utils"
import { chatService } from "@/lib/api"
import { toast } from "sonner"
import { useAuth } from "@/contexts/AuthContext"

interface Message {
  id: string
  content: string
  isBot: boolean
  timestamp: Date
  rating?: number
  type?: 'pergunta_geral' | 'analise_carteira' | 'recomendacoes' | 'educacional' | 'suporte'
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

interface ChatBotProps {
  userProfile?: "conservador" | "moderado" | "agressivo"
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

export function ChatBotAdvanced({ userProfile = "moderado" }: ChatBotProps) {
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState("chat")
  const [messages, setMessages] = useState<Message[]>([
    {
      id: "1",
      content: ninaPersonality[userProfile].greeting,
      isBot: true,
      timestamp: new Date()
    }
  ])
  const [inputValue, setInputValue] = useState("")
  const [isMinimized, setIsMinimized] = useState(false)
  const [isTyping, setIsTyping] = useState(false)
  const [historico, setHistorico] = useState<HistoricoConversa[]>([])
  const [loadingHistory, setLoadingHistory] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
    }, 100)
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  useEffect(() => {
    if (activeTab === "history") {
      carregarHistorico()
    }
  }, [activeTab])

  const carregarHistorico = async () => {
    if (!user) return
    
    setLoadingHistory(true)
    try {
      const response = await chatService.obterHistorico(20)
      setHistorico(response.data || [])
    } catch (error) {
      toast.error('Erro ao carregar histórico')
    } finally {
      setLoadingHistory(false)
    }
  }

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
      const startTime = Date.now()
      // Chamar a API real do backend
      const response = await chatService.fazerPergunta({ pergunta })
      const responseTime = Date.now() - startTime
      
      const botResponse: Message = {
        id: (Date.now() + 1).toString(),
        content: response.data.resposta || fallbackResponse,
        isBot: true,
        timestamp: new Date(),
        type: classificarTipoMensagem(pergunta)
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

  const classificarTipoMensagem = (pergunta: string): Message['type'] => {
    const perguntaLower = pergunta.toLowerCase()
    
    if (perguntaLower.includes('analis') || perguntaLower.includes('carteira')) {
      return 'analise_carteira'
    }
    if (perguntaLower.includes('recomen') || perguntaLower.includes('suger') || perguntaLower.includes('comprar')) {
      return 'recomendacoes'
    }
    if (perguntaLower.includes('como') || perguntaLower.includes('aprend') || perguntaLower.includes('ensina')) {
      return 'educacional'
    }
    if (perguntaLower.includes('problem') || perguntaLower.includes('erro') || perguntaLower.includes('ajuda')) {
      return 'suporte'
    }
    
    return 'pergunta_geral'
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  const avaliarResposta = async (messageId: string, rating: number) => {
    try {
      // Para o sistema atual, vamos usar o ID como UUID para o histórico
      // Em uma implementação mais robusta, seria necessário mapear os IDs
      await chatService.avaliarResposta(messageId, rating)
      setMessages(prev => 
        prev.map(msg => 
          msg.id === messageId ? { ...msg, rating } : msg
        )
      )
      toast.success('Obrigado pelo feedback!')
    } catch (error) {
      console.error('Erro ao avaliar resposta:', error)
      toast.error('Erro ao avaliar resposta')
    }
  }

  const executarAcaoRapida = async (acao: string) => {
    let pergunta = ''
    switch (acao) {
      case 'analise':
        pergunta = 'Faça uma análise completa da minha carteira'
        break
      case 'recomendacoes':
        pergunta = 'Me dê recomendações de investimentos baseadas no meu perfil'
        break
      case 'mercado':
        pergunta = 'Como está o mercado hoje? Alguma oportunidade interessante?'
        break
      case 'educacao':
        pergunta = 'Me ensine algo importante sobre investimentos'
        break
    }
    
    setInputValue(pergunta)
    setTimeout(() => handleSendMessage(), 100)
  }

  const getMessageTypeIcon = (type?: Message['type']) => {
    switch (type) {
      case 'analise_carteira': return <PieChart className="h-3 w-3" />
      case 'recomendacoes': return <TrendingUp className="h-3 w-3" />
      case 'educacional': return <Lightbulb className="h-3 w-3" />
      case 'suporte': return <MessageCircle className="h-3 w-3" />
      default: return <Bot className="h-3 w-3" />
    }
  }

  const getMessageTypeBadge = (type?: Message['type']) => {
    switch (type) {
      case 'analise_carteira': return <Badge variant="secondary" className="text-xs">Análise</Badge>
      case 'recomendacoes': return <Badge variant="default" className="text-xs">Recomendação</Badge>
      case 'educacional': return <Badge variant="outline" className="text-xs">Educativo</Badge>
      case 'suporte': return <Badge variant="destructive" className="text-xs">Suporte</Badge>
      default: return null
    }
  }

  if (isMinimized) {
    return (
      <div className="fixed bottom-6 right-6 z-50">
        <Button
          onClick={() => setIsMinimized(false)}
          className="rounded-full h-14 w-14 bg-gradient-primary shadow-glow animate-pulse"
          size="icon"
        >
          <Bot className="h-6 w-6" />
        </Button>
      </div>
    )
  }

  return (
    <TooltipProvider>
      <Card className={cn(
        "fixed bottom-6 right-6 z-50 flex flex-col bg-gradient-surface border-border/50 shadow-large transition-all duration-300",
        "w-[500px] h-[600px]"
      )}>
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-border/50">
          <div className="flex items-center space-x-3">
            <Avatar className="h-8 w-8 bg-gradient-primary animate-pulse">
              <AvatarFallback className="text-primary-foreground font-semibold">
                N
              </AvatarFallback>
            </Avatar>
            <div>
              <h3 className="font-semibold text-foreground">Nina</h3>
              <div className="flex items-center space-x-2">
                <p className="text-xs text-muted-foreground">IA Assistente</p>
                <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
              </div>
            </div>
          </div>
          <div className="flex items-center space-x-1">
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => setIsMinimized(true)}
                  className="h-8 w-8"
                >
                  <Minimize2 className="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent>
                Minimizar
              </TooltipContent>
            </Tooltip>
          </div>
        </div>

        {/* Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col">
          <TabsList className="grid w-full grid-cols-2 mx-4 mt-2 mb-1">
            <TabsTrigger value="chat" className="text-xs">
              <MessageCircle className="h-3 w-3 mr-1" />
              Chat
            </TabsTrigger>
            <TabsTrigger value="history" className="text-xs">
              <History className="h-3 w-3 mr-1" />
              Histórico
            </TabsTrigger>
          </TabsList>

          <TabsContent value="chat" className="flex-1 flex flex-col m-0 mt-1">
            {/* Messages */}
            <ScrollArea className="flex-1 p-4" style={{height: "calc(100% - 120px)"}}>
              <div className="space-y-4">
                {messages.map((message) => (
                  <div
                    key={message.id}
                    className={cn(
                      "flex flex-col",
                      message.isBot ? "items-start" : "items-end"
                    )}
                  >
                    <div className="flex items-start space-x-2 max-w-[85%]">
                      {message.isBot && (
                        <Avatar className="h-6 w-6 bg-gradient-primary">
                          <AvatarFallback className="text-xs">
                            {getMessageTypeIcon(message.type)}
                          </AvatarFallback>
                        </Avatar>
                      )}
                      <div className="flex flex-col space-y-1">
                        <div className="flex items-center space-x-2">
                          {message.type && getMessageTypeBadge(message.type)}
                          <span className="text-xs text-muted-foreground">
                            {message.timestamp.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>
                        <div
                          className={cn(
                            "rounded-lg p-3 text-sm",
                            message.isBot
                              ? "bg-surface text-foreground border border-border/50"
                              : "bg-gradient-primary text-primary-foreground"
                          )}
                        >
                          {message.content}
                        </div>
                        {message.isBot && (
                          <div className="flex items-center space-x-1 mt-1">
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="h-6 w-6 p-0"
                                  onClick={() => avaliarResposta(message.id, 5)}
                                >
                                  <ThumbsUp className={cn(
                                    "h-3 w-3",
                                    message.rating === 5 && "text-green-500"
                                  )} />
                                </Button>
                              </TooltipTrigger>
                              <TooltipContent>Gostei</TooltipContent>
                            </Tooltip>
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="h-6 w-6 p-0"
                                  onClick={() => avaliarResposta(message.id, 1)}
                                >
                                  <ThumbsDown className={cn(
                                    "h-3 w-3",
                                    message.rating === 1 && "text-red-500"
                                  )} />
                                </Button>
                              </TooltipTrigger>
                              <TooltipContent>Não gostei</TooltipContent>
                            </Tooltip>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
                
                {isTyping && (
                  <div className="flex justify-start">
                    <div className="flex items-start space-x-2">
                      <Avatar className="h-6 w-6 bg-gradient-primary">
                        <AvatarFallback className="text-xs">
                          <Bot className="h-3 w-3" />
                        </AvatarFallback>
                      </Avatar>
                      <div className="bg-surface text-foreground rounded-lg p-3 text-sm border border-border/50">
                        <div className="flex space-x-1">
                          <div className="w-2 h-2 bg-muted-foreground rounded-full animate-bounce"></div>
                          <div className="w-2 h-2 bg-muted-foreground rounded-full animate-bounce" style={{ animationDelay: "0.1s" }}></div>
                          <div className="w-2 h-2 bg-muted-foreground rounded-full animate-bounce" style={{ animationDelay: "0.2s" }}></div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>
            </ScrollArea>
          </TabsContent>

          <TabsContent value="history" className="flex-1 flex flex-col m-0 mt-1">
            <ScrollArea className="flex-1 p-4" style={{height: "calc(100% - 120px)"}}>
              {loadingHistory ? (
                <div className="flex justify-center py-8">
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                </div>
              ) : (
                <div className="space-y-3">
                  {historico.map((item) => (
                    <div key={item.id} className="border border-border/50 rounded-lg p-3 space-y-2">
                      <div className="flex items-center justify-between">
                        <Badge variant="outline" className="text-xs">
                          {item.tipo.replace('_', ' ').toLowerCase()}
                        </Badge>
                        <div className="flex items-center space-x-1 text-xs text-muted-foreground">
                          <Clock className="h-3 w-3" />
                          {new Date(item.criadoEm).toLocaleDateString('pt-BR')}
                        </div>
                      </div>
                      <p className="text-sm font-medium">{item.pergunta}</p>
                      <p className="text-xs text-muted-foreground line-clamp-2">
                        {item.resposta.substring(0, 100)}...
                      </p>
                      {item.avaliacaoUsuario && (
                        <div className="flex items-center space-x-1">
                          <Star className="h-3 w-3 text-yellow-500" />
                          <span className="text-xs">{item.avaliacaoUsuario}/5</span>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </ScrollArea>
          </TabsContent>
        </Tabs>

        {/* Input */}
        <div className="p-4 border-t border-border/50">
          <div className="flex space-x-2">
            <Input
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={handleKeyPress}
              placeholder="Digite sua pergunta... (Shift+Enter para nova linha)"
              className="flex-1 bg-surface border-border/50"
              disabled={isTyping}
            />
            <Button
              onClick={handleSendMessage}
              size="icon"
              className="bg-gradient-primary"
              disabled={!inputValue.trim() || isTyping}
            >
              {isTyping ? (
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-primary-foreground" />
              ) : (
                <Send className="h-4 w-4" />
              )}
            </Button>
          </div>
        </div>
      </Card>
    </TooltipProvider>
  )
}