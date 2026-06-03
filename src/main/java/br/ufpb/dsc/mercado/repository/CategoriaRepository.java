package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade {@link Categoria}.
 *
 * <p>Estendendo {@code JpaRepository} ganhamos CRUD completo automaticamente.
 * Métodos adicionais abaixo seguem a convenção de "Derived Queries" do Spring Data:
 * o nome do método é traduzido para JPQL pelo framework em tempo de inicialização.
 *
 * @author DSC - UFPB Campus IV
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Retorna todas as categorias ordenadas pelo nome (A-Z).
     * Usado nos selects de formulários de produto.
     *
     * @return lista de categorias ordenadas por nome
     */
    List<Categoria> findAllByOrderByNomeAsc();

    /**
     * Verifica se já existe uma categoria com o nome informado (case-insensitive).
     * Útil para validar unicidade antes de salvar.
     *
     * @param nome nome a verificar
     * @return {@code true} se já existir uma categoria com esse nome
     */
    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Busca uma categoria pelo nome exato (case-insensitive).
     *
     * @param nome nome da categoria
     * @return Optional com a categoria encontrada, ou vazio se não existir
     */
    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
