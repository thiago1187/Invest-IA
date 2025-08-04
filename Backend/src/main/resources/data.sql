-- Inserir dados de teste para InvestIA com tratamento de duplicatas

-- Limpar dados existentes (se houver)
DELETE FROM investimentos WHERE id IN ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'ffffffff-ffff-ffff-ffff-ffffffffffff', '11111111-2222-3333-4444-555555555555', '22222222-3333-4444-5555-666666666666', '33333333-4444-5555-6666-777777777777');
DELETE FROM perfis WHERE id IN ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'dddddddd-dddd-dddd-dddd-dddddddddddd');
DELETE FROM usuarios WHERE id IN ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-cccccccccccc', 'cccccccc-cccc-cccc-cccc-cccccccccccc');
DELETE FROM ativos WHERE id IN ('11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444');

-- DADOS EXATOS FORNECIDOS PELO USUÁRIO - NÃO ALTERAR
INSERT INTO ativos (id, ticker, simbolo, nome, tipo_ativo, setor, preco_atual, status, criado_em) VALUES
('11111111-1111-1111-1111-111111111111', 'PETR4', 'PETR4.SA', 'Petrobras PN', 'ACAO', 'ENERGIA', 32.21, true, CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'VALE3', 'VALE3.SA', 'Vale ON', 'ACAO', 'MATERIAIS_BASICOS', 53.75, true, CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'ITUB4', 'ITUB4.SA', 'Itaú Unibanco PN', 'ACAO', 'FINANCEIRO', 34.93, true, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444444', 'BBAS3', 'BBAS3.SA', 'Banco do Brasil ON', 'ACAO', 'FINANCEIRO', 18.35, true, CURRENT_TIMESTAMP),
('55555555-5555-5555-5555-555555555555', 'ABEV3', 'ABEV3.SA', 'Ambev ON', 'ACAO', 'CONSUMO', 12.29, true, CURRENT_TIMESTAMP),
('66666666-6666-6666-6666-666666666666', 'CSNA3', 'CSNA3.SA', 'CSN ON', 'ACAO', 'MATERIAIS_BASICOS', 7.62, true, CURRENT_TIMESTAMP),
('77777777-7777-7777-7777-777777777777', 'GGBR4', 'GGBR4.SA', 'Gerdau PN', 'ACAO', 'MATERIAIS_BASICOS', 16.05, true, CURRENT_TIMESTAMP),
('88888888-8888-8888-8888-888888888888', 'ITSA4', 'ITSA4.SA', 'Itaúsa PN', 'ACAO', 'FINANCEIRO', 10.34, true, CURRENT_TIMESTAMP),
('99999999-9999-9999-9999-999999999999', 'BOVA11', 'BOVA11.SA', 'iShares Ibovespa ETF', 'ACAO', 'FINANCEIRO', 129.57, true, CURRENT_TIMESTAMP);

-- Inserir usuários de teste (senha: 123456)
INSERT INTO usuarios (id, nome, email, senha, ativo, criado_em) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Thiago Alves', 'teste@investia.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', true, CURRENT_TIMESTAMP),
('bbbbbbbb-bbbb-bbbb-bbbb-cccccccccccc', 'Usuário Teste 2', 'teste@teste.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', true, CURRENT_TIMESTAMP),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Thiago Alves', 'novo@teste.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', true, CURRENT_TIMESTAMP);

-- Inserir perfil dos usuários de teste  
INSERT INTO perfis (id, usuario_id, tipo_perfil, nivel_experiencia, tolerancia_risco, criado_em) VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'MODERADO', 'INTERMEDIARIO', 7, CURRENT_TIMESTAMP),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'MODERADO', 'INTERMEDIARIO', 6, CURRENT_TIMESTAMP);

-- DADOS CORRETOS COM COTAÇÕES EXATAS DO USUÁRIO
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES
-- Investimentos para teste@investia.com (CÁLCULOS CORRETOS)
-- BBAS3: 1000 ações × R$18,35 = R$18.350 (investido R$19.000)
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444', 1000, 19.00, 18.35, 19000.00, '2024-01-15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- PETR4: 1000 ações × R$32,21 = R$32.210 (investido R$32.000)  
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 1000, 32.00, 32.21, 32000.00, '2024-02-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Investimentos para teste@teste.com (portfolio pequeno)
('11111111-2222-3333-4444-555555555555', 'bbbbbbbb-bbbb-bbbb-bbbb-cccccccccccc', '11111111-1111-1111-1111-111111111111', 100, 31.00, 32.21, 3100.00, '2024-01-20', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Investimentos para novo@teste.com (seu usuário autenticado)
('22222222-3333-4444-5555-666666666666', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '44444444-4444-4444-4444-444444444444', 500, 19.50, 18.35, 9750.00, '2024-03-01', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-4444-5555-6666-777777777777', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111', 300, 31.00, 32.21, 9300.00, '2024-03-15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);