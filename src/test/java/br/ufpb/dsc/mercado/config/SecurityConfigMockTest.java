package br.ufpb.dsc.mercado.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SecurityConfigMockTest {

    @Test
    @DisplayName("SecurityConfig — Deve retornar passwordEncoder")
    void testPasswordEncoder() {
        SecurityConfig config = new SecurityConfig(null);
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    @DisplayName("SecurityConfig — Deve configurar filterChain")
    void testFilterChain() throws Exception {
        SecurityConfig config = new SecurityConfig(null);
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        org.springframework.security.web.DefaultSecurityFilterChain chain = mock(org.springframework.security.web.DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(chain);

        SecurityFilterChain result = config.filterChain(http);
        assertNotNull(result);
    }

    @Test
    @DisplayName("SecurityConfig — Deve obter authenticationManager")
    void testAuthenticationManager() throws Exception {
        SecurityConfig config = new SecurityConfig(null);
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        config.authenticationManager(authConfig);
        verify(authConfig, times(1)).getAuthenticationManager();
    }
}
