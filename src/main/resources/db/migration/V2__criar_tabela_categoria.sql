-- Migração V2: Criação da tabela de categorias
-- As categorias permitem organizar produtos por tipo (ex.: Hortifruti, Laticínios, Bebidas)

CREATE TABLE categoria (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(80)              NOT NULL,
    descricao   TEXT,
    criado_em   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Nome de categoria deve ser único para evitar duplicatas
CREATE UNIQUE INDEX idx_categoria_nome ON categoria (LOWER(nome));

COMMENT ON TABLE categoria IS 'Categorias de produtos do mercado';
COMMENT ON COLUMN categoria.nome IS 'Nome único da categoria (case-insensitive)';
COMMENT ON COLUMN categoria.criado_em IS 'Timestamp de criação do registro (UTC)';
