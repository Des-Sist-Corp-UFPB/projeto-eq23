package br.ufpb.dsc.mercado.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatrimonioTest {
    @Test
    @DisplayName("Patrimonio - Testar getters, setters e construtores")
    void testPatrimonio() {
        Ativo a = new Ativo();
        Patrimonio p1 = new Patrimonio();
        p1.setId(1L);
        p1.setCodigo("Y5942");
        p1.setNumeroSerie("5853Z210586");
        p1.setAtivo(a);

        assertEquals(1L, p1.getId());
        assertEquals("Y5942", p1.getCodigo());
        assertEquals("5853Z210586", p1.getNumeroSerie());
        assertEquals(a, p1.getAtivo());

        Patrimonio p2 = new Patrimonio("Y5942", "5853Z210586", a);
        assertNotNull(p2);
        assertEquals("Y5942", p2.getCodigo());
        assertEquals("5853Z210586", p2.getNumeroSerie());
        assertEquals(a, p2.getAtivo());
    }
}
