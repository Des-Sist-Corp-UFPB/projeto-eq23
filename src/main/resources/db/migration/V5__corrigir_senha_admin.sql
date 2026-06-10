-- V5: Corrige o hash BCrypt da senha do usuário admin
-- Senha: admin123
-- Hash gerado com BCrypt strength 10, verificado e válido:
UPDATE usuario
SET senha = '$2a$10$2sQmUdo3nOqkVc/guq5wA.hsLf8r99ZMcMRfbejJ0msz04QadYAbe'
WHERE username = 'admin';
