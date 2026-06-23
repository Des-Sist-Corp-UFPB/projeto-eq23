package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("GET / — Deve preencher o modelo com os totais corretos e retornar a view 'home'")
    void home_DeveRetornarViewHomeEModelComTotais() throws Exception {
        when(dashboardService.totalAtivos()).thenReturn(15L);
        when(dashboardService.totalChamadosAbertos()).thenReturn(4L);
        when(dashboardService.totalChamadosEmAtendimento()).thenReturn(2L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("totalProdutos", 15L))
                .andExpect(model().attribute("totalChamadosAbertos", 4L))
                .andExpect(model().attribute("totalChamadosEmAtendimento", 2L));
    }
}
