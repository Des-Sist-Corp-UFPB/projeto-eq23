-- V15: Papel ADMIN_SISTEMA (topo da hierarquia: ADMIN_SISTEMA > ADMIN > TECNICO > CLIENTE)
-- e coluna para distinguir senha "de verdade" (definida por alguém) de senha placeholder
-- gerada automaticamente no provisionamento via Google (ver CustomOidcUserService).

ALTER TABLE usuario ADD COLUMN senha_definida BOOLEAN NOT NULL DEFAULT FALSE;

-- A conta seed 'admin' é a raiz da hierarquia: ninguém a criou, então vira ADMIN_SISTEMA.
-- Ela já tem senha de verdade (admin123), então senha_definida = true.
UPDATE usuario SET role = 'ADMIN_SISTEMA', senha_definida = TRUE
WHERE username IN ('admin', 'admin@dcx.ufpb.br');
