package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Patrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatrimonioRepository extends JpaRepository<Patrimonio, Long> {
    Optional<Patrimonio> findByCodigo(String codigo);
    Optional<Patrimonio> findByNumeroSerie(String numeroSerie);
    List<Patrimonio> findByAtivoId(Long ativoId);
}
