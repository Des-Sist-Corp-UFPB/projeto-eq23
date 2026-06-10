-- Migração V8: Remove preco, quantidade e categoria_id da tabela ativo
ALTER TABLE ativo DROP COLUMN preco;
ALTER TABLE ativo DROP COLUMN quantidade;
ALTER TABLE ativo DROP COLUMN categoria_id;
