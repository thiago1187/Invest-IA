import { useState, useRef, useEffect } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Bot, Send, Minimize2, Maximize2 } from "lucide-react"
import { cn } from "@/lib/utils"

interface Message {
  id: string
  content: string
  isBot: boolean
  timestamp: Date
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

const mockResponses = [
  "Baseado no seu perfil, recomendo diversificar entre renda fixa e variável.",
  "O mercado está apresentando boas oportunidades em fundos imobiliários.",
  "Que tal aumentarmos sua reserva de emergência antes de novos investimentos?",
  "Identifiquei uma oportunidade interessante em ações de tecnologia.",
  "Seus investimentos estão bem balanceados! Continue assim."
]

export function ChatBot({ userProfile = "moderado" }: ChatBotProps) {
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
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const handleSendMessage = async () => {
    if (!inputValue.trim()) return

    const userMessage: Message = {
      id: Date.now().toString(),
      content: inputValue,
      isBot: false,
      timestamp: new Date()
    }

    setMessages(prev => [...prev, userMessage])
    setInputValue("")
    setIsTyping(true)

    // Simular resposta da IA
    setTimeout(() => {
      const botResponse: Message = {
        id: (Date.now() + 1).toString(),
        content: mockResponses[Math.floor(Math.random() * mockResponses.length)],
        isBot: true,
        timestamp: new Date()
      }
      setMessages(prev => [...prev, botResponse])
      setIsTyping(false)
    }, 1500)
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
    <Card className="fixed bottom-6 right-6 w-80 h-96 z-50 flex flex-col bg-gradient-surface border-border/50 shadow-large">
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-border/50">
        <div className="flex items-center space-x-3">
          <Avatar className="h-8 w-8 bg-gradient-primary">
            <AvatarFallback className="text-primary-foreground font-semibold">
              N
            </AvatarFallback>
          </Avatar>
          <div>
            <h3 className="font-semibold text-foreground">Nina</h3>
            <p className="text-xs text-muted-foreground">IA Assistente</p>
          </div>
        </div>
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setIsMinimized(true)}
          className="h-8 w-8"
        >
          <Minimize2 className="h-4 w-4" />
        </Button>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((message) => (
          <div
            key={message.id}
            className={cn(
              "flex",
              message.isBot ? "justify-start" : "justify-end"
            )}
          >
            <div
              className={cn(
                "max-w-[80%] rounded-lg p-3 text-sm",
                message.isBot
                  ? "bg-surface text-foreground"
                  : "bg-gradient-primary text-primary-foreground"
              )}
            >
              {message.content}
            </div>
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
            disabled={!inputValue.trim()}
          >
            <Send className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </Card>
  )
}