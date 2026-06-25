package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.security.CustomOidcUser;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UsuarioRepository usuarioRepository;

    public CustomOidcUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();

        if (email == null || !email.endsWith("@dcx.ufpb.br")) {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_domain",
                    "Domínio de e-mail não autorizado. Apenas e-mails @dcx.ufpb.br são permitidos.", null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.getDescription());
        }

        String resolvedNome = oidcUser.getFullName();
        if (resolvedNome == null) {
            resolvedNome = oidcUser.getGivenName();
        }
        if (resolvedNome == null) {
            resolvedNome = email;
        }
        final String nome = resolvedNome;

        String username = email;
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseGet(() -> {
                    // Cadastra o usuário automaticamente com senha hash randômica segura
                    Usuario novo = new Usuario(nome, username, "$2a$10$" + UUID.randomUUID().toString().replace("-", ""));
                    return usuarioRepository.save(novo);
                });

        return new CustomOidcUser(usuario, oidcUser);
    }
}
