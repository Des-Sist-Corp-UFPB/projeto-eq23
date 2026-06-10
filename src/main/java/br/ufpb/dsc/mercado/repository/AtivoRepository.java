package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Ativo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {

    Page<Ativo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
