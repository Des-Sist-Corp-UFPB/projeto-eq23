package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Ativo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {

    Page<Ativo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Ativo> findByCategoriaId(Long categoriaId, Pageable pageable);

    Page<Ativo> findByNomeContainingIgnoreCaseAndCategoriaId(String nome, Long categoriaId, Pageable pageable);

    long countByQuantidade(int quantidade);

    @Query("SELECT COALESCE(SUM(a.preco * a.quantidade), 0) FROM Ativo a")
    BigDecimal calcularValorTotalEstoque();
}
