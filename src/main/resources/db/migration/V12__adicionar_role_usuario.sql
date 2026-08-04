-- V12: Adicionar papel (role) ao usuário, substituindo a checagem hardcoded por username
ALTER TABLE usuario ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CLIENTE'; -- ADMIN, TECNICO, CLIENTE

UPDATE usuario SET role = 'ADMIN' WHERE username IN ('admin', 'admin@dcx.ufpb.br');
