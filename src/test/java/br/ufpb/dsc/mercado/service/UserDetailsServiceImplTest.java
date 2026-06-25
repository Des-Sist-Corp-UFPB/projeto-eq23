package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    private UsuarioRepository usuarioRepository;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        userDetailsService = new UserDetailsServiceImpl(usuarioRepository);
    }

    @Test
    @DisplayName("loadUserByUsername — Usuário existente deve retornar o objeto correspondente")
    void loadUserByUsername_UsuarioExiste_DeveRetornarUsuario() {
        Usuario mockUsuario = new Usuario();
        mockUsuario.setUsername("user_test");
        mockUsuario.setSenha("encoded_password");

        when(usuarioRepository.findByUsername("user_test")).thenReturn(Optional.of(mockUsuario));

        UserDetails result = userDetailsService.loadUserByUsername("user_test");
        assertNotNull(result);
        assertEquals("user_test", result.getUsername());
        assertEquals("encoded_password", result.getPassword());
    }

    @Test
    @DisplayName("loadUserByUsername — Usuário inexistente deve lançar UsernameNotFoundException")
    void loadUserByUsername_UsuarioNaoExiste_DeveLancarException() {
        when(usuarioRepository.findByUsername("user_not_found")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("user_not_found");
        });
    }
}
