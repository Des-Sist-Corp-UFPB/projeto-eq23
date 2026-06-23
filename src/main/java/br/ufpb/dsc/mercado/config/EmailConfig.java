package br.ufpb.dsc.mercado.config;

import br.ufpb.dsc.mercado.service.EmailService;
import br.ufpb.dsc.mercado.service.MockEmailService;
import br.ufpb.dsc.mercado.service.ResendEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class EmailConfig {

    @Value("${app.email.api-url}")
    private String apiUrl;

    @Value("${app.email.api-key}")
    private String apiKey;

    @Value("${app.email.from}")
    private String from;

    @Value("${app.email.to}")
    private String to;

    @Bean
    public EmailService emailService(RestClient.Builder restClientBuilder) {
        if (StringUtils.hasText(apiKey)) {
            return new ResendEmailService(restClientBuilder, apiUrl, apiKey, from, to);
        } else {
            return new MockEmailService(from, to);
        }
    }
}
