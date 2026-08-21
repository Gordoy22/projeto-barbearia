package br.com.barbearia.controller;

import br.com.barbearia.dto.AgendamentoDTO;
import br.com.barbearia.enums.SituacaoAgendamento;
import br.com.barbearia.exception.NegocioException;
import br.com.barbearia.service.AgendamentoService;
import br.com.barbearia.service.ClienteService;
import br.com.barbearia.service.FuncionarioService;
import br.com.barbearia.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final FuncionarioService funcionarioService;
    private final ServicoService servicoService;

    public AgendamentoController(
            AgendamentoService agendamentoService,
            ClienteService clienteService,
            FuncionarioService funcionarioService,
            ServicoService servicoService
    ) {
        this.agendamentoService = agendamentoService;
        this.clienteService = clienteService;
        this.funcionarioService = funcionarioService;
        this.servicoService = servicoService;
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long funcionarioId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) SituacaoAgendamento situacao,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        if (inicio == null && fim == null) {
            inicio = LocalDate.now();
            fim = LocalDate.now();
        }
        model.addAttribute("pagina", agendamentoService.filtrar(inicio, fim, funcionarioId, clienteId, situacao, pageable));
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        model.addAttribute("funcionarioId", funcionarioId);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("situacao", situacao);
        popularFiltros(model);
        return "agendamento/index";
    }

    @GetMapping("/novo")
    public String create(Model model) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setData(LocalDate.now());
        dto.setSituacao(SituacaoAgendamento.AGENDADO);
        model.addAttribute("agendamento", dto);
        popularFormulario(model);
        return "agendamento/create";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("agendamento") AgendamentoDTO agendamento,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            popularFormulario(model);
            return "agendamento/create";
        }
        try {
            agendamentoService.salvar(agendamento);
        } catch (NegocioException exception) {
            result.reject("conflito", exception.getMessage());
            popularFormulario(model);
            return "agendamento/create";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Agendamento realizado com sucesso.");
        return "redirect:/agendamentos";
    }

    @GetMapping("/calendario")
    public String calendario() {
        return "agendamento/calendario";
    }

    @GetMapping("/eventos")
    @ResponseBody
    public List<Map<String, Object>> eventos(@RequestParam String start, @RequestParam String end) {
        LocalDate inicio = parseIsoDate(start);
        LocalDate fim = parseIsoDate(end);
        if (fim.isAfter(inicio)) {
            fim = fim.minusDays(1);
        }
        DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm");
        return agendamentoService.listarPorPeriodo(inicio, fim).stream()
                .map(item -> {
                    Map<String, Object> evento = new HashMap<>();
                    evento.put("id", item.getId());
                    evento.put("title", item.getClienteNome() + " · " + item.getServicoNome());
                    evento.put("start", item.getData().atTime(item.getHoraInicio()).toString());
                    evento.put("end", item.getData().atTime(item.getHoraFim()).toString());
                    evento.put("url", "/agendamentos/" + item.getId());
                    evento.put("extendedProps", Map.of(
                            "horario", item.getHoraInicio().format(hora) + " - " + item.getHoraFim().format(hora),
                            "cliente", item.getClienteNome(),
                            "servico", item.getServicoNome(),
                            "profissional", item.getFuncionarioNome(),
                            "situacao", item.getSituacao().getDescricao()
                    ));
                    return evento;
                })
                .toList();
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("agendamento", agendamentoService.buscarPorId(id));
        model.addAttribute("situacoes", SituacaoAgendamento.values());
        return "agendamento/details";
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("agendamento", agendamentoService.buscarPorId(id));
        popularFormulario(model);
        return "agendamento/edit";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("agendamento") AgendamentoDTO agendamento,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        agendamento.setId(id);
        if (result.hasErrors()) {
            popularFormulario(model);
            return "agendamento/edit";
        }
        try {
            agendamentoService.salvar(agendamento);
        } catch (NegocioException exception) {
            result.reject("conflito", exception.getMessage());
            popularFormulario(model);
            return "agendamento/edit";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Agendamento atualizado com sucesso.");
        return "redirect:/agendamentos";
    }

    @PostMapping("/{id}/situacao")
    public String alterarSituacao(
            @PathVariable Long id,
            @RequestParam SituacaoAgendamento situacao,
            RedirectAttributes redirectAttributes
    ) {
        agendamentoService.alterarSituacao(id, situacao);
        redirectAttributes.addFlashAttribute("sucesso", "Situação do agendamento atualizada.");
        return "redirect:/agendamentos/" + id;
    }

    @GetMapping("/{id}/json")
    @ResponseBody
    public ResponseEntity<AgendamentoDTO> json(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    private void popularFormulario(Model model) {
        model.addAttribute("clientes", clienteService.listarAtivosNaoPaginado());
        model.addAttribute("funcionarios", funcionarioService.listarAtivos());
        model.addAttribute("servicos", servicoService.listarAtivos());
        model.addAttribute("situacoes", SituacaoAgendamento.values());
    }

    private void popularFiltros(Model model) {
        model.addAttribute("funcionarios", funcionarioService.listarAtivos());
        model.addAttribute("clientes", clienteService.listarAtivosNaoPaginado());
        model.addAttribute("situacoes", SituacaoAgendamento.values());
    }

    private LocalDate parseIsoDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }
        if (value.length() <= 10) {
            return LocalDate.parse(value);
        }
        return OffsetDateTime.parse(value).toLocalDate();
    }
}
