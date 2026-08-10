package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.domain.Chamado;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ChamadoForm;
import br.ufpb.dsc.mercado.exception.ChamadoNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.AtivoRepository;
import br.ufpb.dsc.mercado.repository.ChamadoRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChamadoService — Testes Unitários")
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @Mock
    private br.ufpb.dsc.mercado.repository.PatrimonioRepository patrimonioRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ChamadoService chamadoService;

    private Usuario cliente;
    private Usuario tecnico;
    private Ativo ativo;
    private Chamado chamadoExistente;
    private ChamadoForm formValido;

    @BeforeEach
    void setUp() {
        cliente = new Usuario("Maria Silva", "maria", "senha123");
        cliente.setId(1L);

        tecnico = new Usuario("João Técnico", "joao", "senha123");
        tecnico.setId(2L);

        ativo = new Ativo("Servidor Dell", "PowerEdge", "SN-DELL-555", "ATIVO");
        ativo.setId(1L);

        chamadoExistente = new Chamado("Instalação OS", "Instalar Linux", "MEDIA", "ABERTO", ativo, null, cliente);
        chamadoExistente.setId(10L);

        formValido = new ChamadoForm("Instalação OS", "Instalar Linux", "MEDIA", "ABERTO", 1L, null, 1L, null, null);
    }

    @Test
    @DisplayName("buscarPorId: deve retornar chamado quando ID existe")
    void buscarPorId_quandoIdExiste_deveRetornarChamado() {
        when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamadoExistente));

        Chamado resultado = chamadoService.buscarPorId(10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getTitulo()).isEqualTo("Instalação OS");
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando ID não existe")
    void buscarPorId_quandoIdNaoExiste_deveLancarExcecao() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.buscarPorId(99L))
                .isInstanceOf(ChamadoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("criar: deve salvar chamado, registrar auditoria e enviar e-mail")
    void criar_deveSalvarERegistrarAuditoria() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Chamado chamadoSalvo = new Chamado("Instalação OS", "Instalar Linux", "MEDIA", "ABERTO", ativo, null, cliente);
        chamadoSalvo.setId(10L);
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamadoSalvo);
        doNothing().when(emailService).enviarNotificacaoChamadoCriado(any(Chamado.class));

        Chamado resultado = chamadoService.criar(formValido, cliente);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);

        verify(chamadoRepository).save(any(Chamado.class));
        verify(logAuditoriaService).registrar(eq("CRIAR"), eq("Chamado"), eq(10L), anyString());
        verify(emailService).enviarNotificacaoChamadoCriado(any(Chamado.class));
    }

    @Test
    @DisplayName("atualizar: deve atualizar chamado e registrar auditoria")
    void atualizar_deveAtualizarERegistrarAuditoria() {
        when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamadoExistente));
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamadoExistente);

        ChamadoForm formEdicao = new ChamadoForm("Instalação OS - Urgente", "Instalar Linux Rápido", "ALTA", "ABERTO", 1L, null, 1L, null, null);

        Chamado resultado = chamadoService.atualizar(10L, formEdicao);

        assertThat(resultado.getTitulo()).isEqualTo("Instalação OS - Urgente");
        assertThat(resultado.getPrioridade()).isEqualTo("ALTA");

        verify(chamadoRepository).save(any(Chamado.class));
        verify(logAuditoriaService).registrar(eq("ATUALIZAR"), eq("Chamado"), eq(10L), anyString());
    }

    @Test
    @DisplayName("alterarStatus: deve mudar status e registrar auditoria")
    void alterarStatus_deveMudarStatusERegistrarAuditoria() {
        when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamadoExistente));
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamadoExistente);

        Chamado resultado = chamadoService.alterarStatus(10L, "FECHADO");

        assertThat(resultado.getStatus()).isEqualTo("FECHADO");
        verify(logAuditoriaService).registrar(eq("ALTERAR_STATUS"), eq("Chamado"), eq(10L), contains("ABERTO para FECHADO"));
    }

    @Test
    @DisplayName("atribuirTecnico: deve atribuir técnico e registrar auditoria")
    void atribuirTecnico_deveAtribuirTecnicoERegistrarAuditoria() {
        when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamadoExistente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(tecnico));
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamadoExistente);

        Chamado resultado = chamadoService.atribuirTecnico(10L, 2L);

        assertThat(resultado.getTecnico()).isEqualTo(tecnico);
        assertThat(resultado.getStatus()).isEqualTo("EM_ATENDIMENTO");
        verify(logAuditoriaService).registrar(eq("ATRIBUIR_TECNICO"), eq("Chamado"), eq(10L), contains("João Técnico"));
    }

    @Test
    @DisplayName("excluir: deve excluir chamado e registrar auditoria")
    void excluir_quandoExiste_deveExcluirERegistrarAuditoria() {
        when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamadoExistente));
        doNothing().when(chamadoRepository).deleteById(10L);

        assertThatCode(() -> chamadoService.excluir(10L))
                .doesNotThrowAnyException();

        verify(chamadoRepository).deleteById(10L);
        verify(logAuditoriaService).registrar(eq("EXCLUIR"), eq("Chamado"), eq(10L), anyString());
    }
}
