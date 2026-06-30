package br.ufpb.dsc.mercado.security;

import br.ufpb.dsc.mercado.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOidcUserTest {

    @Mock
    private OidcUser delegate;

    @Test
    @DisplayName("CustomOidcUser - Testar delegacoes")
    void testCustomOidcUserDelegations() {
        Usuario usuario = new Usuario("Maria", "maria@dcx.ufpb.br", "senha");
        usuario.setId(1L);

        OidcIdToken idToken = mock(OidcIdToken.class);
        OidcUserInfo userInfo = mock(OidcUserInfo.class);
        Map<String, Object> claims = Collections.singletonMap("sub", "123");
        Map<String, Object> attributes = Collections.singletonMap("name", "Maria");

        when(delegate.getClaims()).thenReturn(claims);
        when(delegate.getUserInfo()).thenReturn(userInfo);
        when(delegate.getIdToken()).thenReturn(idToken);
        when(delegate.getAttributes()).thenReturn(attributes);

        CustomOidcUser oidcUser = new CustomOidcUser(usuario, delegate);

        assertEquals(claims, oidcUser.getClaims());
        assertEquals(userInfo, oidcUser.getUserInfo());
        assertEquals(idToken, oidcUser.getIdToken());
        assertEquals(attributes, oidcUser.getAttributes());
        assertEquals(usuario, oidcUser.getUsuario());
        assertEquals("Maria", oidcUser.getName());
        assertNotNull(oidcUser.getAuthorities());
    }
}
