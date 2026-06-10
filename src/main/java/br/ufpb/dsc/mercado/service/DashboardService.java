package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.repository.AtivoRepository;
import br.ufpb.dsc.mercado.repository.ChamadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final AtivoRepository ativoRepository;
    private final CategoriaService categoriaService;
    private final ChamadoRepository chamadoRepository;

    public DashboardService(AtivoRepository ativoRepository, CategoriaService categoriaService, ChamadoRepository chamadoRepository) {
        this.ativoRepository = ativoRepository;
        this.categoriaService = categoriaService;
        this.chamadoRepository = chamadoRepository;
    }

    public long totalAtivos() {
        return ativoRepository.count();
    }

    public long totalCategorias() {
        return categoriaService.contarTotal();
    }

    public long ativosSemEstoque() {
        return ativoRepository.countByQuantidade(0);
    }

    public BigDecimal valorTotalEstoque() {
        BigDecimal total = ativoRepository.calcularValorTotalEstoque();
        return total != null ? total : BigDecimal.ZERO;
    }

    public long totalChamadosAbertos() {
        return chamadoRepository.countByStatus("ABERTO");
    }

    public long totalChamadosEmAtendimento() {
        return chamadoRepository.countByStatus("EM_ATENDIMENTO");
    }
}
