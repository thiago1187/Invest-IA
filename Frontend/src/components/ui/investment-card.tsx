import { cn } from "@/lib/utils"
import { Card } from "@/components/ui/card"
import { TrendingUp, TrendingDown, DollarSign } from "lucide-react"

interface InvestmentCardProps {
  title: string
  value: string
  change: string
  changeType: "positive" | "negative" | "neutral"
  icon?: React.ReactNode
  className?: string
}

export function InvestmentCard({ 
  title, 
  value, 
  change, 
  changeType,
  icon = <DollarSign className="h-5 w-5" />,
  className 
}: InvestmentCardProps) {
  const getTrendIcon = () => {
    if (changeType === "positive") return <TrendingUp className="h-4 w-4 text-success" />
    if (changeType === "negative") return <TrendingDown className="h-4 w-4 text-destructive" />
    return null
  }

  const getChangeColor = () => {
    if (changeType === "positive") return "text-success"
    if (changeType === "negative") return "text-destructive"
    return "text-muted-foreground"
  }

  return (
    <Card className={cn(
      "p-6 bg-gradient-surface border-border/50 hover-lift hover-glow transition-smooth", 
      className
    )}>
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-lg bg-primary/10 text-primary">
            {icon}
          </div>
          <div>
            <p className="text-sm font-medium text-muted-foreground">{title}</p>
            <p className="text-2xl font-bold text-foreground">{value}</p>
          </div>
        </div>
        <div className="flex items-center space-x-1">
          {getTrendIcon()}
          <span className={cn("text-sm font-medium", getChangeColor())}>
            {change}
          </span>
        </div>
      </div>
    </Card>
  )
}