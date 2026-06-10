package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Ativo;
import br.ufpb.dsc.mercado.dto.AtivoForm;
import br.ufpb.dsc.mercado.exception.AtivoNaoEncontradoException;
import br.ufpb.dsc.mercado.service.CategoriaService;
import br.ufpb.dsc.mercado.service.AtivoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
    private final CategoriaService categoriaService;

    public AtivoController(AtivoService ativoService, CategoriaService categoriaService) {
        this.ativoService = ativoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "busca", required = false, defaultValue = "") String busca,
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestHeader(value = HEADER_HTMX, required = false) String htmx,
            Model model) {

        PageRequest pageRequest = PageRequest.of(pagina, TAMANHO_PAGINA, Sort.by("nome").ascending());
        Page<Ativo> ativos = ativoService.buscar(busca, categoriaId, pageRequest);

        model.addAttribute("ativos", ativos);
        model.addAttribute("busca", busca);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("categorias", categoriaService.listarTodas());

        if (htmx != null) {
            return "ativos/fragments/tabela :: tabela";
        }

        return "ativos/lista";
    }

    @GetMapping("/fragmento-tabela")
    public String fragmentoTabela(
            @RequestParam(name = "busca", required = false, defaultValue = "") String busca,
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            Model model) {

        PageRequest pageRequest = PageRequest.of(pagina, TAMANHO_PAGINA, Sort.by("nome").ascending());
        Page<Ativo> ativos = ativoService.buscar(busca, categoriaId, pageRequest);

        model.addAttribute("ativos", ativos);
        model.addAttribute("busca", busca);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("paginaAtual", pagina);

        return "ativos/fragments/tabela :: tabela";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("form", new AtivoForm("", "", null, 0, null, "", "ATIVO"));
        model.addAttribute("ativo", null);
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "ativos/fragments/form :: modal";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Ativo ativo = ativoService.buscarPorId(id);
        Long categoriaId = ativo.getCategoria() != null ? ativo.getCategoria().getId() : null;
        AtivoForm form = new AtivoForm(
                ativo.getNome(),
                ativo.getDescricao(),
                ativo.getPreco(),
                ativo.getQuantidade(),
                categoriaId,
                ativo.getNumeroSerie(),
                ativo.getStatus()
        );
        model.addAttribute("form", form);
        model.addAttribute("ativo", ativo);
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "ativos/fragments/form :: modal";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("form") AtivoForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("ativo", null);
            model.addAttribute("categorias", categoriaService.listarTodas());
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
            Model model) {

        if (bindingResult.hasErrors()) {
            Ativo ativo = ativoService.buscarPorId(id);
            model.addAttribute("ativo", ativo);
            model.addAttribute("categorias", categoriaService.listarTodas());
            return "ativos/fragments/form :: modal";
        }

        Ativo ativoAtualizado = ativoService.atualizar(id, form);
        model.addAttribute("ativo", ativoAtualizado);
        model.addAttribute("toastMensagem", "Ativo atualizado!");

        return "ativos/fragments/linha :: linha";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            ativoService.excluir(id);
            return ResponseEntity.ok().build();
        } catch (AtivoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
