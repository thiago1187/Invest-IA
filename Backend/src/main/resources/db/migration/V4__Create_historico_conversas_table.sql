-- Tabela para histórico de conversas do ChatBot
CREATE TABLE historico_conversas (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    usuario_id UUID NOT NULL,
    pergunta TEXT NOT NULL,
    resposta TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('PERGUNTA_GERAL', 'ANALISE_CARTEIRA', 'RECOMENDACOES', 'EDUCACIONAL', 'SUPORTE')),
    contexto_carteira TEXT,
    avaliacao_usuario INTEGER CHECK (avaliacao_usuario BETWEEN 1 AND 5),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tempo_resposta_ms BIGINT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índices para otimizar consultas
CREATE INDEX idx_historico_conversas_usuario_id ON historico_conversas(usuario_id);
CREATE INDEX idx_historico_conversas_criado_em ON historico_conversas(criado_em DESC);
CREATE INDEX idx_historico_conversas_tipo ON historico_conversas(tipo);
CREATE INDEX idx_historico_conversas_avaliacao ON historico_conversas(avaliacao_usuario) WHERE avaliacao_usuario IS NOT NULL;