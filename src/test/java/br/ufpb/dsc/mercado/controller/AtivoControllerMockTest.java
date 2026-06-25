package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.AtivoForm;
import br.ufpb.dsc.mercado.exception.AtivoNaoEncontradoException;
import br.ufpb.dsc.mercado.service.AtivoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AtivoControllerMockTest {

    private MockMvc mockMvc;
    private AtivoService ativoService;
    private AtivoController ativoController;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        ativoService = mock(AtivoService.class);
        ativoController = new AtivoController(ativoService);

        mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setNome("Administrador");
        mockUsuario.setUsername("admin");
        mockUsuario.setSenha("admin123");

        mockMvc = MockMvcBuilders.standaloneSetup(ativoController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return mockUsuario;
                    }
                })
                .build();
    }

    @Test
    @DisplayName("GET /ativos sem HTMX — deve retornar lista de ativos")
    void listar_SemHtmx_DeveRetornarListaView() throws Exception {
        Page<Ativo> page = new PageImpl<>(Collections.emptyList());
        when(ativoService.buscar(any(), any())).thenReturn(page);

        mockMvc.perform(get("/ativos"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/lista"))
                .andExpect(model().attributeExists("ativos"));
    }

    @Test
    @DisplayName("GET /ativos com HTMX — deve retornar fragmento de tabela")
    void listar_ComHtmx_DeveRetornarFragmento() throws Exception {
        Page<Ativo> page = new PageImpl<>(Collections.emptyList());
        when(ativoService.buscar(any(), any())).thenReturn(page);

        mockMvc.perform(get("/ativos")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/tabela :: tabela"));
    }

    @Test
    @DisplayName("GET /ativos/fragmento-tabela — deve retornar fragmento de tabela")
    void fragmentoTabela_DeveRetornarFragmento() throws Exception {
        Page<Ativo> page = new PageImpl<>(Collections.emptyList());
        when(ativoService.buscar(any(), any())).thenReturn(page);

        mockMvc.perform(get("/ativos/fragmento-tabela"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/tabela :: tabela"));
    }

    @Test
    @DisplayName("GET /ativos/novo — deve retornar modal do form")
    void novoForm_DeveRetornarModal() throws Exception {
        mockMvc.perform(get("/ativos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/form :: modal"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    @DisplayName("GET /ativos/{id}/editar — deve retornar modal do form de edição")
    void editarForm_DeveRetornarModal() throws Exception {
        Ativo mockAtivo = new Ativo();
        mockAtivo.setId(1L);
        mockAtivo.setNome("Laptop");
        mockAtivo.setStatus("ATIVO");
        when(ativoService.buscarPorId(1L)).thenReturn(mockAtivo);

        mockMvc.perform(get("/ativos/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/form :: modal"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("ativo", mockAtivo));
    }

    @Test
    @DisplayName("POST /ativos com dados válidos — deve criar ativo e retornar fragmento de linha")
    void criar_DadosValidos_DeveRetornarLinha() throws Exception {
        Ativo mockAtivo = new Ativo();
        mockAtivo.setId(1L);
        mockAtivo.setNome("Laptop");
        when(ativoService.criar(any(AtivoForm.class))).thenReturn(mockAtivo);

        mockMvc.perform(post("/ativos")
                        .param("nome", "Laptop")
                        .param("descricao", "Novo laptop")
                        .param("numeroSerie", "123456")
                        .param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/linha :: linha"))
                .andExpect(model().attribute("ativo", mockAtivo));
    }

    @Test
    @DisplayName("POST /ativos com dados inválidos — deve retornar modal com erros")
    void criar_DadosInvalidos_DeveRetornarModal() throws Exception {
        mockMvc.perform(post("/ativos")
                        .param("nome", "")
                        .param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/form :: modal"));
    }

    @Test
    @DisplayName("PUT /ativos/{id} com dados válidos — deve atualizar e retornar fragmento de linha")
    void atualizar_DadosValidos_DeveRetornarLinha() throws Exception {
        Ativo mockAtivo = new Ativo();
        mockAtivo.setId(1L);
        mockAtivo.setNome("Laptop Atualizado");
        when(ativoService.atualizar(eq(1L), any(AtivoForm.class))).thenReturn(mockAtivo);

        mockMvc.perform(put("/ativos/1")
                        .param("nome", "Laptop Atualizado")
                        .param("descricao", "Atualizado")
                        .param("numeroSerie", "123456")
                        .param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(view().name("ativos/fragments/linha :: linha"))
                .andExpect(model().attribute("ativo", mockAtivo));
    }

    @Test
    @DisplayName("DELETE /ativos/{id} — deve excluir e retornar 200")
    void excluir_DeveRetornar200() throws Exception {
        doNothing().when(ativoService).excluir(1L);

        mockMvc.perform(delete("/ativos/1"))
                .andExpect(status().isOk());

        verify(ativoService, times(1)).excluir(1L);
    }

    @Test
    @DisplayName("DELETE /ativos/{id} inexistente — deve retornar 404")
    void excluir_Inexistente_DeveRetornar404() throws Exception {
        doThrow(new AtivoNaoEncontradoException(1L)).when(ativoService).excluir(1L);

        mockMvc.perform(delete("/ativos/1"))
                .andExpect(status().isNotFound());
    }
}
