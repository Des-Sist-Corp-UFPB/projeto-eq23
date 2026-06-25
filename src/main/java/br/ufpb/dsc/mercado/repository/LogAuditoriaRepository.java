package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    Page<LogAuditoria> findAllByOrderByDataHoraDesc(Pageable pageable);
}
