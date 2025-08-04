-- ============================================================================
-- BANCO DE DADOS INVESTIA - SCRIPT SIMPLES E FUNCIONAL
-- ============================================================================

-- Limpar dados existentes
DELETE FROM investimentos;
DELETE FROM perfis; 
DELETE FROM ativos;
DELETE FROM usuarios;

-- ============================================================================
-- 1. USUÁRIOS DE TESTE
-- ============================================================================
INSERT INTO usuarios (id, nome, email, senha, telefone, ativo, criado_em, atualizado_em) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'João Investidor', 'joao@investia.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '11999999999', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Maria Trader', 'maria@investia.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '11888888888', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 2. PERFIS DE RISCO DOS USUÁRIOS
-- ============================================================================
INSERT INTO perfis (id, usuario_id, tipo_perfil, nivel_experiencia, tolerancia_risco, horizonte_investimento, pontuacao_simulado, respostas_simulado, criado_em, atualizado_em) VALUES
('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'MODERADO', 'INTERMEDIARIO', 7, 24, 75, '{"q1":"B","q2":"C","q3":"B","q4":"A","q5":"C"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'AGRESSIVO', 'AVANCADO', 9, 60, 92, '{"q1":"C","q2":"C","q3":"C","q4":"C","q5":"C"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 3. ATIVOS FINANCEIROS
-- ============================================================================
INSERT INTO ativos (id, ticker, simbolo, nome, tipo_ativo, setor, preco_atual, variacao_diaria, variacao_mensal, variacao_anual, risco, status, descricao, criado_em, ultima_atualizacao) VALUES
-- Ações Brasileiras
('a1111111-1111-1111-1111-111111111111', 'PETR4.SA', 'PETR4', 'Petrobras PN', 'ACAO', 'ENERGIA', 32.45, -1.23, 4.56, 18.90, 0.65, true, 'Maior empresa de energia do Brasil', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a2222222-2222-2222-2222-222222222222', 'VALE3.SA', 'VALE3', 'Vale ON', 'ACAO', 'MATERIAIS_BASICOS', 58.12, 2.34, -2.10, 25.67, 0.70, true, 'Maior mineradora das Américas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a3333333-3333-3333-3333-333333333333', 'ITUB4.SA', 'ITUB4', 'Itaú Unibanco PN', 'ACAO', 'FINANCEIRO', 30.25, 0.89, 3.45, 12.34, 0.45, true, 'Maior banco privado do Brasil', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a4444444-4444-4444-4444-444444444444', 'BBAS3.SA', 'BBAS3', 'Banco do Brasil ON', 'ACAO', 'FINANCEIRO', 47.83, 1.45, 5.67, 15.23, 0.50, true, 'Maior banco público do Brasil', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Ações Americanas
('b1111111-1111-1111-1111-111111111111', 'AAPL', 'AAPL', 'Apple Inc', 'ACAO', 'TECNOLOGIA', 182.50, 1.25, 8.90, 32.45, 0.40, true, 'Maior empresa de tecnologia do mundo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b2222222-2222-2222-2222-222222222222', 'MSFT', 'MSFT', 'Microsoft Corp', 'ACAO', 'TECNOLOGIA', 415.30, 0.78, 12.34, 28.67, 0.38, true, 'Líder em software e cloud computing', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Criptomoedas
('c1111111-1111-1111-1111-111111111111', 'BTC-USD', 'BTC', 'Bitcoin', 'CRIPTO', 'CRIPTO', 67850.00, 4.56, 23.45, 89.67, 0.90, true, 'Primeira e maior criptomoeda do mundo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c2222222-2222-2222-2222-222222222222', 'ETH-USD', 'ETH', 'Ethereum', 'CRIPTO', 'CRIPTO', 3420.50, 3.78, 18.90, 78.45, 0.85, true, 'Segunda maior criptomoeda', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 4. INVESTIMENTOS DOS USUÁRIOS
-- ============================================================================

-- INVESTIMENTOS DO JOÃO
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES
('i1111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'a1111111-1111-1111-1111-111111111111', 100, 31.50, 32.45, 3150.00, '2024-01-15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('i2222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'a2222222-2222-2222-2222-222222222222', 50, 55.00, 58.12, 2750.00, '2024-02-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('i3333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'a3333333-3333-3333-3333-333333333333', 150, 28.00, 30.25, 4200.00, '2024-03-05', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('i4444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'b1111111-1111-1111-1111-111111111111', 10, 175.00, 182.50, 1750.00, '2024-04-12', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- INVESTIMENTOS DA MARIA
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES
('j1111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'a4444444-4444-4444-4444-444444444444', 200, 45.00, 47.83, 9000.00, '2024-01-20', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('j2222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c1111111-1111-1111-1111-111111111111', 1, 65000.00, 67850.00, 65000.00, '2024-02-15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('j3333333-3333-3333-3333-333333333333', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c2222222-2222-2222-2222-222222222222', 2, 3200.00, 3420.50, 6400.00, '2024-03-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

COMMIT;