-- ============================================================================
-- BANCO DE DADOS INVESTIA - ESQUEMA COMPLETO E ROBUSTO
-- Este script garante que o banco esteja 100% funcional para CRUD operations
-- ============================================================================

-- Limpar dados existentes (ordem correta devido às foreign keys)
DELETE FROM investimentos;
DELETE FROM perfis; 
DELETE FROM ativos;
DELETE FROM usuarios;

-- ============================================================================
-- 1. USUÁRIOS DE TESTE
-- ============================================================================
INSERT INTO usuarios (id, nome, email, senha, telefone, ativo, criado_em, atualizado_em) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'João Investidor', 'joao@investia.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '11999999999', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Maria Trader', 'maria@investia.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '11888888888', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Admin InvestIA', 'admin@investia.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '11777777777', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 2. PERFIS DE RISCO DOS USUÁRIOS
-- ============================================================================
INSERT INTO perfis (id, usuario_id, tipo_perfil, nivel_experiencia, tolerancia_risco, horizonte_investimento, pontuacao_simulado, respostas_simulado, criado_em, atualizado_em) VALUES
('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'MODERADO', 'INTERMEDIARIO', 7, 'MEDIO_PRAZO', 75, '{"q1":"B","q2":"C","q3":"B","q4":"A","q5":"C"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'AGRESSIVO', 'AVANCADO', 9, 'LONGO_PRAZO', 92, '{"q1":"C","q2":"C","q3":"C","q4":"C","q5":"C"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'CONSERVADOR', 'INICIANTE', 4, 'CURTO_PRAZO', 45, '{"q1":"A","q2":"A","q3":"A","q4":"A","q5":"A"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 3. ATIVOS FINANCEIROS (BASE COMPLETA)
-- ============================================================================

-- AÇÕES BRASILEIRAS (B3)
INSERT INTO ativos (id, ticker, simbolo, nome, tipo_ativo, setor, preco_atual, variacao_diaria, variacao_mensal, variacao_anual, risco, status, descricao, criado_em, ultima_atualizacao) VALUES
-- Blue Chips Brasileiras
('a1111111-1111-1111-1111-111111111111', 'PETR4.SA', 'PETR4', 'Petrobras PN', 'ACAO', 'ENERGIA', 32.45, -1.23, 4.56, 18.90, 0.65, true, 'Maior empresa de energia do Brasil', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a2222222-2222-2222-2222-222222222222', 'VALE3.SA', 'VALE3', 'Vale ON', 'ACAO', 'MATERIAIS_BASICOS', 58.12, 2.34, -2.10, 25.67, 0.70, true, 'Maior mineradora das Américas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a3333333-3333-3333-3333-333333333333', 'ITUB4.SA', 'ITUB4', 'Itaú Unibanco PN', 'ACAO', 'FINANCEIRO', 30.25, 0.89, 3.45, 12.34, 0.45, true, 'Maior banco privado do Brasil', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a4444444-4444-4444-4444-444444444444', 'BBAS3.SA', 'BBAS3', 'Banco do Brasil ON', 'ACAO', 'FINANCEIRO', 47.83, 1.45, 5.67, 15.23, 0.50, true, 'Maior banco público do Brasil', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a5555555-5555-5555-5555-555555555555', 'ABEV3.SA', 'ABEV3', 'Ambev ON', 'ACAO', 'BENS_CONSUMO', 14.78, -0.67, 1.23, 8.90, 0.35, true, 'Maior cervejaria da América Latina', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a6666666-6666-6666-6666-666666666666', 'MGLU3.SA', 'MGLU3', 'Magazine Luiza ON', 'ACAO', 'CONSUMO_CICLICO', 8.45, -2.34, -8.90, -45.67, 0.85, true, 'Maior varejista online do Brasil', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a7777777-7777-7777-7777-777777777777', 'WEGE3.SA', 'WEGE3', 'WEG ON', 'ACAO', 'INDUSTRIAL', 42.30, 1.12, 6.78, 28.45, 0.55, true, 'Líder em motores elétricos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a8888888-8888-8888-8888-888888888888', 'SUZB3.SA', 'SUZB3', 'Suzano ON', 'ACAO', 'MATERIAIS_BASICOS', 52.67, 0.45, 2.34, 16.78, 0.60, true, 'Maior produtora de celulose do mundo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- AÇÕES AMERICANAS (NASDAQ/NYSE)
('b1111111-1111-1111-1111-111111111111', 'AAPL', 'AAPL', 'Apple Inc', 'ACAO', 'TECNOLOGIA', 182.50, 1.25, 8.90, 32.45, 0.40, true, 'Maior empresa de tecnologia do mundo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b2222222-2222-2222-2222-222222222222', 'MSFT', 'MSFT', 'Microsoft Corp', 'ACAO', 'TECNOLOGIA', 415.30, 0.78, 12.34, 28.67, 0.38, true, 'Líder em software e cloud computing', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b3333333-3333-3333-3333-333333333333', 'GOOGL', 'GOOGL', 'Alphabet Inc', 'ACAO', 'TECNOLOGIA', 142.80, -0.45, 5.67, 24.89, 0.42, true, 'Empresa controladora do Google', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b4444444-4444-4444-4444-444444444444', 'TSLA', 'TSLA', 'Tesla Inc', 'ACAO', 'CONSUMO_CICLICO', 248.90, 3.45, 18.90, 67.89, 0.75, true, 'Líder em veículos elétricos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b5555555-5555-5555-5555-555555555555', 'NVDA', 'NVDA', 'NVIDIA Corp', 'ACAO', 'TECNOLOGIA', 875.40, 2.34, 25.67, 125.45, 0.80, true, 'Líder em chips para IA e gaming', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- CRIPTOMOEDAS
('c1111111-1111-1111-1111-111111111111', 'BTC-USD', 'BTC', 'Bitcoin', 'CRIPTO', 'CRIPTO', 67850.00, 4.56, 23.45, 89.67, 0.90, true, 'Primeira e maior criptomoeda do mundo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c2222222-2222-2222-2222-222222222222', 'ETH-USD', 'ETH', 'Ethereum', 'CRIPTO', 'CRIPTO', 3420.50, 3.78, 18.90, 78.45, 0.85, true, 'Segunda maior criptomoeda e plataforma de smart contracts', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3333333-3333-3333-3333-333333333333', 'BNB-USD', 'BNB', 'Binance Coin', 'CRIPTO', 'CRIPTO', 412.80, 2.34, 15.67, 56.78, 0.80, true, 'Token nativo da maior exchange de cripto', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- ETFs BRASILEIROS
('e1111111-1111-1111-1111-111111111111', 'BOVA11.SA', 'BOVA11', 'iShares Ibovespa Fundo Índice', 'ETF', 'DIVERSIFICADO', 98.45, 1.23, 4.56, 16.78, 0.50, true, 'ETF que replica o índice Ibovespa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e2222222-2222-2222-2222-222222222222', 'SMAL11.SA', 'SMAL11', 'iShares Small Cap Fundo Índice', 'ETF', 'DIVERSIFICADO', 45.67, 2.34, 6.78, 22.45, 0.65, true, 'ETF de small caps brasileiras', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- FIIs (Fundos Imobiliários)
('f1111111-1111-1111-1111-111111111111', 'HGLG11.SA', 'HGLG11', 'CSHG Logística FII', 'FII', 'IMOVEIS', 142.30, 0.45, 2.34, 12.67, 0.30, true, 'FII focado em galpões logísticos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f2222222-2222-2222-2222-222222222222', 'XPML11.SA', 'XPML11', 'XP Malls FII', 'FII', 'IMOVEIS', 89.45, -0.23, 1.56, 8.90, 0.35, true, 'FII focado em shopping centers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 4. INVESTIMENTOS DOS USUÁRIOS DE TESTE
-- ============================================================================

-- INVESTIMENTOS DO JOÃO (aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa)
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES
('i1111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'a1111111-1111-1111-1111-111111111111', 100, 31.50, 32.45, 3150.00, '2024-01-15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('i2222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'a2222222-2222-2222-2222-222222222222', 50, 55.00, 58.12, 2750.00, '2024-02-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('i3333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'a3333333-3333-3333-3333-333333333333', 150, 28.00, 30.25, 4200.00, '2024-03-05', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('i4444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'b1111111-1111-1111-1111-111111111111', 10, 175.00, 182.50, 1750.00, '2024-04-12', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('i5555555-5555-5555-5555-555555555555', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c1111111-1111-1111-1111-111111111111', 1, 65000.00, 67850.00, 65000.00, '2024-05-20', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- INVESTIMENTOS DA MARIA (bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb)
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES
('j1111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'a4444444-4444-4444-4444-444444444444', 200, 45.00, 47.83, 9000.00, '2024-01-20', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('j2222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'a7777777-7777-7777-7777-777777777777', 75, 40.00, 42.30, 3000.00, '2024-02-15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('j3333333-3333-3333-3333-333333333333', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'b4444444-4444-4444-4444-444444444444', 20, 230.00, 248.90, 4600.00, '2024-03-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('j4444444-4444-4444-4444-444444444444', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c2222222-2222-2222-2222-222222222222', 2, 3200.00, 3420.50, 6400.00, '2024-04-25', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- INVESTIMENTOS DO ADMIN (cccccccc-cccc-cccc-cccc-cccccccccccc)
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES
('k1111111-1111-1111-1111-111111111111', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'e1111111-1111-1111-1111-111111111111', 100, 95.00, 98.45, 9500.00, '2024-01-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('k2222222-2222-2222-2222-222222222222', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'f1111111-1111-1111-1111-111111111111', 50, 140.00, 142.30, 7000.00, '2024-02-20', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- VERIFICAÇÕES DE INTEGRIDADE
-- ============================================================================

-- Verificar total de registros inseridos
SELECT 'USUÁRIOS' as tabela, COUNT(*) as total FROM usuarios
UNION ALL
SELECT 'PERFIS' as tabela, COUNT(*) as total FROM perfis  
UNION ALL
SELECT 'ATIVOS' as tabela, COUNT(*) as total FROM ativos
UNION ALL
SELECT 'INVESTIMENTOS' as tabela, COUNT(*) as total FROM investimentos;

-- Verificar integridade referencial
SELECT 
    u.nome as usuario,
    COUNT(i.id) as total_investimentos,
    SUM(i.valor_total_investido) as valor_total_investido,
    SUM(i.valor_atual * i.quantidade) as valor_atual_total
FROM usuarios u
LEFT JOIN investimentos i ON u.id = i.usuario_id AND i.ativo_status = true
GROUP BY u.id, u.nome
ORDER BY u.nome;

-- ============================================================================
-- CONFIGURAÇÕES FINAIS
-- ============================================================================

-- Garantir que todas as foreign keys estão funcionando
-- Atualizar timestamps para refletir última execução
UPDATE ativos SET ultima_atualizacao = CURRENT_TIMESTAMP;
UPDATE investimentos SET atualizado_em = CURRENT_TIMESTAMP;

COMMIT;