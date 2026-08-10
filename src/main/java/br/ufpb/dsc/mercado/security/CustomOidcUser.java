package br.ufpb.dsc.mercado.security;

import br.ufpb.dsc.mercado.domain.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

public class CustomOidcUser extends Usuario implements OidcUser {
    
    private final OidcUser delegate;
    private final Usuario usuario;

    public CustomOidcUser(Usuario usuario, OidcUser delegate) {
        super(usuario.getNome(), usuario.getUsername(), usuario.getSenha());
        this.setId(usuario.getId());
        this.usuario = usuario;
        this.delegate = delegate;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getAuthorities();
    }

    @Override
    public String getName() {
        return usuario.getNome();
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
