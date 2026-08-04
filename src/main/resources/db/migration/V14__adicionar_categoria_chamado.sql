-- V14: Categoria do chamado (sugerida por IA ao abrir o chamado, ver ClassificacaoIaService)
ALTER TABLE chamado ADD COLUMN categoria VARCHAR(20); -- HARDWARE, SOFTWARE, REDE, ACESSO, OUTRO
