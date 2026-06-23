package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.domain.Chamado;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ChamadoForm;
import br.ufpb.dsc.mercado.exception.ChamadoNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.AtivoRepository;
import br.ufpb.dsc.mercado.repository.ChamadoRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final AtivoRepository ativoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaService logAuditoriaService;

    public ChamadoService(ChamadoRepository chamadoRepository, AtivoRepository ativoRepository, UsuarioRepository usuarioRepository, LogAuditoriaService logAuditoriaService) {
        this.chamadoRepository = chamadoRepository;
        this.ativoRepository = ativoRepository;
        this.usuarioRepository = usuarioRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    public Page<Chamado> listar(Pageable pageable) {
        return chamadoRepository.findAll(pageable);
    }

    public Page<Chamado> listarPorCliente(Long clienteId, Pageable pageable) {
        return chamadoRepository.findByClienteId(clienteId, pageable);
    }

    public Page<Chamado> listarPorTecnico(Long tecnicoId, Pageable pageable) {
        return chamadoRepository.findByTecnicoId(tecnicoId, pageable);
    }

    public Chamado buscarPorId(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new ChamadoNaoEncontradoException(id));
    }

    @Transactional
    public Chamado criar(ChamadoForm form, Usuario cliente) {
        Ativo ativo = form.ativoId() != null ? ativoRepository.findById(form.ativoId()).orElse(null) : null;
        Usuario tecnico = form.tecnicoId() != null ? usuarioRepository.findById(form.tecnicoId()).orElse(null) : null;
        Usuario clienteEntidade = form.clienteId() != null ? usuarioRepository.findById(form.clienteId()).orElse(cliente) : cliente;

        Chamado chamado = new Chamado(
                form.titulo(),
                form.descricao(),
                form.prioridade(),
                form.status() != null ? form.status() : "ABERTO",
                ativo,
                tecnico,
                clienteEntidade
        );
        Chamado salvo = chamadoRepository.save(chamado);
        logAuditoriaService.registrar("CRIAR", "Chamado", salvo.getId(), "Criado chamado: " + salvo.getTitulo());
        return salvo;
    }

    @Transactional
    public Chamado atualizar(Long id, ChamadoForm form) {
        Chamado chamado = buscarPorId(id);
        chamado.setTitulo(form.titulo());
        chamado.setDescricao(form.descricao());
        chamado.setPrioridade(form.prioridade());
        if (form.status() != null) {
            chamado.setStatus(form.status());
        }
        chamado.setAtivo(form.ativoId() != null ? ativoRepository.findById(form.ativoId()).orElse(null) : null);
        chamado.setTecnico(form.tecnicoId() != null ? usuarioRepository.findById(form.tecnicoId()).orElse(null) : null);
        Chamado salvo = chamadoRepository.save(chamado);
        logAuditoriaService.registrar("ATUALIZAR", "Chamado", salvo.getId(), "Atualizado chamado: " + salvo.getTitulo());
        return salvo;
    }

    @Transactional
    public Chamado alterarStatus(Long id, String status) {
        Chamado chamado = buscarPorId(id);
        String statusAntigo = chamado.getStatus();
        chamado.setStatus(status);
        Chamado salvo = chamadoRepository.save(chamado);
        logAuditoriaService.registrar("ALTERAR_STATUS", "Chamado", salvo.getId(), "Status alterado de " + statusAntigo + " para " + status);
        return salvo;
    }

    @Transactional
    public Chamado atribuirTecnico(Long id, Long tecnicoId) {
        Chamado chamado = buscarPorId(id);
        Usuario tecnico = tecnicoId != null ? usuarioRepository.findById(tecnicoId).orElse(null) : null;
        chamado.setTecnico(tecnico);
        if (tecnico != null && "ABERTO".equals(chamado.getStatus())) {
            chamado.setStatus("EM_ATENDIMENTO");
        }
        Chamado salvo = chamadoRepository.save(chamado);
        logAuditoriaService.registrar("ATRIBUIR_TECNICO", "Chamado", salvo.getId(), "Técnico atribuído: " + (tecnico != null ? tecnico.getNome() : "Nenhum"));
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ChamadoNaoEncontradoException(id));
        logAuditoriaService.registrar("EXCLUIR", "Chamado", id, "Excluído chamado: " + chamado.getTitulo());
        chamadoRepository.deleteById(id);
    }
}
