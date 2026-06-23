CREATE TABLE log_auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL,
    acao VARCHAR(50) NOT NULL,
    entidade VARCHAR(50) NOT NULL,
    entidade_id BIGINT,
    detalhes TEXT,
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL
);
