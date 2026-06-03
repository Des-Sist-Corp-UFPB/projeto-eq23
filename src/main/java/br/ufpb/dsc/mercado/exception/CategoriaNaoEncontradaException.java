package br.ufpb.dsc.mercado.exception;

/**
 * Exceção lançada quando uma categoria não é encontrada pelo ID informado.
 *
 * <p>Estende {@code RuntimeException} para ser uma unchecked exception —
 * não precisa ser declarada no throws de cada método, mas ainda causa rollback
 * automático nas transações marcadas com {@code @Transactional}.
 *
 * @author DSC - UFPB Campus IV
 */
public class CategoriaNaoEncontradaException extends RuntimeException {

    /**
     * @param id identificador da categoria que não foi encontrada
     */
    public CategoriaNaoEncontradaException(Long id) {
        super("Categoria não encontrada com o id: " + id);
    }
}
