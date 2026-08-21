package br.com.barbearia.controller;

import br.com.barbearia.dto.FuncionarioDTO;
import br.com.barbearia.service.FuncionarioService;
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
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        model.addAttribute("pagina", funcionarioService.listar(busca, pageable));
        model.addAttribute("busca", busca);
        return "funcionario/index";
    }

    @GetMapping("/novo")
    public String create(Model model) {
        model.addAttribute("funcionario", new FuncionarioDTO());
        return "funcionario/create";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("funcionario") FuncionarioDTO funcionario,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "funcionario/create";
        }
        funcionarioService.salvar(funcionario);
        redirectAttributes.addFlashAttribute("sucesso", "Funcionário cadastrado com sucesso.");
        return "redirect:/funcionarios";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("funcionario", funcionarioService.buscarPorId(id));
        return "funcionario/details";
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("funcionario", funcionarioService.buscarPorId(id));
        return "funcionario/edit";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("funcionario") FuncionarioDTO funcionario,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            funcionario.setId(id);
            return "funcionario/edit";
        }
        funcionario.setId(id);
        funcionarioService.salvar(funcionario);
        redirectAttributes.addFlashAttribute("sucesso", "Funcionário atualizado com sucesso.");
        return "redirect:/funcionarios";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        funcionarioService.alterarStatus(id);
        redirectAttributes.addFlashAttribute("sucesso", "Situação do funcionário atualizada.");
        return "redirect:/funcionarios";
    }
}
