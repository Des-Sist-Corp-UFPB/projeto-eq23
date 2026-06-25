package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Chamado;

public interface EmailService {
    void enviarNotificacaoChamadoCriado(Chamado chamado);
}
