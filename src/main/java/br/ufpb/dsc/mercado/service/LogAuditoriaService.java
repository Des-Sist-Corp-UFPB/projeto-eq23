package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import br.ufpb.dsc.mercado.repository.LogAuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class LogAuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;

    public LogAuditoriaService(LogAuditoriaRepository logAuditoriaRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
    }

    public Page<LogAuditoria> listar(Pageable pageable) {
        return logAuditoriaRepository.findAllByOrderByDataHoraDesc(pageable);
    }

    @Transactional
    public LogAuditoria registrar(String acao, String entidade, Long entidadeId, String detalhes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = "sistema";
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            usuario = auth.getName();
        }

        LogAuditoria log = new LogAuditoria(
                usuario,
                acao,
                entidade,
                entidadeId,
                detalhes,
                Instant.now()
        );
        return logAuditoriaRepository.save(log);
    }
}
