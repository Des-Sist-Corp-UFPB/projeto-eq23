CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    senha VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserindo um usuário administrador padrão (senha: admin123)
-- A senha 'admin123' em BCrypt fica assim:
INSERT INTO usuario (nome, username, senha)
VALUES ('Administrador', 'admin', '$2a$10$X8O.U1Vb8W/2SOfTf/J63.H7A32t0/fE0bB.fNlq2.nIqU1u/7wRe');
