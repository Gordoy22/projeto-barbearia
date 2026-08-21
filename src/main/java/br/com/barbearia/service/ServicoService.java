package br.com.barbearia.service;

import br.com.barbearia.dto.ServicoDTO;
import br.com.barbearia.entity.Servico;
import br.com.barbearia.exception.RecursoNaoEncontradoException;
import br.com.barbearia.repository.ServicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional(readOnly = true)
    public Page<ServicoDTO> listar(String busca, Pageable pageable) {
        return servicoRepository.buscar(busca, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ServicoDTO> listarAtivos() {
        return servicoRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicoDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Servico buscarEntidade(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado."));
    }

    @Transactional(readOnly = true)
    public long contarAtivos() {
        return servicoRepository.countByAtivoTrue();
    }

    public ServicoDTO salvar(ServicoDTO dto) {
        Servico servico = dto.getId() != null ? buscarEntidade(dto.getId()) : new Servico();
        copiar(dto, servico);
        if (dto.getId() == null) {
            servico.setAtivo(true);
        }
        return toDTO(servicoRepository.save(servico));
    }

    public void alterarStatus(Long id) {
        Servico servico = buscarEntidade(id);
        servico.setAtivo(!servico.isAtivo());
        servicoRepository.save(servico);
    }

    private void copiar(ServicoDTO dto, Servico servico) {
        servico.setNome(dto.getNome().trim());
        servico.setDescricao(blankToNull(dto.getDescricao()));
        servico.setDuracaoMinutos(dto.getDuracaoMinutos());
        servico.setValor(dto.getValor());
    }

    private ServicoDTO toDTO(Servico servico) {
        ServicoDTO dto = new ServicoDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setDuracaoMinutos(servico.getDuracaoMinutos());
        dto.setValor(servico.getValor());
        dto.setAtivo(servico.isAtivo());
        return dto;
    }

    private String blankToNull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
