import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/contexts/AuthContext"

export function DashboardDebug() {
  const [data, setData] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { user } = useAuth()

  const testDashboard = async () => {
    if (!user) return
    
    setLoading(true)
    setError(null)
    
    try {
      const token = localStorage.getItem('token')
      console.log('Token:', token)
      
      const response = await fetch('http://localhost:8080/api/dashboard-fixed', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      console.log('Response status:', response.status)
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
      
      const result = await response.json()
      console.log('Dashboard Data:', result)
      setData(result)
    } catch (err: any) {
      console.error('Dashboard Error:', err)
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (user?.id) {
      testDashboard()
    }
  }, [user?.id])

  if (!user) {
    return <div>Usuário não autenticado</div>
  }

  return (
    <Card className="w-full max-w-4xl mx-auto">
      <CardHeader>
        <CardTitle>Dashboard Debug</CardTitle>
        <Button onClick={testDashboard} disabled={loading}>
          {loading ? 'Carregando...' : 'Recarregar Dashboard'}
        </Button>
      </CardHeader>
      <CardContent>
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            <strong>Erro:</strong> {error}
          </div>
        )}
        
        {data && (
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="bg-blue-50 p-4 rounded">
                <h3 className="font-semibold">Valor Total</h3>
                <p className="text-2xl font-bold">
                  R$ {data.resumoCarteira?.valorTotal?.toLocaleString('pt-BR') || '0'}
                </p>
              </div>
              <div className="bg-green-50 p-4 rounded">
                <h3 className="font-semibold">Lucro/Prejuízo</h3>
                <p className="text-2xl font-bold">
                  R$ {data.resumoCarteira?.lucroPreju?.toLocaleString('pt-BR') || '0'}
                </p>
              </div>
              <div className="bg-purple-50 p-4 rounded">
                <h3 className="font-semibold">Percentual</h3>
                <p className="text-2xl font-bold">
                  {data.resumoCarteira?.percentualLucroPreju?.toFixed(2) || '0'}%
                </p>
              </div>
              <div className="bg-orange-50 p-4 rounded">
                <h3 className="font-semibold">Total Ativos</h3>
                <p className="text-2xl font-bold">
                  {data.resumoCarteira?.totalAtivos || '0'}
                </p>
              </div>
            </div>
            
            <div className="bg-gray-50 p-4 rounded">
              <h3 className="font-semibold mb-2">Evolução Patrimonial</h3>
              <p>Dados de evolução: {data.performance?.evolucaoPatrimonio?.length || 0} pontos</p>
              {data.performance?.evolucaoPatrimonio?.slice(-5).map((item: any, index: number) => (
                <div key={index} className="text-sm">
                  {new Date(item.data).toLocaleDateString('pt-BR')}: R$ {Number(item.valor).toLocaleString('pt-BR')}
                </div>
              ))}
            </div>
            
            <div className="bg-gray-50 p-4 rounded">
              <h3 className="font-semibold mb-2">Distribuição</h3>
              <pre className="bg-white p-2 rounded text-xs overflow-auto">
                {JSON.stringify(data.distribuicaoAtivos, null, 2)}
              </pre>
            </div>
            
            <div className="bg-gray-50 p-4 rounded">
              <h3 className="font-semibold mb-2">JSON Completo</h3>
              <pre className="bg-white p-2 rounded text-xs overflow-auto max-h-96">
                {JSON.stringify(data, null, 2)}
              </pre>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}