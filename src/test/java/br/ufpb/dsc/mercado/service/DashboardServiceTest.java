package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.repository.AtivoRepository;
import br.ufpb.dsc.mercado.repository.ChamadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private AtivoRepository ativoRepository;
    private ChamadoRepository chamadoRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        ativoRepository = Mockito.mock(AtivoRepository.class);
        chamadoRepository = Mockito.mock(ChamadoRepository.class);
        dashboardService = new DashboardService(ativoRepository, chamadoRepository);
    }

    @Test
    @DisplayName("totalAtivos — Deve retornar contagem total de ativos")
    void totalAtivos_DeveRetornarContagem() {
        when(ativoRepository.count()).thenReturn(10L);
        assertEquals(10L, dashboardService.totalAtivos());
    }

    @Test
    @DisplayName("totalChamadosAbertos — Deve retornar contagem de chamados ABERTOS")
    void totalChamadosAbertos_DeveRetornarContagem() {
        when(chamadoRepository.countByStatus("ABERTO")).thenReturn(5L);
        assertEquals(5L, dashboardService.totalChamadosAbertos());
    }

    @Test
    @DisplayName("totalChamadosEmAtendimento — Deve retornar contagem de chamados EM_ATENDIMENTO")
    void totalChamadosEmAtendimento_DeveRetornarContagem() {
        when(chamadoRepository.countByStatus("EM_ATENDIMENTO")).thenReturn(3L);
        assertEquals(3L, dashboardService.totalChamadosEmAtendimento());
    }
}
