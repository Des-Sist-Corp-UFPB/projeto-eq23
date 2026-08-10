-- V13: Conta de sistema usada para atribuir/auditar ações disparadas via MCP (assistentes de IA)
INSERT INTO usuario (nome, username, senha, role)
VALUES ('Assistente de IA (MCP)', 'mcp-agent', '$2a$10$' || md5(random()::text), 'ADMIN');
