package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.security.CustomOidcUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UsuarioRepository usuarioRepository;

    public CustomOidcUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();

        if (email == null || !email.endsWith("@dcx.ufpb.br")) {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_domain",
                    "Domínio de e-mail não autorizado. Apenas e-mails @dcx.ufpb.br são permitidos.", null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.getDescription());
        }

        // Se já existe uma sessão autenticada (não anônima) nessa mesma requisição, essa chamada
        // veio do botão "Conectar conta Google" em /perfil — não é um login novo, é um pedido de
        // vínculo à conta JÁ logada (local ou já-Google). Ver PerfilController/perfil.html.
        Usuario usuarioSessaoAtual = usuarioJaAutenticado();
        if (usuarioSessaoAtual != null) {
            return new CustomOidcUser(vincularGoogle(usuarioSessaoAtual, email), oidcUser);
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

    private Usuario usuarioJaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomOidcUser custom) {
            return custom.getUsuario();
        }
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        return null;
    }

    private Usuario vincularGoogle(Usuario usuarioAtual, String email) {
        Optional<Usuario> existente = usuarioRepository.findByUsername(email);
        if (existente.isPresent() && !existente.get().getId().equals(usuarioAtual.getId())) {
            OAuth2Error erro = new OAuth2Error("email_ja_vinculado",
                    "Esse e-mail Google já está vinculado a outra conta do sistema.", null);
            throw new OAuth2AuthenticationException(erro, erro.getDescription());
        }

        Usuario usuario = usuarioRepository.findById(usuarioAtual.getId()).orElse(usuarioAtual);
        usuario.setUsername(email);
        return usuarioRepository.save(usuario);
    }
}
