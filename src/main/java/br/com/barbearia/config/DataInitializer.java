package br.com.barbearia.config;

import br.com.barbearia.entity.Agendamento;
import br.com.barbearia.entity.Cliente;
import br.com.barbearia.entity.Funcionario;
import br.com.barbearia.entity.Servico;
import br.com.barbearia.entity.Usuario;
import br.com.barbearia.enums.PerfilUsuario;
import br.com.barbearia.enums.SituacaoAgendamento;
import br.com.barbearia.repository.AgendamentoRepository;
import br.com.barbearia.repository.ClienteRepository;
import br.com.barbearia.repository.FuncionarioRepository;
import br.com.barbearia.repository.ServicoRepository;
import br.com.barbearia.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            FuncionarioRepository funcionarioRepository,
            ServicoRepository servicoRepository,
            AgendamentoRepository agendamentoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.servicoRepository = servicoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail("admin@barbearia.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);
        usuarioRepository.save(admin);

        Funcionario carlos = criarFuncionario("Carlos Silva", "45999990001", "carlos@barbearia.com", "Barbeiro", new BigDecimal("40.00"));
        Funcionario marcos = criarFuncionario("Marcos Souza", "45999990002", "marcos@barbearia.com", "Barbeiro", new BigDecimal("40.00"));

        Servico corte = criarServico("Corte Masculino", "Corte clássico ou degradê.", 30, new BigDecimal("45.00"));
        Servico barba = criarServico("Barba", "Barba completa com toalha quente.", 30, new BigDecimal("35.00"));
        Servico combo = criarServico("Corte + Barba", "Corte masculino combinado com barba.", 60, new BigDecimal("70.00"));

        Cliente joao = criarCliente("João Silva", "45999999999", "45999999999", "joao@email.com");
        Cliente pedro = criarCliente("Pedro Souza", "45988888888", "45988888888", "pedro@email.com");
        Cliente lucas = criarCliente("Lucas Lima", "45977777777", "45977777777", "lucas@email.com");

        LocalDate hoje = LocalDate.now();
        criarAgendamento(joao, carlos, corte, hoje, LocalTime.of(9, 0), SituacaoAgendamento.CONFIRMADO);
        criarAgendamento(pedro, marcos, barba, hoje, LocalTime.of(9, 30), SituacaoAgendamento.AGENDADO);
        criarAgendamento(lucas, carlos, combo, hoje, LocalTime.of(10, 0), SituacaoAgendamento.AGENDADO);

        LOGGER.info("Dados iniciais da barbearia criados. Login: admin@barbearia.com");
    }

    private Funcionario criarFuncionario(String nome, String telefone, String email, String cargo, BigDecimal comissao) {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(nome);
        funcionario.setTelefone(telefone);
        funcionario.setEmail(email);
        funcionario.setCargo(cargo);
        funcionario.setDataAdmissao(LocalDate.now().minusYears(1));
        funcionario.setPercentualComissao(comissao);
        funcionario.setAtivo(true);
        return funcionarioRepository.save(funcionario);
    }

    private Servico criarServico(String nome, String descricao, int duracao, BigDecimal valor) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDescricao(descricao);
        servico.setDuracaoMinutos(duracao);
        servico.setValor(valor);
        servico.setAtivo(true);
        return servicoRepository.save(servico);
    }

    private Cliente criarCliente(String nome, String telefone, String whatsapp, String email) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        cliente.setWhatsapp(whatsapp);
        cliente.setEmail(email);
        cliente.setAtivo(true);
        return clienteRepository.save(cliente);
    }

    private void criarAgendamento(
            Cliente cliente,
            Funcionario funcionario,
            Servico servico,
            LocalDate data,
            LocalTime inicio,
            SituacaoAgendamento situacao
    ) {
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setFuncionario(funcionario);
        agendamento.setServico(servico);
        agendamento.setData(data);
        agendamento.setHoraInicio(inicio);
        agendamento.setHoraFim(inicio.plusMinutes(servico.getDuracaoMinutos()));
        agendamento.setValor(servico.getValor());
        agendamento.setSituacao(situacao);
        agendamentoRepository.save(agendamento);
    }
}
