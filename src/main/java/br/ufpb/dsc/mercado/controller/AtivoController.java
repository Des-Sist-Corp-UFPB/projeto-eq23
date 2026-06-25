package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.dto.AtivoForm;
import br.ufpb.dsc.mercado.exception.AtivoNaoEncontradoException;
import br.ufpb.dsc.mercado.service.AtivoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import br.ufpb.dsc.mercado.domain.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ativos")
public class AtivoController {

    private static final int TAMANHO_PAGINA = 10;
    private static final String HEADER_HTMX = "HX-Request";

    private final AtivoService ativoService;

    public AtivoController(AtivoService ativoService) {
        this.ativoService = ativoService;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "busca", required = false, defaultValue = "") String busca,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestHeader(value = HEADER_HTMX, required = false) String htmx,
            Model model) {

        PageRequest pageRequest = PageRequest.of(pagina, TAMANHO_PAGINA, Sort.by("nome").ascending());
        Page<Ativo> ativos = ativoService.buscar(busca, pageRequest);

        model.addAttribute("ativos", ativos);
        model.addAttribute("busca", busca);
        model.addAttribute("paginaAtual", pagina);

        if (htmx != null) {
            return "ativos/fragments/tabela :: tabela";
        }

        return "ativos/lista";
    }

    @GetMapping("/fragmento-tabela")
    public String fragmentoTabela(
            @RequestParam(name = "busca", required = false, defaultValue = "") String busca,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            Model model) {

        PageRequest pageRequest = PageRequest.of(pagina, TAMANHO_PAGINA, Sort.by("nome").ascending());
        Page<Ativo> ativos = ativoService.buscar(busca, pageRequest);

        model.addAttribute("ativos", ativos);
        model.addAttribute("busca", busca);
        model.addAttribute("paginaAtual", pagina);

        return "ativos/fragments/tabela :: tabela";
    }

    private void verificarAcesso(Usuario usuario) {
        if (usuario == null || usuario.getAuthorities().stream()
                .noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Não autorizado para realizar esta ação");
        }
    }

    private Usuario getUsuarioLogado(Object principal) {
        if (principal instanceof br.ufpb.dsc.mercado.security.CustomOidcUser) {
            return ((br.ufpb.dsc.mercado.security.CustomOidcUser) principal).getUsuario();
        }
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        return null;
    }

    @GetMapping("/novo")
    public String novoForm(@AuthenticationPrincipal Object principal, Model model) {
        Usuario usuarioLogado = getUsuarioLogado(principal);
        verificarAcesso(usuarioLogado);
        model.addAttribute("form", new AtivoForm("", "", "", "ATIVO", java.util.Collections.emptyList()));
        model.addAttribute("ativo", null);
        return "ativos/fragments/form :: modal";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, @AuthenticationPrincipal Object principal, Model model) {
        Usuario usuarioLogado = getUsuarioLogado(principal);
        verificarAcesso(usuarioLogado);
        Ativo ativo = ativoService.buscarPorId(id);
        java.util.List<br.ufpb.dsc.mercado.dto.PatrimonioItemForm> patForms = ativo.getPatrimonios().stream()
                .map(p -> new br.ufpb.dsc.mercado.dto.PatrimonioItemForm(p.getCodigo(), p.getNumeroSerie()))
                .toList();
        AtivoForm form = new AtivoForm(
                ativo.getNome(),
                ativo.getDescricao(),
                ativo.getNumeroSerie(),
                ativo.getStatus(),
                patForms
        );
        model.addAttribute("form", form);
        model.addAttribute("ativo", ativo);
        return "ativos/fragments/form :: modal";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("form") AtivoForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal Object principal,
            Model model) {

        Usuario usuarioLogado = getUsuarioLogado(principal);
        verificarAcesso(usuarioLogado);
        if (bindingResult.hasErrors()) {
            model.addAttribute("ativo", null);
            return "ativos/fragments/form :: modal";
        }

        Ativo novoAtivo = ativoService.criar(form);
        model.addAttribute("ativo", novoAtivo);
        model.addAttribute("toastMensagem", "Ativo criado com sucesso!");

        return "ativos/fragments/linha :: linha";
    }

    @PutMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") AtivoForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal Object principal,
            Model model) {

        Usuario usuarioLogado = getUsuarioLogado(principal);
        verificarAcesso(usuarioLogado);
        if (bindingResult.hasErrors()) {
            Ativo ativo = ativoService.buscarPorId(id);
            model.addAttribute("ativo", ativo);
            return "ativos/fragments/form :: modal";
        }

        Ativo ativoAtualizado = ativoService.atualizar(id, form);
        model.addAttribute("ativo", ativoAtualizado);
        model.addAttribute("toastMensagem", "Ativo atualizado!");

        return "ativos/fragments/linha :: linha";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal Object principal) {
        Usuario usuarioLogado = getUsuarioLogado(principal);
        verificarAcesso(usuarioLogado);
        try {
            ativoService.excluir(id);
            return ResponseEntity.ok().build();
        } catch (AtivoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
