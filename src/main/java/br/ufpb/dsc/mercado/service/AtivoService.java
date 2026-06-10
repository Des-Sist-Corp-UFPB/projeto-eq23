package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.dto.AtivoForm;
import br.ufpb.dsc.mercado.exception.AtivoNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.AtivoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AtivoService {

    private final AtivoRepository ativoRepository;

    public AtivoService(AtivoRepository ativoRepository) {
        this.ativoRepository = ativoRepository;
    }

    public Page<Ativo> listar(Pageable pageable) {
        return ativoRepository.findAll(pageable);
    }

    public Page<Ativo> buscar(String busca, Pageable pageable) {
        boolean temBusca = StringUtils.hasText(busca);

        if (temBusca) {
            return ativoRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        } else {
            return ativoRepository.findAll(pageable);
        }
    }

    public Ativo buscarPorId(Long id) {
        return ativoRepository.findById(id)
                .orElseThrow(() -> new AtivoNaoEncontradoException(id));
    }

    @Transactional
    public Ativo criar(AtivoForm form) {
        Ativo ativo = new Ativo(
                form.nome(),
                form.descricao(),
                form.numeroSerie(),
                form.status()
        );
        return ativoRepository.save(ativo);
    }

    @Transactional
    public Ativo atualizar(Long id, AtivoForm form) {
        Ativo ativo = buscarPorId(id);
        ativo.setNome(form.nome());
        ativo.setDescricao(form.descricao());
        ativo.setNumeroSerie(form.numeroSerie());
        ativo.setStatus(form.status());
        return ativoRepository.save(ativo);
    }

    @Transactional
    public void excluir(Long id) {
        if (!ativoRepository.existsById(id)) {
            throw new AtivoNaoEncontradoException(id);
        }
        ativoRepository.deleteById(id);
    }
}
