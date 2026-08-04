package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Painel administrativo simples para gestão de papéis (role) dos usuários.
 * Acesso restrito a ROLE_ADMIN (ver SecurityConfig).
 */
@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;

    public UsuarioAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        model.addAttribute("papeis", UsuarioService.PAPEIS_VALIDOS);
        return "admin/usuarios";
    }

    @PostMapping("/{id}/role")
    public String alterarPapel(
            @PathVariable Long id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        try {
            var usuario = usuarioService.alterarPapel(id, role);
            redirectAttributes.addFlashAttribute("toastMensagem",
                    "Papel de " + usuario.getNome() + " atualizado para " + role);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("toastMensagem", "Papel inválido: " + role);
        }

        return "redirect:/admin/usuarios";
    }
}
