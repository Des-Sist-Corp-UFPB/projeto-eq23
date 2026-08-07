package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String cadastroForm() {
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String username,
                            @RequestParam String senha,
                            @RequestParam String confirmarSenha,
                            Model model) {
        try {
            usuarioService.autocadastro(nome, username, senha, confirmarSenha);
            return "redirect:/login?cadastrado";
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("nome", nome);
            model.addAttribute("username", username);
            return "auth/cadastro";
        }
    }
}

