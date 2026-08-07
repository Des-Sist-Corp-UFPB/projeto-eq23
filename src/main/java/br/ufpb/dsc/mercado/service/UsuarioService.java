package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.exception.UsuarioNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    public static final List<String> PAPEIS_VALIDOS = List.of("ADMIN_SISTEMA", "ADMIN", "TECNICO", "CLIENTE");

    // Hierarquia: quanto maior o número, mais privilégio. Cada papel só pode criar/atribuir
    // papéis com nível ESTRITAMENTE menor que o seu — ninguém promove para o próprio nível
    // ou acima (é a falha que existia antes: qualquer ADMIN podia criar outro ADMIN à vontade).
    private static final Map<String, Integer> NIVEL_HIERARQUIA = new LinkedHashMap<>();
    static {
        NIVEL_HIERARQUIA.put("CLIENTE", 0);
        NIVEL_HIERARQUIA.put("TECNICO", 1);
        NIVEL_HIERARQUIA.put("ADMIN", 2);
        NIVEL_HIERARQUIA.put("ADMIN_SISTEMA", 3);
    }

    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaService logAuditoriaService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, LogAuditoriaService logAuditoriaService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.logAuditoriaService = logAuditoriaService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    /** Papéis que "criador" pode atribuir (criar ou promover outros) — sempre abaixo do próprio nível. */
    public List<String> papeisPermitidos(Usuario criador) {
        int nivelCriador = nivel(criador.getRole());
        return PAPEIS_VALIDOS.stream()
                .filter(papel -> nivel(papel) < nivelCriador)
                .toList();
    }

    @Transactional
    public Usuario criar(String nome, String username, String papel, String senha, Usuario criador) {
        validarCascata(papel, criador);
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com esse e-mail/usuário: " + username);
        }

        Usuario novo = new Usuario(nome, username, passwordEncoder.encode(senha));
        novo.setRole(papel);
        novo.setSenhaDefinida(true);
        Usuario salvo = usuarioRepository.save(novo);
        logAuditoriaService.registrar("CRIAR", "Usuario", salvo.getId(),
                "Usuário " + salvo.getNome() + " (" + username + ") criado com papel " + papel + " por " + criador.getUsername());
        return salvo;
    }

    @Transactional
    public Usuario alterarPapel(Long id, String novoPapel, Usuario criador) {
        validarCascata(novoPapel, criador);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        String papelAntigo = usuario.getRole();
        usuario.setRole(novoPapel);
        Usuario salvo = usuarioRepository.save(usuario);
        logAuditoriaService.registrar("EDITAR", "Usuario", salvo.getId(),
                "Papel de " + salvo.getNome() + " alterado de " + papelAntigo + " para " + novoPapel + " por " + criador.getUsername());
        return salvo;
    }

    private void validarCascata(String papel, Usuario criador) {
        if (!PAPEIS_VALIDOS.contains(papel)) {
            throw new IllegalArgumentException("Papel inválido: " + papel);
        }
        if (nivel(papel) >= nivel(criador.getRole())) {
            throw new IllegalArgumentException(
                    criador.getNome() + " (" + criador.getRole() + ") não tem permissão para atribuir o papel " + papel);
        }
    }

    private int nivel(String papel) {
        return NIVEL_HIERARQUIA.getOrDefault(papel, -1);
    }

    @Transactional
    public void trocarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        // Contas provisionadas via Google têm uma senha placeholder que ninguém conhece —
        // nesse caso não tem o que "confirmar", a sessão autenticada já é a garantia.
        if (usuario.isSenhaDefinida() && (senhaAtual == null || !passwordEncoder.matches(senhaAtual, usuario.getSenha()))) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("A nova senha precisa ter pelo menos 6 caracteres.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setSenhaDefinida(true);
        usuarioRepository.save(usuario);
        logAuditoriaService.registrar("EDITAR", "Usuario", usuario.getId(), "Senha alterada por " + usuario.getUsername());
    }

    @Transactional
    public Usuario autocadastro(String nome, String username, String senha, String confirmarSenha) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o seu nome completo.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Informe o seu e-mail/usuário.");
        }
        String cleanUsername = username.trim();
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
        if (!senha.equals(confirmarSenha)) {
            throw new IllegalArgumentException("A confirmação de senha não confere.");
        }
        if (usuarioRepository.findByUsernameIgnoreCase(cleanUsername).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com esse e-mail/usuário.");
        }

        Usuario novo = new Usuario(nome.trim(), cleanUsername, passwordEncoder.encode(senha));
        novo.setRole("CLIENTE");
        novo.setSenhaDefinida(true);
        Usuario salvo = usuarioRepository.save(novo);
        logAuditoriaService.registrar("CRIAR", "Usuario", salvo.getId(),
                "Autocadastro de integrante " + salvo.getNome() + " (" + cleanUsername + ")");
        return salvo;
    }
}

