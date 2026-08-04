package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Chamado;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ChamadoForm;
import br.ufpb.dsc.mercado.exception.ChamadoNaoEncontradoException;
import br.ufpb.dsc.mercado.ia.ChamadoClassificacao;
import br.ufpb.dsc.mercado.ia.ClassificacaoIaService;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.repository.PatrimonioRepository;
import br.ufpb.dsc.mercado.service.AtivoService;
import br.ufpb.dsc.mercado.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/chamados")
public class ChamadoController {

    private static final int TAMANHO_PAGINA = 10;
    private static final String HEADER_HTMX = "HX-Request";

    private final ChamadoService chamadoService;
    private final AtivoService ativoService;
    private final UsuarioRepository usuarioRepository;
    private final PatrimonioRepository patrimonioRepository;
    private final ClassificacaoIaService classificacaoIaService;

    public ChamadoController(ChamadoService chamadoService, AtivoService ativoService, UsuarioRepository usuarioRepository,
                              PatrimonioRepository patrimonioRepository, ClassificacaoIaService classificacaoIaService) {
        this.chamadoService = chamadoService;
        this.ativoService = ativoService;
        this.usuarioRepository = usuarioRepository;
        this.patrimonioRepository = patrimonioRepository;
        this.classificacaoIaService = classificacaoIaService;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestHeader(value = HEADER_HTMX, required = false) String htmx,
            @AuthenticationPrincipal Object principal,
            Model model) {

        Usuario usuarioLogado = getUsuarioLogado(principal);
        PageRequest pageRequest = PageRequest.of(pagina, TAMANHO_PAGINA, Sort.by("criadoEm").descending());
        
        Page<Chamado> chamados;
        // Simple authorization check: if user is not ADMIN or TECNICO, list only their tickets
        if (usuarioLogado != null && usuarioLogado.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_TECNICO".equals(a.getAuthority()))) {
            chamados = chamadoService.listar(pageRequest);
        } else {
            Long usuarioId = usuarioLogado != null ? usuarioLogado.getId() : -1L;
            chamados = chamadoService.listarPorCliente(usuarioId, pageRequest);
        }

        model.addAttribute("chamados", chamados);
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("tecnicos", usuarioRepository.findByRoleOrderByNomeAsc("TECNICO")); // for filters/assignment

        if (htmx != null) {
            return "chamados/fragments/tabela :: tabela";
        }

        return "chamados/lista";
    }

    @GetMapping("/abrir")
    public String abrirForm(Model model) {
        model.addAttribute("form", new ChamadoForm("", "", "MEDIA", "ABERTO", null, null, null, null, null));
        model.addAttribute("ativos", ativoService.listar(PageRequest.of(0, 100)).getContent());
        model.addAttribute("patrimonios", patrimonioRepository.findAll());
        return "chamados/abrir";
    }

    @PostMapping("/abrir")
    public String abrirCriar(
            @Valid @ModelAttribute("form") ChamadoForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        Usuario usuarioLogado = getUsuarioLogado(principal);
        if (bindingResult.hasErrors()) {
            model.addAttribute("ativos", ativoService.listar(PageRequest.of(0, 100)).getContent());
            model.addAttribute("patrimonios", patrimonioRepository.findAll());
            return "chamados/abrir";
        }

        chamadoService.criar(form, usuarioLogado);
        redirectAttributes.addFlashAttribute("toastMensagem", "Chamado aberto com sucesso!");
        return "redirect:/chamados";
    }

