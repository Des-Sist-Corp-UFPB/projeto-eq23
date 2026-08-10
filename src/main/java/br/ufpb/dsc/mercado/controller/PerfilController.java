package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Perfil do usuário logado: trocar senha e conectar conta Google.
 * Acessível a qualquer usuário autenticado, independente do papel.
 */
@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String perfil(@AuthenticationPrincipal Object principal, Model model) {
        model.addAttribute("usuario", getUsuarioLogado(principal));
        return "perfil";
    }

    @PostMapping("/senha")
    public String trocarSenha(
            @RequestParam(required = false) String senhaAtual,
            @RequestParam String novaSenha,
            @RequestParam String confirmarSenha,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = getUsuarioLogado(principal);

        if (!novaSenha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("toastMensagem", "As senhas não coincidem.");
            return "redirect:/perfil";
        }

        try {
            usuarioService.trocarSenha(usuario.getId(), senhaAtual, novaSenha);
            redirectAttributes.addFlashAttribute("toastMensagem", "Senha atualizada com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("toastMensagem", e.getMessage());
        }

        return "redirect:/perfil";
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
