package br.ufpb.dsc.mercado.controller;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.willThrow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PingController.class)
@AutoConfigureMockMvc(addFilters = false)
class PingControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("GET /ping — Deve responder OK com status ok quando o banco responde")
    void ping_DeveRetornarOk() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")))
                .andExpect(jsonPath("$.service", is("eq23")))
                .andExpect(jsonPath("$.database", is("ok")));
    }

    @Test
    @DisplayName("GET /ping — Deve responder 503 com status down quando o banco não responde")
    void ping_DeveRetornarDown_QuandoBancoFalha() throws Exception {
        willThrow(new DataAccessResourceFailureException("sem conexão"))
                .given(jdbcTemplate).execute("SELECT 1");

        mockMvc.perform(get("/ping"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is("down")))
                .andExpect(jsonPath("$.database", is("down")));
    }
}
