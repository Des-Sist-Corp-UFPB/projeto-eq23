-- V6: Força a atualização do hash BCrypt da senha do admin para a versão correta
-- Senha correspondente: admin123
UPDATE usuario
SET senha = '$2a$10$2sQmUdo3nOqkVc/guq5wA.hsLf8r99ZMcMRfbejJ0msz04QadYAbe'
WHERE username = 'admin';
