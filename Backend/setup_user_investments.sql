-- Inserir dados de investimento para o usuário novo@teste.com
-- Primeiro criar um perfil para o usuário
INSERT INTO perfis (id, usuario_id, tipo_perfil, nivel_experiencia, tolerancia_risco, criado_em) 
SELECT 
    'novo-profile-id-12345678-1234-1234-1234-123456789012', 
    u.id, 
    'MODERADO', 
    'INTERMEDIARIO', 
    0.6, 
    CURRENT_TIMESTAMP
FROM usuarios u 
WHERE u.email = 'novo@teste.com'
AND NOT EXISTS (SELECT 1 FROM perfis p WHERE p.usuario_id = u.id);

-- Inserir investimentos para o usuário novo@teste.com
INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em)
SELECT 
    'inv-novo-bbas3-12345678-1234-1234-1234-123456789012',
    u.id,
    '44444444-4444-4444-4444-444444444444', -- BBAS3
    500,
    19.50,
    18.35,
    9750.00,
    '2024-03-01',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM usuarios u 
WHERE u.email = 'novo@teste.com'
AND NOT EXISTS (SELECT 1 FROM investimentos i WHERE i.usuario_id = u.id AND i.ativo_id = '44444444-4444-4444-4444-444444444444');

INSERT INTO investimentos (id, usuario_id, ativo_id, quantidade, valor_medio_compra, valor_atual, valor_total_investido, data_compra, ativo_status, criado_em, atualizado_em)
SELECT 
    'inv-novo-petr4-12345678-1234-1234-1234-123456789012',
    u.id,
    '11111111-1111-1111-1111-111111111111', -- PETR4
    300,
    31.00,
    32.21,
    9300.00,
    '2024-03-15',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM usuarios u 
WHERE u.email = 'novo@teste.com'
AND NOT EXISTS (SELECT 1 FROM investimentos i WHERE i.usuario_id = u.id AND i.ativo_id = '11111111-1111-1111-1111-111111111111');