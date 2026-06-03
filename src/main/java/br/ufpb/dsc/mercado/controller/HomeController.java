package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller responsável pela página inicial (dashboard).
 *
 * <p>Exibe estatísticas gerais do sistema: total de produtos, categorias,
 * produtos sem estoque e valor total do estoque.
 *
 * @author DSC - UFPB Campus IV
 */
@Controller
@RequestMapping("/")
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Exibe o dashboard principal.
     *
     * @param model modelo Thymeleaf com as estatísticas
     * @return template do dashboard
     */
    @GetMapping
    public String home(Model model) {
        model.addAttribute("totalProdutos", dashboardService.totalProdutos());
        model.addAttribute("totalCategorias", dashboardService.totalCategorias());
        model.addAttribute("produtosSemEstoque", dashboardService.produtosSemEstoque());
        model.addAttribute("valorTotalEstoque", dashboardService.valorTotalEstoque());
        return "home";
    }
}
