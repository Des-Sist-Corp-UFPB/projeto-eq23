package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import br.ufpb.dsc.mercado.service.LogAuditoriaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuditoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditoriaControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogAuditoriaService logAuditoriaService;

    @Test
    @DisplayName("GET /auditoria sem HTMX — deve retornar index de auditoria")
    void listar_SemHtmx_DeveRetornarIndex() throws Exception {
        Page<LogAuditoria> page = new PageImpl<>(Collections.emptyList());
        when(logAuditoriaService.listar(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/auditoria"))
                .andExpect(status().isOk())
                .andExpect(view().name("auditoria/index"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(model().attribute("paginaAtual", 0));
    }

    @Test
    @DisplayName("GET /auditoria com HTMX — deve retornar fragmento de tabela")
    void listar_ComHtmx_DeveRetornarFragmento() throws Exception {
        Page<LogAuditoria> page = new PageImpl<>(Collections.emptyList());
        when(logAuditoriaService.listar(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/auditoria")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auditoria/fragments/tabela :: tabela"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(model().attribute("paginaAtual", 0));
    }

    @Test
    @DisplayName("GET /auditoria/fragmento-tabela — deve retornar fragmento de tabela")
    void fragmentoTabela_DeveRetornarFragmento() throws Exception {
        Page<LogAuditoria> page = new PageImpl<>(Collections.emptyList());
        when(logAuditoriaService.listar(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/auditoria/fragmento-tabela")
                        .param("pagina", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("auditoria/fragments/tabela :: tabela"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(model().attribute("paginaAtual", 2));
    }
}
