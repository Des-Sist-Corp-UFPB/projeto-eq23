package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Chamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    Page<Chamado> findByClienteId(Long clienteId, Pageable pageable);

    Page<Chamado> findByTecnicoId(Long tecnicoId, Pageable pageable);

    Page<Chamado> findByStatus(String status, Pageable pageable);

    Page<Chamado> findByPrioridade(String prioridade, Pageable pageable);

    Page<Chamado> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    long countByStatus(String status);

    long countByClienteIdAndStatus(Long clienteId, String status);

    long countByClienteId(Long clienteId);
}
