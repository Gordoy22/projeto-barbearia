package br.com.barbearia.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ClienteService clienteService;
    private final FuncionarioService funcionarioService;
    private final ServicoService servicoService;
    private final AgendamentoService agendamentoService;

    public DashboardService(
            ClienteService clienteService,
            FuncionarioService funcionarioService,
            ServicoService servicoService,
            AgendamentoService agendamentoService
    ) {
        this.clienteService = clienteService;
        this.funcionarioService = funcionarioService;
        this.servicoService = servicoService;
        this.agendamentoService = agendamentoService;
    }

    public Map<String, Long> resumo() {
        Map<String, Long> cards = new LinkedHashMap<>();
        cards.put("agendamentosHoje", agendamentoService.contarPorData(LocalDate.now()));
        cards.put("clientes", clienteService.contarAtivos());
        cards.put("funcionarios", funcionarioService.contarAtivos());
        cards.put("servicos", servicoService.contarAtivos());
        return cards;
    }
}
