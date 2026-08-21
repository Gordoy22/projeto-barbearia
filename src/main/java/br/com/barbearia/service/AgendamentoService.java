package br.com.barbearia.service;

import br.com.barbearia.dto.AgendamentoDTO;
import br.com.barbearia.entity.Agendamento;
import br.com.barbearia.entity.Cliente;
import br.com.barbearia.entity.Funcionario;
import br.com.barbearia.entity.Servico;
import br.com.barbearia.enums.SituacaoAgendamento;
import br.com.barbearia.exception.NegocioException;
import br.com.barbearia.exception.RecursoNaoEncontradoException;
import br.com.barbearia.repository.AgendamentoRepository;
import br.com.barbearia.repository.AgendamentoSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AgendamentoService {

    private static final Set<SituacaoAgendamento> SITUACOES_IGNORADAS_CONFLITO =
            EnumSet.of(SituacaoAgendamento.CANCELADO, SituacaoAgendamento.NAO_COMPARECEU);

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteService clienteService;
    private final FuncionarioService funcionarioService;
    private final ServicoService servicoService;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            ClienteService clienteService,
            FuncionarioService funcionarioService,
            ServicoService servicoService
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteService = clienteService;
        this.funcionarioService = funcionarioService;
        this.servicoService = servicoService;
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoDTO> filtrar(
            LocalDate inicio,
            LocalDate fim,
            Long funcionarioId,
            Long clienteId,
            SituacaoAgendamento situacao,
            Pageable pageable
    ) {
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("data").descending().and(Sort.by("horaInicio").ascending())
        );
        return agendamentoRepository.findAll(
                AgendamentoSpecifications.comFiltros(inicio, fim, funcionarioId, clienteId, situacao),
                ordenado
        ).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public AgendamentoDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Agendamento buscarEntidade(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado."));
    }

    @Transactional(readOnly = true)
    public long contarPorData(LocalDate data) {
        return agendamentoRepository.countByData(data);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> proximos(int quantidade) {
        return agendamentoRepository
                .findByDataGreaterThanEqualAndSituacaoNotInOrderByDataAscHoraInicioAsc(
                        LocalDate.now(),
                        List.of(SituacaoAgendamento.CANCELADO, SituacaoAgendamento.NAO_COMPARECEU, SituacaoAgendamento.CONCLUIDO),
                        PageRequest.of(0, quantidade)
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return agendamentoRepository.findByDataBetweenOrderByDataAscHoraInicioAsc(inicio, fim)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public AgendamentoDTO salvar(AgendamentoDTO dto) {
        Cliente cliente = clienteService.buscarEntidade(dto.getClienteId());
        Funcionario funcionario = funcionarioService.buscarEntidade(dto.getFuncionarioId());
        Servico servico = servicoService.buscarEntidade(dto.getServicoId());

        if (!cliente.isAtivo()) {
            throw new NegocioException("Não é possível agendar para um cliente inativo.");
        }
        if (!funcionario.isAtivo()) {
            throw new NegocioException("Não é possível agendar para um profissional inativo.");
        }
        if (!servico.isAtivo()) {
            throw new NegocioException("Não é possível agendar um serviço inativo.");
        }

        LocalTime horaInicio = dto.getHoraInicio();
        LocalTime horaFim = calcularHoraFim(horaInicio, servico.getDuracaoMinutos());
        BigDecimal valor = dto.getValor() != null ? dto.getValor() : servico.getValor();

        validarConflito(dto.getId(), funcionario.getId(), dto.getData(), horaInicio, horaFim);

        Agendamento agendamento = dto.getId() != null ? buscarEntidade(dto.getId()) : new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setFuncionario(funcionario);
        agendamento.setServico(servico);
        agendamento.setData(dto.getData());
        agendamento.setHoraInicio(horaInicio);
        agendamento.setHoraFim(horaFim);
        agendamento.setValor(valor);
        agendamento.setObservacao(blankToNull(dto.getObservacao()));
        agendamento.setSituacao(dto.getSituacao() != null ? dto.getSituacao() : SituacaoAgendamento.AGENDADO);

        return toDTO(agendamentoRepository.save(agendamento));
    }

    public void alterarSituacao(Long id, SituacaoAgendamento situacao) {
        Agendamento agendamento = buscarEntidade(id);
        agendamento.setSituacao(situacao);
        agendamentoRepository.save(agendamento);
    }

    private void validarConflito(Long id, Long funcionarioId, LocalDate data, LocalTime inicio, LocalTime fim) {
        boolean conflito = id == null
                ? agendamentoRepository.existeConflitoNovo(funcionarioId, data, inicio, fim, SITUACOES_IGNORADAS_CONFLITO)
                : agendamentoRepository.existeConflitoEdicao(funcionarioId, data, inicio, fim, id, SITUACOES_IGNORADAS_CONFLITO);
        if (conflito) {
            throw new NegocioException("O profissional já possui um agendamento neste horário.");
        }
    }

    public LocalTime calcularHoraFim(LocalTime inicio, Integer duracaoMinutos) {
        return inicio.plusMinutes(duracaoMinutos);
    }

    private AgendamentoDTO toDTO(Agendamento agendamento) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setId(agendamento.getId());
        dto.setClienteId(agendamento.getCliente().getId());
        dto.setClienteNome(agendamento.getCliente().getNome());
        dto.setFuncionarioId(agendamento.getFuncionario().getId());
        dto.setFuncionarioNome(agendamento.getFuncionario().getNome());
        dto.setServicoId(agendamento.getServico().getId());
        dto.setServicoNome(agendamento.getServico().getNome());
        dto.setDuracaoMinutos(agendamento.getServico().getDuracaoMinutos());
        dto.setData(agendamento.getData());
        dto.setHoraInicio(agendamento.getHoraInicio());
        dto.setHoraFim(agendamento.getHoraFim());
        dto.setValor(agendamento.getValor());
        dto.setObservacao(agendamento.getObservacao());
        dto.setSituacao(agendamento.getSituacao());
        dto.setDataCadastro(agendamento.getDataCadastro());
        return dto;
    }

    private String blankToNull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
