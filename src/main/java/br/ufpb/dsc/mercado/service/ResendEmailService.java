package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Chamado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    private final RestClient restClient;
    private final String apiUrl;
    private final String apiKey;
    private final String from;
    private final String to;

    public ResendEmailService(RestClient.Builder restClientBuilder, String apiUrl, String apiKey, String from, String to) {
        this.restClient = restClientBuilder.build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.from = from;
        this.to = to;
    }

    @Override
    public void enviarNotificacaoChamadoCriado(Chamado chamado) {
        String subject = "Novo Chamado Aberto #" + chamado.getId() + " - " + chamado.getTitulo();
        String htmlBody = String.format(
                "<h3>Novo Chamado de Suporte Aberto</h3>" +
                "<p><strong>Título:</strong> %s</p>" +
                "<p><strong>Descrição:</strong> %s</p>" +
                "<p><strong>Prioridade:</strong> %s</p>" +
                "<p><strong>Cliente:</strong> %s</p>" +
                "<br/>" +
                "<p><em>Este é um e-mail automático gerado pelo sistema SparkTech.</em></p>",
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getPrioridade(),
                chamado.getCliente().getNome()
        );

        EmailPayload payload = new EmailPayload(from, to, subject, htmlBody);

        try {
            restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Notificação de e-mail enviada com sucesso para o chamado #{}", chamado.getId());
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de notificação para o chamado #{}: {}", chamado.getId(), e.getMessage(), e);
        }
    }

    private record EmailPayload(String from, String to, String subject, String html) {}
}
