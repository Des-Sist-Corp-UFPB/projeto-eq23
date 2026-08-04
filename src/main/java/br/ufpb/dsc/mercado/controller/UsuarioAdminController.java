package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Painel administrativo de usuários: papéis (role) e criação de novas contas.
 * Acesso restrito a ROLE_ADMIN (ADMIN_SISTEMA herda essa authority — ver Usuario.getAuthorities()).
 *
 * <p>Quem pode atribuir qual papel segue a hierarquia ADMIN_SISTEMA > ADMIN > TECNICO > CLIENTE:
 * cada papel só cria/promove para papéis estritamente abaixo do próprio (ver UsuarioService).
 */
@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;

    public UsuarioAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal Object principal, Model model) {
        Usuario criador = getUsuarioLogado(principal);
        model.addAttribute("usuarios", usuarioService.listar());
        model.addAttribute("papeis", usuarioService.papeisPermitidos(criador));
        return "admin/usuarios";
    }

    @GetMapping("/novo")
    public String novoForm(@AuthenticationPrincipal Object principal, Model model) {
        Usuario criador = getUsuarioLogado(principal);
        model.addAttribute("papeis", usuarioService.papeisPermitidos(criador));
        return "admin/usuario-novo";
    }

    @PostMapping
    public String criar(
            @RequestParam String nome,
            @RequestParam String username,
            @RequestParam String papel,
            @RequestParam String senha,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {

        Usuario criador = getUsuarioLogado(principal);
        try {
            Usuario novo = usuarioService.criar(nome, username, papel, senha, criador);
            redirectAttributes.addFlashAttribute("toastMensagem",
                    "Usuário " + novo.getNome() + " criado como " + papel + ". Repasse o login (" + username + ") e a senha à pessoa.");
            return "redirect:/admin/usuarios";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("toastMensagem", e.getMessage());
            return "redirect:/admin/usuarios/novo";
        }
    }

    @PostMapping("/{id}/role")
    public String alterarPapel(
            @PathVariable Long id,
            @RequestParam String role,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {

        Usuario criador = getUsuarioLogado(principal);
        try {
            var usuario = usuarioService.alterarPapel(id, role, criador);
            redirectAttributes.addFlashAttribute("toastMensagem",
                    "Papel de " + usuario.getNome() + " atualizado para " + role);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("toastMensagem", e.getMessage());
        }

        return "redirect:/admin/usuarios";
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
}
