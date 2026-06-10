package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Categoria;
import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.dto.AtivoForm;
import br.ufpb.dsc.mercado.exception.AtivoNaoEncontradoException;
import br.ufpb.dsc.mercado.repository.CategoriaRepository;
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
    private final CategoriaRepository categoriaRepository;

    public AtivoService(AtivoRepository ativoRepository, CategoriaRepository categoriaRepository) {
        this.ativoRepository = ativoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Page<Ativo> listar(Pageable pageable) {
        return ativoRepository.findAll(pageable);
    }

    public Page<Ativo> buscar(String busca, Long categoriaId, Pageable pageable) {
        boolean temBusca = StringUtils.hasText(busca);
        boolean temCategoria = categoriaId != null;

        if (temBusca && temCategoria) {
            return ativoRepository.findByNomeContainingIgnoreCaseAndCategoriaId(busca.trim(), categoriaId, pageable);
        } else if (temBusca) {
            return ativoRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        } else if (temCategoria) {
            return ativoRepository.findByCategoriaId(categoriaId, pageable);
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
        Categoria categoria = resolverCategoria(form.categoriaId());
        Ativo ativo = new Ativo(
                form.nome(),
                form.descricao(),
                form.preco(),
                form.quantidade(),
                categoria,
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
        ativo.setPreco(form.preco());
        ativo.setQuantidade(form.quantidade());
        ativo.setCategoria(resolverCategoria(form.categoriaId()));
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

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId).orElse(null);
    }
}
