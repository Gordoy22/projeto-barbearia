package br.com.barbearia.controller;

import br.com.barbearia.dto.ServicoDTO;
import br.com.barbearia.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        model.addAttribute("pagina", servicoService.listar(busca, pageable));
        model.addAttribute("busca", busca);
        return "servico/index";
    }

    @GetMapping("/novo")
    public String create(Model model) {
        model.addAttribute("servico", new ServicoDTO());
        return "servico/create";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("servico") ServicoDTO servico,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "servico/create";
        }
        servicoService.salvar(servico);
        redirectAttributes.addFlashAttribute("sucesso", "Serviço cadastrado com sucesso.");
        return "redirect:/servicos";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("servico", servicoService.buscarPorId(id));
        return "servico/details";
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("servico", servicoService.buscarPorId(id));
        return "servico/edit";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("servico") ServicoDTO servico,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            servico.setId(id);
            return "servico/edit";
        }
        servico.setId(id);
        servicoService.salvar(servico);
        redirectAttributes.addFlashAttribute("sucesso", "Serviço atualizado com sucesso.");
        return "redirect:/servicos";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        servicoService.alterarStatus(id);
        redirectAttributes.addFlashAttribute("sucesso", "Situação do serviço atualizada.");
        return "redirect:/servicos";
    }

    @GetMapping("/{id}/json")
    @ResponseBody
    public ResponseEntity<ServicoDTO> json(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }
}
