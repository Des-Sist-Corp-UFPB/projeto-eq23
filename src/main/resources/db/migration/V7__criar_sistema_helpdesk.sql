-- V7: Renomear produto para ativo e criar tabela de chamados para helpdesk
ALTER TABLE produto RENAME TO ativo;

ALTER TABLE ativo ADD COLUMN numero_serie VARCHAR(50);
ALTER TABLE ativo ADD COLUMN status VARCHAR(20) DEFAULT 'ATIVO';

CREATE TABLE chamado (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    prioridade VARCHAR(20) NOT NULL, -- BAIXA, MEDIA, ALTA, CRITICA
    status VARCHAR(20) NOT NULL, -- ABERTO, EM_ATENDIMENTO, RESOLVIDO, FECHADO
    ativo_id BIGINT,
    tecnico_id BIGINT,
    cliente_id BIGINT NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chamado_ativo FOREIGN KEY (ativo_id) REFERENCES ativo(id) ON DELETE SET NULL,
    CONSTRAINT fk_chamado_tecnico FOREIGN KEY (tecnico_id) REFERENCES usuario(id) ON DELETE SET NULL,
    CONSTRAINT fk_chamado_cliente FOREIGN KEY (cliente_id) REFERENCES usuario(id) ON DELETE CASCADE
);
