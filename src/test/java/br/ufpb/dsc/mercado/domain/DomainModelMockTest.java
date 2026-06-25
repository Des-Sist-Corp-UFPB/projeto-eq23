package br.ufpb.dsc.mercado.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelMockTest {

    @Test
    @DisplayName("Ativo — Testar getters, setters e construtores")
    void testAtivo() {
        Ativo a1 = new Ativo();
        a1.setId(1L);
        a1.setNome("Computador");
        a1.setDescricao("Laptop");
        a1.setNumeroSerie("123");
        a1.setStatus("ATIVO");
        a1.setCriadoEm(Instant.EPOCH);
        a1.setAtualizadoEm(Instant.EPOCH);

        assertEquals(1L, a1.getId());
        assertEquals("Computador", a1.getNome());
        assertEquals("Laptop", a1.getDescricao());
        assertEquals("123", a1.getNumeroSerie());
        assertEquals("ATIVO", a1.getStatus());
        assertEquals(Instant.EPOCH, a1.getCriadoEm());
        assertEquals(Instant.EPOCH, a1.getAtualizadoEm());

        Ativo a2 = new Ativo("Computador", "Laptop", "123", "ATIVO");
        assertNotNull(a2);
        assertTrue(a1.equals(a1));
        assertFalse(a1.equals(null));
        assertFalse(a1.equals(new Object()));
    }

    @Test
    @DisplayName("LogAuditoria — Testar getters, setters e construtores")
    void testLogAuditoria() {
        Instant now = Instant.now();
        LogAuditoria log1 = new LogAuditoria();
        log1.setId(1L);
        log1.setUsuario("admin");
        log1.setAcao("CRIAR");
        log1.setEntidade("Ativo");
        log1.setEntidadeId(2L);
        log1.setDetalhes("Criou o ativo");
        log1.setDataHora(now);

        assertEquals(1L, log1.getId());
        assertEquals("admin", log1.getUsuario());
        assertEquals("CRIAR", log1.getAcao());
        assertEquals("Ativo", log1.getEntidade());
        assertEquals(2L, log1.getEntidadeId());
        assertEquals("Criou o ativo", log1.getDetalhes());
        assertEquals(now, log1.getDataHora());

        LogAuditoria log2 = new LogAuditoria("admin", "CRIAR", "Ativo", 2L, "Criou o ativo", now);
        assertNotNull(log2);
        assertTrue(log1.equals(log1));
        assertFalse(log1.equals(null));
    }

    @Test
    @DisplayName("Usuario — Testar getters, setters, UserDetails e construtores")
    void testUsuario() {
        Usuario u1 = new Usuario();
        u1.setId(1L);
        u1.setNome("User");
        u1.setUsername("usr");
        u1.setSenha("123");

        assertEquals(1L, u1.getId());
        assertEquals("User", u1.getNome());
        assertEquals("usr", u1.getUsername());
        assertEquals("123", u1.getSenha());
        assertNull(u1.getCreatedAt());

        Usuario u2 = new Usuario("User", "usr", "123");
        assertNotNull(u2);
        assertTrue(u1.equals(u1));

        // UserDetails properties
        assertTrue(u1.isAccountNonExpired());
        assertTrue(u1.isAccountNonLocked());
        assertTrue(u1.isCredentialsNonExpired());
        assertTrue(u1.isEnabled());
        assertNotNull(u1.getAuthorities());
    }

    @Test
    @DisplayName("Chamado — Testar getters, setters e construtores")
    void testChamado() {
        Chamado c1 = new Chamado();
        c1.setId(1L);
        c1.setTitulo("Erro");
        c1.setDescricao("Desc");
        c1.setPrioridade("ALTA");
        c1.setStatus("ABERTO");
        c1.setCriadoEm(Instant.EPOCH);
        c1.setAtualizadoEm(Instant.EPOCH);

        Ativo a = new Ativo();
        c1.setAtivo(a);
        Usuario u = new Usuario();
        c1.setCliente(u);
        c1.setTecnico(u);

        assertEquals(1L, c1.getId());
        assertEquals("Erro", c1.getTitulo());
        assertEquals("Desc", c1.getDescricao());
        assertEquals("ALTA", c1.getPrioridade());
        assertEquals("ABERTO", c1.getStatus());
        assertEquals(Instant.EPOCH, c1.getCriadoEm());
        assertEquals(Instant.EPOCH, c1.getAtualizadoEm());
        assertEquals(a, c1.getAtivo());
        assertEquals(u, c1.getCliente());
        assertEquals(u, c1.getTecnico());

        assertTrue(c1.equals(c1));
        assertFalse(c1.equals(null));
    }
}
