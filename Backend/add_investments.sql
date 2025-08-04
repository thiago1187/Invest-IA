-- Script para adicionar investimentos ao usuário teste@teste.com

-- Primeiro, buscar o ID do usuário
-- SELECT id FROM usuarios WHERE email = 'teste@teste.com';

-- IDs que vamos usar (substitua o primeiro pelo ID real do usuário)
-- Usuario ID: 982764fa-be35-49e5-af1f-0ba9bd648c3b

-- Inserir ativos se não existem
INSERT INTO ativos (id, ticker, simbolo, nome, tipo_ativo, setor, preco_atual, status, criado_em) VALUES
('11111111-1111-1111-1111-111111111111', 'PETR4', 'PETR4.SA', 'Petrobras PN', 'ACAO', 'ENERGIA', 32.45, true, CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'VALE3', 'VALE3.SA', 'Vale ON', 'ACAO', 'MATERIAIS_BASICOS', 58.12, true, CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'ITUB4', 'ITUB4.SA', 'Itaú Unibanco PN', 'ACAO', 'FINANCEIRO', 30.25, true, CURRENT_TIMESTAMP)
ON CONFLICT (ticker) DO NOTHING;

-- Inserir investimentos para o usuário teste@teste.com
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em) VALUES
('cccccccc-cccc-cccc-cccc-cccccccccccc', '982764fa-be35-49e5-af1f-0ba9bd648c3b', '11111111-1111-1111-1111-111111111111', 100, 32.50, 32.45, 3250.00, '2024-01-15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dddddddd-dddd-dddd-dddd-dddddddddddd', '982764fa-be35-49e5-af1f-0ba9bd648c3b', '22222222-2222-2222-2222-222222222222', 50, 55.00, 58.12, 2750.00, '2024-02-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '982764fa-be35-49e5-af1f-0ba9bd648c3b', '33333333-3333-3333-3333-333333333333', 150, 28.00, 30.25, 4200.00, '2024-03-05', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);