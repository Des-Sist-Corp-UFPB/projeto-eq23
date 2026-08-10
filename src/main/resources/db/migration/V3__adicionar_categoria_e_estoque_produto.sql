-- Migração V3: Adiciona controle de estoque e relação com categoria em produto
--
-- Regras:
-- - quantidade: inteiro >= 0, padrão 0 (produto sem estoque inicial)
-- - categoria_id: chave estrangeira opcional (nullable) — produto pode não ter categoria

ALTER TABLE produto
    ADD COLUMN quantidade   INTEGER                  NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    ADD COLUMN categoria_id BIGINT                   REFERENCES categoria(id) ON DELETE SET NULL;

-- Índice para facilitar filtragem de produtos por categoria
CREATE INDEX idx_produto_categoria ON produto (categoria_id);

COMMENT ON COLUMN produto.quantidade IS 'Quantidade em estoque (deve ser >= 0)';
COMMENT ON COLUMN produto.categoria_id IS 'FK para categoria — nullable (produto sem categoria)';
