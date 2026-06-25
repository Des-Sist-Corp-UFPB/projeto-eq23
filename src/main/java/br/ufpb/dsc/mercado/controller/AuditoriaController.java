package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import br.ufpb.dsc.mercado.service.LogAuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    private static final int TAMANHO_PAGINA = 15;
    private static final String HEADER_HTMX = "HX-Request";

    private final LogAuditoriaService logAuditoriaService;

    public AuditoriaController(LogAuditoriaService logAuditoriaService) {
        this.logAuditoriaService = logAuditoriaService;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestHeader(value = HEADER_HTMX, required = false) String htmx,
            Model model) {

        Pageable pageable = PageRequest.of(pagina, TAMANHO_PAGINA);
        Page<LogAuditoria> logs = logAuditoriaService.listar(pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("paginaAtual", pagina);

        if (htmx != null) {
            return "auditoria/fragments/tabela :: tabela";
        }

        return "auditoria/index";
    }

    @GetMapping("/fragmento-tabela")
    public String fragmentoTabela(
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            Model model) {

        Pageable pageable = PageRequest.of(pagina, TAMANHO_PAGINA);
        Page<LogAuditoria> logs = logAuditoriaService.listar(pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("paginaAtual", pagina);

        return "auditoria/fragments/tabela :: tabela";
    }
}
