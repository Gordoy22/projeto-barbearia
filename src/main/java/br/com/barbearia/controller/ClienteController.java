package br.com.barbearia.controller;

import br.com.barbearia.dto.ClienteDTO;
import br.com.barbearia.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        model.addAttribute("pagina", clienteService.listar(busca, pageable));
        model.addAttribute("busca", busca);
        return "cliente/index";
    }

    @GetMapping("/novo")
    public String create(Model model) {
        model.addAttribute("cliente", new ClienteDTO());
        return "cliente/create";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("cliente") ClienteDTO cliente,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "cliente/create";
        }
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente cadastrado com sucesso.");
        return "redirect:/clientes";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        return "cliente/details";
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        return "cliente/edit";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("cliente") ClienteDTO cliente,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            cliente.setId(id);
            return "cliente/edit";
        }
        cliente.setId(id);
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente atualizado com sucesso.");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clienteService.alterarStatus(id);
        redirectAttributes.addFlashAttribute("sucesso", "Situação do cliente atualizada.");
        return "redirect:/clientes";
    }
}
