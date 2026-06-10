package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.service.DashboardService;
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
    public String home(Model model) {
        model.addAttribute("totalProdutos", dashboardService.totalAtivos());
        model.addAttribute("totalCategorias", dashboardService.totalCategorias());
        model.addAttribute("totalChamadosAbertos", dashboardService.totalChamadosAbertos());
        model.addAttribute("totalChamadosEmAtendimento", dashboardService.totalChamadosEmAtendimento());
        return "home";
    }
}
