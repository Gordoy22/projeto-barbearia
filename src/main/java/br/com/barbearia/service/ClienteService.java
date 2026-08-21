package br.com.barbearia.service;

import br.com.barbearia.dto.ClienteDTO;
import br.com.barbearia.entity.Cliente;
import br.com.barbearia.exception.RecursoNaoEncontradoException;
import br.com.barbearia.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClienteDTO> listar(String busca, Pageable pageable) {
        return clienteRepository.buscar(busca, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarAtivosNaoPaginado() {
        return clienteRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
    }

    @Transactional(readOnly = true)
    public long contarAtivos() {
        return clienteRepository.countByAtivoTrue();
    }

    public ClienteDTO salvar(ClienteDTO dto) {
        Cliente cliente = dto.getId() != null ? buscarEntidade(dto.getId()) : new Cliente();
        copiar(dto, cliente);
        if (dto.getId() == null) {
            cliente.setAtivo(true);
        }
        return toDTO(clienteRepository.save(cliente));
    }

    public void alterarStatus(Long id) {
        Cliente cliente = buscarEntidade(id);
        cliente.setAtivo(!cliente.isAtivo());
        clienteRepository.save(cliente);
    }

    private void copiar(ClienteDTO dto, Cliente cliente) {
        cliente.setNome(dto.getNome().trim());
        cliente.setTelefone(dto.getTelefone().trim());
        cliente.setWhatsapp(blankToNull(dto.getWhatsapp()));
        cliente.setEmail(blankToNull(dto.getEmail()));
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setObservacao(blankToNull(dto.getObservacao()));
    }

    private ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setTelefone(cliente.getTelefone());
        dto.setWhatsapp(cliente.getWhatsapp());
        dto.setEmail(cliente.getEmail());
        dto.setDataNascimento(cliente.getDataNascimento());
        dto.setObservacao(cliente.getObservacao());
        dto.setAtivo(cliente.isAtivo());
        dto.setDataCadastro(cliente.getDataCadastro());
        return dto;
    }

    private String blankToNull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