    /**
     * Sugestão de categoria/prioridade via IA a partir do título e descrição digitados.
     * É só uma sugestão pré-preenchida no formulário — quem abre o chamado sempre pode
     * mudar antes de enviar, a IA nunca decide sozinha.
     */
    @PostMapping("/sugestao-ia")
    public String sugestaoIa(
            @RequestParam(defaultValue = "") String titulo,
            @RequestParam(defaultValue = "") String descricao,
            Model model) {

        ChamadoClassificacao sugestao = classificacaoIaService.classificar(titulo, descricao).orElse(null);
        model.addAttribute("sugestao", sugestao);
        return "chamados/fragments/sugestao-ia :: sugestao";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("form", new ChamadoForm("", "", "MEDIA", "ABERTO", null, null, null, null, null));
        model.addAttribute("chamado", null);
        model.addAttribute("ativos", ativoService.listar(PageRequest.of(0, 100)).getContent());
        model.addAttribute("patrimonios", patrimonioRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "chamados/fragments/form :: modal";
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

    private void verificarAcessoChamado(Chamado chamado, Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            throw new AccessDeniedException("Não autenticado");
        }
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_TECNICO".equals(a.getAuthority()));
        boolean isCliente = chamado.getCliente() != null && chamado.getCliente().getId().equals(usuarioLogado.getId());
        boolean isTecnico = chamado.getTecnico() != null && chamado.getTecnico().getId().equals(usuarioLogado.getId());

        if (!isAdmin && !isCliente && !isTecnico) {
            throw new AccessDeniedException("Não autorizado a acessar ou modificar este chamado");
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, @AuthenticationPrincipal Object principal, Model model) {
        Usuario usuarioLogado = getUsuarioLogado(principal);
        Chamado chamado = chamadoService.buscarPorId(id);
        verificarAcessoChamado(chamado, usuarioLogado);

        Long ativoId = chamado.getAtivo() != null ? chamado.getAtivo().getId() : null;
        Long tecnicoId = chamado.getTecnico() != null ? chamado.getTecnico().getId() : null;
        Long clienteId = chamado.getCliente() != null ? chamado.getCliente().getId() : null;
        Long patrimonioId = chamado.getPatrimonio() != null ? chamado.getPatrimonio().getId() : null;
        ChamadoForm form = new ChamadoForm(
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                ativoId,
                tecnicoId,
                clienteId,
                patrimonioId,
                chamado.getCategoria()
        );

        model.addAttribute("form", form);
        model.addAttribute("chamado", chamado);
        model.addAttribute("ativos", ativoService.listar(PageRequest.of(0, 100)).getContent());
        model.addAttribute("patrimonios", patrimonioRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "chamados/fragments/form :: modal";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("form") ChamadoForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal Object principal,
            Model model) {

        Usuario usuarioLogado = getUsuarioLogado(principal);
        if (bindingResult.hasErrors()) {
            model.addAttribute("chamado", null);
            model.addAttribute("ativos", ativoService.listar(PageRequest.of(0, 100)).getContent());
            model.addAttribute("patrimonios", patrimonioRepository.findAll());
            model.addAttribute("usuarios", usuarioRepository.findAll());
            return "chamados/fragments/form :: modal";
        }

        Chamado novoChamado = chamadoService.criar(form, usuarioLogado);
        model.addAttribute("chamado", novoChamado);
        model.addAttribute("toastMensagem", "Chamado aberto com sucesso!");

        return "chamados/fragments/linha :: linha";
    }

    @PutMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") ChamadoForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal Object principal,
            Model model) {

        Usuario usuarioLogado = getUsuarioLogado(principal);
        Chamado chamado = chamadoService.buscarPorId(id);
        verificarAcessoChamado(chamado, usuarioLogado);

        if (bindingResult.hasErrors()) {
            model.addAttribute("chamado", chamado);
            model.addAttribute("ativos", ativoService.listar(PageRequest.of(0, 100)).getContent());
            model.addAttribute("usuarios", usuarioRepository.findAll());
            return "chamados/fragments/form :: modal";
        }

        Chamado chamadoAtualizado = chamadoService.atualizar(id, form);
        model.addAttribute("chamado", chamadoAtualizado);
        model.addAttribute("toastMensagem", "Chamado atualizado!");

        return "chamados/fragments/linha :: linha";
    }

    @PutMapping("/{id}/status")
    public String alterarStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @AuthenticationPrincipal Object principal,
            Model model) {
        
        Usuario usuarioLogado = getUsuarioLogado(principal);
        Chamado chamado = chamadoService.buscarPorId(id);
        verificarAcessoChamado(chamado, usuarioLogado);

        Chamado chamadoAtualizado = chamadoService.alterarStatus(id, status);
        model.addAttribute("chamado", chamadoAtualizado);
        model.addAttribute("toastMensagem", "Status alterado para " + status);
        return "chamados/fragments/linha :: linha";
    }

    @PutMapping("/{id}/atribuir")
    public String atribuirTecnico(
            @PathVariable Long id,
            @RequestParam("tecnicoId") Long tecnicoId,
            @AuthenticationPrincipal Object principal,
            Model model) {
        
        Usuario usuarioLogado = getUsuarioLogado(principal);
        Chamado chamado = chamadoService.buscarPorId(id);
        verificarAcessoChamado(chamado, usuarioLogado);

        Chamado chamadoAtualizado = chamadoService.atribuirTecnico(id, tecnicoId);
        model.addAttribute("chamado", chamadoAtualizado);
        model.addAttribute("toastMensagem", "Técnico atribuído com sucesso!");
        return "chamados/fragments/linha :: linha";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal Object principal) {
        try {
            Usuario usuarioLogado = getUsuarioLogado(principal);
            Chamado chamado = chamadoService.buscarPorId(id);
            verificarAcessoChamado(chamado, usuarioLogado);
            chamadoService.excluir(id);
            return ResponseEntity.ok().build();
        } catch (ChamadoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
