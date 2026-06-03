package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Categoria;
import br.ufpb.dsc.mercado.dto.CategoriaForm;
import br.ufpb.dsc.mercado.exception.CategoriaNaoEncontradaException;
import br.ufpb.dsc.mercado.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço de negócio para operações relacionadas a {@link Categoria}.
 *
 * <p>Segue o mesmo padrão do {@code ProdutoService}:
 * {@code @Transactional(readOnly = true)} como padrão na classe para leituras,
 * com {@code @Transactional} sobrescrito nos métodos de escrita.
 *
 * @author DSC - UFPB Campus IV
 */
@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Lista todas as categorias ordenadas por nome (A-Z).
     *
     * @return lista de categorias
     */
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAllByOrderByNomeAsc();
    }

    /**
     * Busca uma categoria pelo ID.
     *
     * @param id identificador da categoria
     * @return categoria encontrada
     * @throws CategoriaNaoEncontradaException se não existir
     */
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException(id));
    }

    /**
     * Cria uma nova categoria.
     *
     * @param form dados validados do formulário
     * @return categoria criada e persistida
     */
    @Transactional
    public Categoria criar(CategoriaForm form) {
        Categoria categoria = new Categoria(form.nome(), form.descricao());
        return categoriaRepository.save(categoria);
    }

    /**
     * Atualiza uma categoria existente.
     *
     * @param id   ID da categoria
     * @param form novos dados
     * @return categoria atualizada
     * @throws CategoriaNaoEncontradaException se não existir
     */
    @Transactional
    public Categoria atualizar(Long id, CategoriaForm form) {
        Categoria categoria = buscarPorId(id);
        categoria.setNome(form.nome());
        categoria.setDescricao(form.descricao());
        return categoriaRepository.save(categoria);
    }

    /**
     * Exclui uma categoria pelo ID.
     *
     * <p><strong>Atenção:</strong> a FK {@code categoria_id} em {@code produto}
     * está configurada com {@code ON DELETE SET NULL}, então produtos associados
     * não são excluídos — apenas ficam sem categoria.
     *
     * @param id ID da categoria
     * @throws CategoriaNaoEncontradaException se não existir
     */
    @Transactional
    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new CategoriaNaoEncontradaException(id);
        }
        categoriaRepository.deleteById(id);
    }

    /**
     * Conta o total de categorias cadastradas.
     *
     * @return total de categorias
     */
    public long contarTotal() {
        return categoriaRepository.count();
    }
}
