-- Dados iniciais para a aplicação InvestIA

-- Inserir ativos populares da B3
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(RANDOM_UUID(), 'Vale S.A.', 'VALE3', 'VALE3.SA', 'ACAO', 'MATERIAIS_BASICOS', true),
(RANDOM_UUID(), 'Petróleo Brasileiro S.A.', 'PETR4', 'PETR4.SA', 'ACAO', 'ENERGIA', true),
(RANDOM_UUID(), 'Itaú Unibanco Holding S.A.', 'ITUB4', 'ITUB4.SA', 'ACAO', 'FINANCEIRO', true),
(RANDOM_UUID(), 'Banco Bradesco S.A.', 'BBDC4', 'BBDC4.SA', 'ACAO', 'FINANCEIRO', true),
(RANDOM_UUID(), 'Ambev S.A.', 'ABEV3', 'ABEV3.SA', 'ACAO', 'CONSUMO', true),
(RANDOM_UUID(), 'Magazine Luiza S.A.', 'MGLU3', 'MGLU3.SA', 'ACAO', 'VAREJO', true),
(RANDOM_UUID(), 'WEG S.A.', 'WEGE3', 'WEGE3.SA', 'ACAO', 'INDUSTRIAL', true),
(RANDOM_UUID(), 'JBS S.A.', 'JBSS3', 'JBSS3.SA', 'ACAO', 'CONSUMO', true),
(RANDOM_UUID(), 'B3 S.A.', 'B3SA3', 'B3SA3.SA', 'ACAO', 'FINANCEIRO', true),
(RANDOM_UUID(), 'Suzano S.A.', 'SUZB3', 'SUZB3.SA', 'ACAO', 'INDUSTRIAL', true);

-- Inserir ETFs populares
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(RANDOM_UUID(), 'iShares Bovespa', 'BOVA11', 'BOVA11.SA', 'ETF', 'FINANCEIRO', true),
(RANDOM_UUID(), 'iShares Small Cap', 'SMAL11', 'SMAL11.SA', 'ETF', 'FINANCEIRO', true),
(RANDOM_UUID(), 'SPDR S&P 500 ETF', 'SPY', 'SPY', 'ETF', 'FINANCEIRO', true);

-- Inserir FIIs populares
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(RANDOM_UUID(), 'Maxi Renda FII', 'MXRF11', 'MXRF11.SA', 'FII', 'IMOBILIARIO', true),
(RANDOM_UUID(), 'XP Log FII', 'XPLG11', 'XPLG11.SA', 'FII', 'IMOBILIARIO', true),
(RANDOM_UUID(), 'Kinea Renda Imobiliária FII', 'KNRI11', 'KNRI11.SA', 'FII', 'IMOBILIARIO', true),
(RANDOM_UUID(), 'CSHG Real Estate FII', 'HGRE11', 'HGRE11.SA', 'FII', 'IMOBILIARIO', true),
(RANDOM_UUID(), 'Vinci Partners RE FII', 'VINO11', 'VINO11.SA', 'FII', 'IMOBILIARIO', true);

-- Inserir alguns títulos de renda fixa
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(RANDOM_UUID(), 'Tesouro Selic 2029', 'SELIC2029', 'SELIC2029', 'TESOURO_DIRETO', 'FINANCEIRO', true),
(RANDOM_UUID(), 'Tesouro IPCA+ 2029', 'IPCA2029', 'IPCA2029', 'TESOURO_DIRETO', 'FINANCEIRO', true),
(RANDOM_UUID(), 'Tesouro Prefixado 2027', 'PRE2027', 'PRE2027', 'TESOURO_DIRETO', 'FINANCEIRO', true),
(RANDOM_UUID(), 'CDB Banco Inter', 'CDB_INTER', 'CDB_INTER', 'CDB', 'FINANCEIRO', true),
(RANDOM_UUID(), 'LCI Banco do Brasil', 'LCI_BB', 'LCI_BB', 'LCI_LCA', 'FINANCEIRO', true);

-- Inserir algumas criptomoedas
INSERT INTO ativos (id, nome, ticker, simbolo, tipo_ativo, setor, status) VALUES
(RANDOM_UUID(), 'Bitcoin', 'BTC', 'BTC-USD', 'CRIPTO', 'TECNOLOGIA', true),
(RANDOM_UUID(), 'Ethereum', 'ETH', 'ETH-USD', 'CRIPTO', 'TECNOLOGIA', true),
(RANDOM_UUID(), 'Cardano', 'ADA', 'ADA-USD', 'CRIPTO', 'TECNOLOGIA', true);

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