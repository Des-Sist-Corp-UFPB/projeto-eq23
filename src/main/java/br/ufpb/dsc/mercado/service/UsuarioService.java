package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.exception.UsuarioNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    public static final List<String> PAPEIS_VALIDOS = List.of("ADMIN", "TECNICO", "CLIENTE");

    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaService logAuditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository, LogAuditoriaService logAuditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    @Transactional
    public Usuario alterarPapel(Long id, String novoPapel) {
        if (!PAPEIS_VALIDOS.contains(novoPapel)) {
            throw new IllegalArgumentException("Papel inválido: " + novoPapel);
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        String papelAntigo = usuario.getRole();
        usuario.setRole(novoPapel);
        Usuario salvo = usuarioRepository.save(usuario);
        logAuditoriaService.registrar("EDITAR", "Usuario", salvo.getId(),
                "Papel de " + salvo.getNome() + " alterado de " + papelAntigo + " para " + novoPapel);
        return salvo;
    }
}
