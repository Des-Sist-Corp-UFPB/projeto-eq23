package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.domain.Chamado;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ChamadoForm;
import br.ufpb.dsc.mercado.exception.ChamadoNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.service.AtivoService;
import br.ufpb.dsc.mercado.service.ChamadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChamadoControllerMockTest {

    private MockMvc mockMvc;
    private ChamadoService chamadoService;
    private AtivoService ativoService;
    private UsuarioRepository usuarioRepository;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        chamadoService = mock(ChamadoService.class);
        ativoService = mock(AtivoService.class);
        usuarioRepository = mock(UsuarioRepository.class);

        mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setNome("Administrador");
        mockUsuario.setUsername("admin");
        mockUsuario.setSenha("admin123");

        // mock default lists
        Page<Ativo> emptyAtivos = new PageImpl<>(Collections.emptyList());
        when(ativoService.listar(any(Pageable.class))).thenReturn(emptyAtivos);
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        ChamadoController chamadoController = new ChamadoController(chamadoService, ativoService, usuarioRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(chamadoController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(Usuario.class) && 
                               parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
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
    @DisplayName("GET /chamados — deve retornar lista de chamados")
    void listar_DeveRetornarListaView() throws Exception {
        Page<Chamado> page = new PageImpl<>(Collections.emptyList());
        when(chamadoService.listarPorCliente(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/chamados"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/lista"))
                .andExpect(model().attributeExists("chamados"));
    }

    @Test
    @DisplayName("GET /chamados/abrir — deve retornar template de abertura")
    void abrirForm_DeveRetornarAbrirView() throws Exception {
        mockMvc.perform(get("/chamados/abrir"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/abrir"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    @DisplayName("POST /chamados/abrir com dados válidos — deve redirecionar para lista")
    void abrirCriar_DadosValidos_DeveRedirecionar() throws Exception {
        Chamado mockChamado = new Chamado();
        mockChamado.setId(1L);
        when(chamadoService.criar(any(ChamadoForm.class), any(Usuario.class))).thenReturn(mockChamado);

        mockMvc.perform(post("/chamados/abrir")
                        .param("titulo", "Impressora Quebrada")
                        .param("descricao", "Não liga")
                        .param("prioridade", "MEDIA")
                        .param("status", "ABERTO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/chamados"));
    }

    @Test
    @DisplayName("GET /chamados/novo — deve retornar modal do form")
    void novoForm_DeveRetornarModal() throws Exception {
        mockMvc.perform(get("/chamados/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/fragments/form :: modal"));
    }

    @Test
    @DisplayName("GET /chamados/{id}/editar — deve retornar modal do form de edição")
    void editarForm_DeveRetornarModal() throws Exception {
        Chamado mockChamado = new Chamado();
        mockChamado.setId(1L);
        mockChamado.setTitulo("Erro grave");
        mockChamado.setDescricao("Erro de rede");
        mockChamado.setPrioridade("MEDIA");
        mockChamado.setStatus("ABERTO");

        when(chamadoService.buscarPorId(1L)).thenReturn(mockChamado);

        mockMvc.perform(get("/chamados/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/fragments/form :: modal"));
    }

    @Test
    @DisplayName("POST /chamados com dados válidos — deve criar chamado e retornar linha")
    void criar_DadosValidos_DeveRetornarLinha() throws Exception {
        Chamado mockChamado = new Chamado();
        mockChamado.setId(1L);
        mockChamado.setTitulo("Erro grave");
        when(chamadoService.criar(any(ChamadoForm.class), any(Usuario.class))).thenReturn(mockChamado);

        mockMvc.perform(post("/chamados")
                        .param("titulo", "Erro grave")
                        .param("descricao", "Erro de rede")
                        .param("prioridade", "MEDIA")
                        .param("status", "ABERTO"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/fragments/linha :: linha"));
    }

    @Test
    @DisplayName("PUT /chamados/{id} com dados válidos — deve atualizar e retornar linha")
    void atualizar_DadosValidos_DeveRetornarLinha() throws Exception {
        Chamado mockChamado = new Chamado();
        mockChamado.setId(1L);
        mockChamado.setTitulo("Erro corrigido");
        when(chamadoService.atualizar(eq(1L), any(ChamadoForm.class))).thenReturn(mockChamado);

        mockMvc.perform(put("/chamados/1")
                        .param("titulo", "Erro corrigido")
                        .param("descricao", "Erro corrigido")
                        .param("prioridade", "MEDIA")
                        .param("status", "ABERTO"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/fragments/linha :: linha"));
    }

    @Test
    @DisplayName("PUT /chamados/{id}/status — deve alterar status e retornar linha")
    void alterarStatus_DeveRetornarLinha() throws Exception {
        Chamado mockChamado = new Chamado();
        mockChamado.setId(1L);
        mockChamado.setStatus("CONCLUIDO");
        when(chamadoService.alterarStatus(eq(1L), eq("CONCLUIDO"))).thenReturn(mockChamado);

        mockMvc.perform(put("/chamados/1/status")
                        .param("status", "CONCLUIDO"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/fragments/linha :: linha"));
    }

    @Test
    @DisplayName("PUT /chamados/{id}/atribuir — deve atribuir tecnico e retornar linha")
    void atribuirTecnico_DeveRetornarLinha() throws Exception {
        Chamado mockChamado = new Chamado();
        mockChamado.setId(1L);
        when(chamadoService.atribuirTecnico(eq(1L), eq(2L))).thenReturn(mockChamado);

        mockMvc.perform(put("/chamados/1/atribuir")
                        .param("tecnicoId", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamados/fragments/linha :: linha"));
    }

    @Test
    @DisplayName("DELETE /chamados/{id} — deve excluir e retornar 200")
    void excluir_DeveRetornar200() throws Exception {
        doNothing().when(chamadoService).excluir(1L);

        mockMvc.perform(delete("/chamados/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /chamados/{id} inexistente — deve retornar 404")
    void excluir_Inexistente_DeveRetornar404() throws Exception {
        doThrow(new ChamadoNaoEncontradoException(1L)).when(chamadoService).excluir(1L);

        mockMvc.perform(delete("/chamados/1"))
                .andExpect(status().isNotFound());
    }
}
