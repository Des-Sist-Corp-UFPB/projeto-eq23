package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Chamado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class MockEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MockEmailService.class);

    private final String from;
    private final String to;

    public MockEmailService(String from, String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void enviarNotificacaoChamadoCriado(Chamado chamado) {
        String destinatario = obterDestinatario(chamado);
        log.info("=== SIMULAÇÃO DE ENVIO DE E-MAIL (CRIADO) ===");
        log.info("Remetente: {}", from);
        log.info("Destinatário: {}", destinatario);
        log.info("Assunto: Novo Chamado Aberto #{} - {}", chamado.getId(), chamado.getTitulo());
        log.info("Corpo: Olá, um novo chamado foi criado por {}. Status: {}. Descrição: {}",
                chamado.getCliente() != null ? chamado.getCliente().getNome() : "Cliente", chamado.getStatus(), chamado.getDescricao());
        log.info("==============================================");
    }

    @Override
    public void enviarNotificacaoChamadoAtualizado(Chamado chamado, String motivo) {
        String destinatario = obterDestinatario(chamado);
        log.info("=== SIMULAÇÃO DE ENVIO DE E-MAIL (ATUALIZADO) ===");
        log.info("Remetente: {}", from);
        log.info("Destinatário: {}", destinatario);
        log.info("Assunto: Atualização no Chamado #{} - {}", chamado.getId(), chamado.getTitulo());
        log.info("Corpo: Chamado #{}. Motivo/Status: {}. Descrição: {}",
                chamado.getId(), motivo, chamado.getDescricao());
        log.info("================================================");
    }

    private String obterDestinatario(Chamado chamado) {
        if (chamado != null && chamado.getCliente() != null && StringUtils.hasText(chamado.getCliente().getUsername()) && chamado.getCliente().getUsername().contains("@")) {
            return chamado.getCliente().getUsername();
        }
        return this.to;
    }
}
