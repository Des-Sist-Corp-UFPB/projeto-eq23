package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.repository.AtivoRepository;
import br.ufpb.dsc.mercado.repository.ChamadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final AtivoRepository ativoRepository;
    private final ChamadoRepository chamadoRepository;

    public DashboardService(AtivoRepository ativoRepository, ChamadoRepository chamadoRepository) {
        this.ativoRepository = ativoRepository;
        this.chamadoRepository = chamadoRepository;
    }

    public long totalAtivos() {
        return ativoRepository.count();
    }

    public long totalChamadosAbertos() {
        return chamadoRepository.countByStatus("ABERTO");
    }

    public long totalChamadosEmAtendimento() {
        return chamadoRepository.countByStatus("EM_ATENDIMENTO");
    }
}
