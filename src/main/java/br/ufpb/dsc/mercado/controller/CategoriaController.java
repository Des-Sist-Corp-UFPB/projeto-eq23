package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Categoria;
import br.ufpb.dsc.mercado.dto.CategoriaForm;
import br.ufpb.dsc.mercado.exception.CategoriaNaoEncontradaException;
import br.ufpb.dsc.mercado.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável pelo CRUD de categorias.
 *
 * <p>Segue o mesmo padrão do {@code ProdutoController}:
 * requisições HTMX retornam fragmentos Thymeleaf; requisições normais retornam páginas completas.
 *
 * @author DSC - UFPB Campus IV
 */
@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private static final String HEADER_HTMX = "HX-Request";

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // =========================================================================
    // LISTAGEM
    // =========================================================================

    /**
     * Exibe a página de categorias.
     *
     * @param model modelo Thymeleaf
     * @return template da listagem
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "categorias/lista";
    }

    // =========================================================================
    // FORMULÁRIO (NOVO / EDITAR)
    // =========================================================================

    /**
     * Retorna o fragmento do formulário para criar uma nova categoria.
     */
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("form", new CategoriaForm(null, null));
        model.addAttribute("categoria", null);
        return "categorias/fragments/form :: modal";
    }

    /**
     * Retorna o fragmento do formulário preenchido para edição.
     */
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.buscarPorId(id);
        CategoriaForm form = new CategoriaForm(categoria.getNome(), categoria.getDescricao());
        model.addAttribute("form", form);
        model.addAttribute("categoria", categoria);
        return "categorias/fragments/form :: modal";
    }

    // =========================================================================
    // CRIAÇÃO
    // =========================================================================

    /**
     * Processa o formulário de criação.
     *
     * <p>Retorna a nova linha da tabela via HTMX ou o formulário com erros.
     * Também define o header {@code HX-Trigger: categoriasSalvas} para o toast.
     */
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("form") CategoriaForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categoria", null);
            return "categorias/fragments/form :: modal";
        }

        Categoria novaCategoria = categoriaService.criar(form);
        model.addAttribute("categoria", novaCategoria);
        model.addAttribute("toastMensagem", "Categoria criada com sucesso!");
        return "categorias/fragments/linha :: linha";
    }

    // =========================================================================
    // ATUALIZAÇÃO
    // =========================================================================

    /**
     * Processa o formulário de edição.
     */
    @PutMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") CategoriaForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            Categoria categoria = categoriaService.buscarPorId(id);
            model.addAttribute("categoria", categoria);
            return "categorias/fragments/form :: modal";
        }

        Categoria categoriaAtualizada = categoriaService.atualizar(id, form);
        model.addAttribute("categoria", categoriaAtualizada);
        model.addAttribute("toastMensagem", "Categoria atualizada!");
        return "categorias/fragments/linha :: linha";
    }

    // =========================================================================
    // EXCLUSÃO
    // =========================================================================

    /**
     * Exclui uma categoria.
     *
     * <p>Retorna 200 OK com body vazio para o HTMX remover a linha da tabela.
     * Produtos associados ficam sem categoria (ON DELETE SET NULL no banco).
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            categoriaService.excluir(id);
            return ResponseEntity.ok().build();
        } catch (CategoriaNaoEncontradaException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
