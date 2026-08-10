package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.security.CustomOidcUser;
import br.ufpb.dsc.mercado.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String home(@AuthenticationPrincipal Object principal, Model model) {
        Usuario usuarioLogado = getUsuarioLogado(principal);
        boolean isAdminOuTecnico = usuarioLogado != null && usuarioLogado.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_TECNICO".equals(a.getAuthority()));

        if (isAdminOuTecnico) {
            model.addAttribute("isAdminOuTecnico", true);
            model.addAttribute("totalProdutos", dashboardService.totalAtivos());
            model.addAttribute("totalChamadosAbertos", dashboardService.totalChamadosAbertos());
            model.addAttribute("totalChamadosEmAtendimento", dashboardService.totalChamadosEmAtendimento());
        } else {
            Long clienteId = usuarioLogado != null ? usuarioLogado.getId() : null;
            model.addAttribute("isAdminOuTecnico", false);
            model.addAttribute("meusChamadosAbertos", dashboardService.totalChamadosAbertosPorCliente(clienteId));
            model.addAttribute("meusChamadosEmAtendimento", dashboardService.totalChamadosEmAtendimentoPorCliente(clienteId));
            model.addAttribute("meusChamadosTotal", dashboardService.totalChamadosPorCliente(clienteId));
        }

        return "home";
    }

    private Usuario getUsuarioLogado(Object principal) {
        if (principal instanceof CustomOidcUser) {
            return ((CustomOidcUser) principal).getUsuario();
        }
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        return null;
    }
}

