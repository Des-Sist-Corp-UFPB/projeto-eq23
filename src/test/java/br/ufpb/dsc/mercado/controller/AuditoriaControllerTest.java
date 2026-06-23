package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import br.ufpb.dsc.mercado.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DisplayName("AuditoriaController — Testes de Integração")
class AuditoriaControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @BeforeEach
    void setUp() {
        logAuditoriaRepository.deleteAll();
        logAuditoriaRepository.save(new LogAuditoria(
                "usuario-teste",
                "CRIAR",
                "Ativo",
                1L,
                "Criado ativo: Computador de Teste",
                Instant.now()
        ));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("GET /auditoria: deve retornar listagem de auditoria completa")
    void listar_usuarioAutenticado_deveRetornarPaginaAuditoria() throws Exception {
        mockMvc.perform(get("/auditoria"))
                .andExpect(status().isOk())
                .andExpect(view().name("auditoria/index"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(content().string(containsString("usuario-teste")))
                .andExpect(content().string(containsString("CRIAR")))
                .andExpect(content().string(containsString("Ativo")))
                .andExpect(content().string(containsString("Computador de Teste")));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("GET /auditoria com header HTMX: deve retornar apenas fragmento da tabela")
    void listar_comHtmxHeader_deveRetornarFragmentoTabela() throws Exception {
        mockMvc.perform(get("/auditoria")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auditoria/fragments/tabela :: tabela"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(content().string(containsString("usuario-teste")))
                .andExpect(content().string(containsString("Computador de Teste")));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("GET /auditoria/fragmento-tabela: deve retornar apenas fragmento da tabela")
    void fragmentoTabela_deveRetornarFragmentoTabela() throws Exception {
        mockMvc.perform(get("/auditoria/fragmento-tabela"))
                .andExpect(status().isOk())
                .andExpect(view().name("auditoria/fragments/tabela :: tabela"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(content().string(containsString("usuario-teste")))
                .andExpect(content().string(containsString("Computador de Teste")));
    }

    @Test
    @DisplayName("GET /auditoria: sem autenticação deve redirecionar para /login")
    void listar_semAutenticacao_deveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/auditoria"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
