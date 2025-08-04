-- Debug query para verificar os dados

-- 1. Verificar se o usuário existe
SELECT 'USUARIO:' as tipo, id, email FROM usuarios WHERE email = 'teste@teste.com';

-- 2. Verificar investimentos para este usuário
SELECT 'INVESTIMENTOS:' as tipo, i.*, a.ticker, a.nome 
FROM investimentos i 
LEFT JOIN ativos a ON i.ativo_id = a.id 
WHERE i.usuario_id = '982764fa-be35-49e5-af1f-0ba9bd648c3b';

-- 3. Testar a query específica do dashboard
SELECT 'DASHBOARD_QUERY:' as tipo, i.quantidade, i.valor_total_investido, i.valor_medio_compra, a.ticker, a.nome 
FROM investimentos i 
JOIN ativos a ON i.ativo_id = a.id 
WHERE i.usuario_id = '982764fa-be35-49e5-af1f-0ba9bd648c3b' AND i.ativo_status = true;