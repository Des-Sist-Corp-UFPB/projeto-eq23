-- V11: Criar tabela de patrimônios vinculados a ativos e linkar a chamados
CREATE TABLE patrimonio (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    numero_serie VARCHAR(50) NOT NULL UNIQUE,
    ativo_id BIGINT NOT NULL,
    CONSTRAINT fk_patrimonio_ativo FOREIGN KEY (ativo_id) REFERENCES ativo(id) ON DELETE CASCADE
);

ALTER TABLE chamado ADD COLUMN patrimonio_id BIGINT;
ALTER TABLE chamado ADD CONSTRAINT fk_chamado_patrimonio FOREIGN KEY (patrimonio_id) REFERENCES patrimonio(id) ON DELETE SET NULL;
