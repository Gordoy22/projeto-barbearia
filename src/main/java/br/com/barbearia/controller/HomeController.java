package br.com.barbearia.controller;

import br.com.barbearia.service.AgendamentoService;
import br.com.barbearia.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DashboardService dashboardService;
    private final AgendamentoService agendamentoService;

    public HomeController(DashboardService dashboardService, AgendamentoService agendamentoService) {
        this.dashboardService = dashboardService;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("resumo", dashboardService.resumo());
        model.addAttribute("proximos", agendamentoService.proximos(8));
        return "home/index";
    }
}
