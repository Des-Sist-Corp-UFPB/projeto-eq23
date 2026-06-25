package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Chamado;
import br.ufpb.dsc.mercado.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
@DisplayName("EmailService — Testes de Integração com API Externa")
class EmailServiceTest {

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private MockRestServiceServer server;

    @Test
    @DisplayName("ResendEmailService: deve enviar POST com cabeçalhos e payload corretos")
    void enviarNotificacaoChamadoCriado_deveEnviarRequestCorreto() {
        String apiUrl = "https://api.resend.com/emails";
        String apiKey = "re_testkey12345";
        String from = "onboarding@resend.dev";
        String to = "suporte@sparktech.com";

        ResendEmailService resendEmailService = new ResendEmailService(
                restClientBuilder, apiUrl, apiKey, from, to
        );

        Usuario cliente = new Usuario("Gabriel", "gabriel", "senha");
        Chamado chamado = new Chamado("Impressora Quebrada", "Não liga na tomada", "ALTA", "ABERTO", null, null, cliente);
        chamado.setId(42L);

        // Configura a expectativa do servidor mock
        server.expect(requestTo(apiUrl))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer re_testkey12345"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from").value(from))
                .andExpect(jsonPath("$.to").value(to))
                .andExpect(jsonPath("$.subject").value("Novo Chamado Aberto #42 - Impressora Quebrada"))
                .andExpect(jsonPath("$.html").value(org.hamcrest.Matchers.containsString("Não liga na tomada")))
                .andRespond(withSuccess());

        // Executa o envio
        resendEmailService.enviarNotificacaoChamadoCriado(chamado);

        // Valida se as chamadas de rede esperadas ocorreram
        server.verify();
    }

    @Test
    @DisplayName("MockEmailService: deve logar e-mail sem disparar chamadas de rede")
    void mockEmailService_deveExecutarSemErro() {
        MockEmailService mockEmailService = new MockEmailService("from@test.com", "to@test.com");
        Usuario cliente = new Usuario("Gabriel", "gabriel", "senha");
        Chamado chamado = new Chamado("Impressora Quebrada", "Não liga na tomada", "ALTA", "ABERTO", null, null, cliente);
        chamado.setId(42L);

        assertThatCode(() -> mockEmailService.enviarNotificacaoChamadoCriado(chamado))
                .doesNotThrowAnyException();
    }
}
