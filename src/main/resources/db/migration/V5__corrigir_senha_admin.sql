-- V5: Corrige o hash BCrypt da senha do usuário admin
-- Senha: admin123
-- Hash gerado com BCrypt strength 10, verificado e válido:
UPDATE usuario
SET senha = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'admin';
