package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Serviço responsável por agregar as estatísticas exibidas no dashboard.
 *
 * <p>Todas as operações são somente-leitura, sem necessidade de transações de escrita.
 *
 * @author DSC - UFPB Campus IV
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaService categoriaService;

    public DashboardService(ProdutoRepository produtoRepository, CategoriaService categoriaService) {
        this.produtoRepository = produtoRepository;
        this.categoriaService = categoriaService;
    }

    /**
     * @return total de produtos cadastrados
     */
    public long totalProdutos() {
        return produtoRepository.count();
    }

    /**
     * @return total de categorias cadastradas
     */
    public long totalCategorias() {
        return categoriaService.contarTotal();
    }

    /**
     * @return quantidade de produtos com estoque zerado
     */
    public long produtosSemEstoque() {
        return produtoRepository.countByQuantidade(0);
    }

    /**
     * Calcula o valor total do estoque (soma de preço × quantidade de todos os produtos).
     *
     * @return valor total formatado como {@code BigDecimal}, nunca {@code null}
     */
    public BigDecimal valorTotalEstoque() {
        BigDecimal total = produtoRepository.calcularValorTotalEstoque();
        return total != null ? total : BigDecimal.ZERO;
    }
}
