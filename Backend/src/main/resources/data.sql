-- Dados iniciais para a aplicação InvestIA

-- Inserir ativos populares da B3
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(gen_random_uuid(), 'Vale S.A.', 'VALE3', 'VALE3.SA', 'ACAO', 'MINERACAO', true),
(gen_random_uuid(), 'Petróleo Brasileiro S.A.', 'PETR4', 'PETR4.SA', 'ACAO', 'ENERGIA', true),
(gen_random_uuid(), 'Itaú Unibanco Holding S.A.', 'ITUB4', 'ITUB4.SA', 'ACAO', 'FINANCEIRO', true),
(gen_random_uuid(), 'Banco Bradesco S.A.', 'BBDC4', 'BBDC4.SA', 'ACAO', 'FINANCEIRO', true),
(gen_random_uuid(), 'Ambev S.A.', 'ABEV3', 'ABEV3.SA', 'ACAO', 'BENS_CONSUMO', true),
(gen_random_uuid(), 'Magazine Luiza S.A.', 'MGLU3', 'MGLU3.SA', 'ACAO', 'VAREJO', true),
(gen_random_uuid(), 'WEG S.A.', 'WEGE3', 'WEGE3.SA', 'ACAO', 'INDUSTRIAL', true),
(gen_random_uuid(), 'JBS S.A.', 'JBSS3', 'JBSS3.SA', 'ACAO', 'BENS_CONSUMO', true),
(gen_random_uuid(), 'B3 S.A.', 'B3SA3', 'B3SA3.SA', 'ACAO', 'FINANCEIRO', true),
(gen_random_uuid(), 'Suzano S.A.', 'SUZB3', 'SUZB3.SA', 'ACAO', 'INDUSTRIAL', true);

-- Inserir ETFs populares
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(gen_random_uuid(), 'iShares Bovespa', 'BOVA11', 'BOVA11.SA', 'ETF', 'FUNDOS', true),
(gen_random_uuid(), 'iShares Small Cap', 'SMAL11', 'SMAL11.SA', 'ETF', 'FUNDOS', true),
(gen_random_uuid(), 'SPDR S&P 500 ETF', 'SPY', 'SPY', 'ETF', 'FUNDOS', true);

-- Inserir FIIs populares
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(gen_random_uuid(), 'Maxi Renda FII', 'MXRF11', 'MXRF11.SA', 'FII', 'IMOBILIARIO', true),
(gen_random_uuid(), 'XP Log FII', 'XPLG11', 'XPLG11.SA', 'FII', 'IMOBILIARIO', true),
(gen_random_uuid(), 'Kinea Renda Imobiliária FII', 'KNRI11', 'KNRI11.SA', 'FII', 'IMOBILIARIO', true),
(gen_random_uuid(), 'CSHG Real Estate FII', 'HGRE11', 'HGRE11.SA', 'FII', 'IMOBILIARIO', true),
(gen_random_uuid(), 'Vinci Partners RE FII', 'VINO11', 'VINO11.SA', 'FII', 'IMOBILIARIO', true);

-- Inserir alguns títulos de renda fixa
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(gen_random_uuid(), 'Tesouro Selic 2029', 'SELIC2029', 'SELIC2029', 'RENDA_FIXA', 'GOVERNO', true),
(gen_random_uuid(), 'Tesouro IPCA+ 2029', 'IPCA2029', 'IPCA2029', 'RENDA_FIXA', 'GOVERNO', true),
(gen_random_uuid(), 'Tesouro Prefixado 2027', 'PRE2027', 'PRE2027', 'RENDA_FIXA', 'GOVERNO', true),
(gen_random_uuid(), 'CDB Banco Inter', 'CDB_INTER', 'CDB_INTER', 'RENDA_FIXA', 'FINANCEIRO', true),
(gen_random_uuid(), 'LCI Banco do Brasil', 'LCI_BB', 'LCI_BB', 'RENDA_FIXA', 'FINANCEIRO', true);

-- Inserir algumas criptomoedas
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(gen_random_uuid(), 'Bitcoin', 'BTC', 'BTC-USD', 'CRIPTO', 'TECNOLOGIA', true),
(gen_random_uuid(), 'Ethereum', 'ETH', 'ETH-USD', 'CRIPTO', 'TECNOLOGIA', true),
(gen_random_uuid(), 'Cardano', 'ADA', 'ADA-USD', 'CRIPTO', 'TECNOLOGIA', true);

-- Atualizar preços iniciais (valores aproximados para demonstração)
UPDATE ativos SET preco_atual = 65.50 WHERE ticker = 'VALE3';
UPDATE ativos SET preco_atual = 35.20 WHERE ticker = 'PETR4';
UPDATE ativos SET preco_atual = 23.45 WHERE ticker = 'ITUB4';
UPDATE ativos SET preco_atual = 12.80 WHERE ticker = 'BBDC4';
UPDATE ativos SET preco_atual = 11.90 WHERE ticker = 'ABEV3';
UPDATE ativos SET preco_atual = 8.50 WHERE ticker = 'MGLU3';
UPDATE ativos SET preco_atual = 42.30 WHERE ticker = 'WEGE3';
UPDATE ativos SET preco_atual = 28.75 WHERE ticker = 'JBSS3';
UPDATE ativos SET preco_atual = 11.85 WHERE ticker = 'B3SA3';
UPDATE ativos SET preco_atual = 54.20 WHERE ticker = 'SUZB3';

-- ETFs
UPDATE ativos SET preco_atual = 118.50 WHERE ticker = 'BOVA11';
UPDATE ativos SET preco_atual = 45.30 WHERE ticker = 'SMAL11';
UPDATE ativos SET preco_atual = 520.75 WHERE ticker = 'SPY';

-- FIIs
UPDATE ativos SET preco_atual = 10.85 WHERE ticker = 'MXRF11';
UPDATE ativos SET preco_atual = 98.20 WHERE ticker = 'XPLG11';
UPDATE ativos SET preco_atual = 158.90 WHERE ticker = 'KNRI11';
UPDATE ativos SET preco_atual = 125.40 WHERE ticker = 'HGRE11';
UPDATE ativos SET preco_atual = 89.75 WHERE ticker = 'VINO11';

-- Renda Fixa (valores em % a.a.)
UPDATE ativos SET preco_atual = 13.75 WHERE ticker = 'SELIC2029';
UPDATE ativos SET preco_atual = 6.25 WHERE ticker = 'IPCA2029';
UPDATE ativos SET preco_atual = 11.50 WHERE ticker = 'PRE2027';
UPDATE ativos SET preco_atual = 14.80 WHERE ticker = 'CDB_INTER';
UPDATE ativos SET preco_atual = 13.20 WHERE ticker = 'LCI_BB';

-- Criptomoedas (em USD)
UPDATE ativos SET preco_atual = 43500.00 WHERE ticker = 'BTC';
UPDATE ativos SET preco_atual = 2650.00 WHERE ticker = 'ETH';
UPDATE ativos SET preco_atual = 0.48 WHERE ticker = 'ADA';