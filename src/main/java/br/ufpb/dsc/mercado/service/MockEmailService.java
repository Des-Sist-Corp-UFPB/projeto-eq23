package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Chamado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.info("=== SIMULAÇÃO DE ENVIO DE E-MAIL ===");
        log.info("Remetente: {}", from);
        log.info("Destinatário: {}", to);
        log.info("Assunto: Novo Chamado Aberto #{} - {}", chamado.getId(), chamado.getTitulo());
        log.info("Corpo: Olá, um novo chamado foi criado por {}. Prioridade: {}. Descrição: {}",
                chamado.getCliente().getNome(), chamado.getPrioridade(), chamado.getDescricao());
        log.info("====================================");
    }
}
