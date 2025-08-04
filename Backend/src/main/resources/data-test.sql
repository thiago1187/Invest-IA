-- Inserir usuário de teste para frontend
INSERT INTO usuarios (id, nome, email, senha, telefone, status, criado_em, atualizado_em) 
VALUES (
    'b3a6f8e4-4c5e-4d6f-8e9a-1b2c3d4e5f6a',
    'Frontend User', 
    'frontend.user@investia.com', 
    '$2a$10$N.zmdr.0LV4s.tDASYEeq.DaXgwkRNIjhLLG4cNrYTAMI5ym6dQBK', -- senha: frontend123
    '11999887766',
    true,
    NOW(),
    NOW()
);

-- Inserir alguns ativos de teste
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, descricao, preco_atual, variacao_diaria, status, ultima_atualizacao)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'Itaú Unibanco', 'ITUB4', 'ITUB4', 'ACAO', 'FINANCEIRO', 'Banco Itaú Unibanco S.A.', 35.50, 1.25, true, NOW()),
    ('550e8400-e29b-41d4-a716-446655440002', 'Petrobras', 'PETR4', 'PETR4', 'ACAO', 'ENERGIA', 'Petróleo Brasileiro S.A.', 42.80, -0.75, true, NOW()),
    ('550e8400-e29b-41d4-a716-446655440003', 'Vale', 'VALE3', 'VALE3', 'ACAO', 'MINERACAO', 'Vale S.A.', 65.30, 2.10, true, NOW());

-- Inserir alguns investimentos para o usuário
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_compra, valor_total_investido, valor_atual, data_compra, status)
VALUES 
    ('650e8400-e29b-41d4-a716-446655440001', 'b3a6f8e4-4c5e-4d6f-8e9a-1b2c3d4e5f6a', '550e8400-e29b-41d4-a716-446655440001', 100, 30.00, 3000.00, 35.50, '2024-01-15', true),
    ('650e8400-e29b-41d4-a716-446655440002', 'b3a6f8e4-4c5e-4d6f-8e9a-1b2c3d4e5f6a', '550e8400-e29b-41d4-a716-446655440002', 50, 45.00, 2250.00, 42.80, '2024-02-10', true),
    ('650e8400-e29b-41d4-a716-446655440003', 'b3a6f8e4-4c5e-4d6f-8e9a-1b2c3d4e5f6a', '550e8400-e29b-41d4-a716-446655440003', 75, 60.00, 4500.00, 65.30, '2024-03-05', true);