package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — Testes Unitários")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario adminSistema;
    private Usuario admin;
    private Usuario tecnico;

    @BeforeEach
    void setUp() {
        adminSistema = new Usuario("Raiz", "raiz@dcx.ufpb.br", "hash");
        adminSistema.setId(1L);
        adminSistema.setRole("ADMIN_SISTEMA");

        admin = new Usuario("Admin", "admin2@dcx.ufpb.br", "hash");
        admin.setId(2L);
        admin.setRole("ADMIN");

        tecnico = new Usuario("Tecnico", "tecnico@dcx.ufpb.br", "hash");
        tecnico.setId(3L);
        tecnico.setRole("TECNICO");
    }

    @Test
    @DisplayName("papeisPermitidos: ADMIN_SISTEMA pode atribuir ADMIN, TECNICO e CLIENTE")
    void papeisPermitidos_adminSistema() {
        assertThat(usuarioService.papeisPermitidos(adminSistema))
                .containsExactlyInAnyOrder("ADMIN", "TECNICO", "CLIENTE");
    }

    @Test
    @DisplayName("papeisPermitidos: ADMIN só pode atribuir TECNICO e CLIENTE")
    void papeisPermitidos_admin() {
        assertThat(usuarioService.papeisPermitidos(admin))
                .containsExactlyInAnyOrder("TECNICO", "CLIENTE");
    }

    @Test
    @DisplayName("criar: ADMIN não pode criar outro ADMIN (cascata)")
    void criar_adminNaoPodeCriarAdmin() {
        assertThatThrownBy(() -> usuarioService.criar("Novo", "novo@dcx.ufpb.br", "ADMIN", "senha123", admin))
                .isInstanceOf(IllegalArgumentException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: ADMIN_SISTEMA pode criar ADMIN")
    void criar_adminSistemaPodeCriarAdmin() {
        when(usuarioRepository.findByUsername("novo@dcx.ufpb.br")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hash-codificado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario criado = usuarioService.criar("Novo Admin", "novo@dcx.ufpb.br", "ADMIN", "senha123", adminSistema);

        assertThat(criado.getRole()).isEqualTo("ADMIN");
        assertThat(criado.getSenha()).isEqualTo("hash-codificado");
        assertThat(criado.isSenhaDefinida()).isTrue();
        verify(logAuditoriaService).registrar(eq("CRIAR"), eq("Usuario"), any(), anyString());
    }

    @Test
    @DisplayName("criar: rejeita username já existente")
    void criar_usernameDuplicado() {
        when(usuarioRepository.findByUsername("tecnico@dcx.ufpb.br")).thenReturn(Optional.of(tecnico));

        assertThatThrownBy(() -> usuarioService.criar("Outro", "tecnico@dcx.ufpb.br", "TECNICO", "senha123", admin))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("trocarSenha: exige senha atual correta quando senhaDefinida = true")
    void trocarSenha_exigeSenhaAtualCorreta() {
        tecnico.setSenhaDefinida(true);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(tecnico));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.trocarSenha(3L, "errada", "novaSenha123"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("trocarSenha: dispensa senha atual quando senhaDefinida = false (conta provisionada via Google)")
    void trocarSenha_dispensaSenhaAtualSeNaoDefinida() {
        tecnico.setSenhaDefinida(false);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(tecnico));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-nova");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.trocarSenha(3L, null, "novaSenha123");

        assertThat(tecnico.getSenha()).isEqualTo("hash-nova");
        assertThat(tecnico.isSenhaDefinida()).isTrue();
    }
}
