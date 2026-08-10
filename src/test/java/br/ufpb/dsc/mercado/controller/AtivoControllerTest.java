package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.repository.AtivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DisplayName("AtivoController — Testes de Integração")
class AtivoControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AtivoRepository ativoRepository;

    private Ativo ativoCadastrado;

    @BeforeEach
    void setUp() {
        ativoRepository.deleteAll();
        ativoCadastrado = ativoRepository.save(
                new Ativo("Arroz Integral", "Arroz integral tipo 1", "SN123", "ATIVO")
        );
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("GET /ativos: deve retornar página de listagem com status 200")
    void listar_usuarioAutenticado_deveRetornarPaginaLista() throws Exception {
        mockMvc.perform(get("/ativos"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/lista"))
                .andExpect(model().attributeExists("ativos"))
                .andExpect(content().string(containsString("Arroz Integral")));
    }

    @Test
    @DisplayName("GET /ativos: usuário não autenticado deve ser redirecionado para /login")
    void listar_semAutenticacao_deveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/ativos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("GET /ativos/novo: deve retornar fragmento do formulário vazio")
    void novoForm_deveRetornarFragmentoFormulario() throws Exception {
        mockMvc.perform(get("/ativos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/form :: modal"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("ativo", nullValue()));
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("POST /ativos: deve criar ativo e retornar fragmento da linha")
    void criar_dadosValidos_deveCriarERetornarLinha() throws Exception {
        mockMvc.perform(post("/ativos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", "Feijão Preto")
                        .param("descricao", "Feijão preto premium")
                        .param("numeroSerie", "SN999")
                        .param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/linha :: linha"))
                .andExpect(model().attributeExists("ativo"))
                .andExpect(content().string(containsString("Feijão Preto")));
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("POST /ativos: dados inválidos devem retornar formulário com erros")
    void criar_dadosInvalidos_deveRetornarFormularioComErros() throws Exception {
        mockMvc.perform(post("/ativos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/form :: modal"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("GET /ativos/{id}/editar: deve retornar formulário preenchido")
    void editarForm_ativoExistente_deveRetornarFormularioPreenchido() throws Exception {
        mockMvc.perform(get("/ativos/{id}/editar", ativoCadastrado.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/form :: modal"))
                .andExpect(model().attributeExists("form", "ativo"))
                .andExpect(content().string(containsString("Arroz Integral")));
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("DELETE /ativos/{id}: deve excluir ativo e retornar 200")
    void excluir_ativoExistente_deveRetornar200() throws Exception {
        mockMvc.perform(delete("/ativos/{id}", ativoCadastrado.getId())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("DELETE /ativos/{id}: ativo inexistente deve retornar 404")
    void excluir_ativoInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(delete("/ativos/{id}", 9999L)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}


