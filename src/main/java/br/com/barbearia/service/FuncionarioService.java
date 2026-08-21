package br.com.barbearia.service;

import br.com.barbearia.dto.FuncionarioDTO;
import br.com.barbearia.entity.Funcionario;
import br.com.barbearia.exception.RecursoNaoEncontradoException;
import br.com.barbearia.repository.FuncionarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<FuncionarioDTO> listar(String busca, Pageable pageable) {
        return funcionarioRepository.buscar(busca, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<FuncionarioDTO> listarAtivos() {
        return funcionarioRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public FuncionarioDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Funcionario buscarEntidade(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado."));
    }

    @Transactional(readOnly = true)
    public long contarAtivos() {
        return funcionarioRepository.countByAtivoTrue();
    }

    public FuncionarioDTO salvar(FuncionarioDTO dto) {
        Funcionario funcionario = dto.getId() != null ? buscarEntidade(dto.getId()) : new Funcionario();
        copiar(dto, funcionario);
        if (dto.getId() == null) {
            funcionario.setAtivo(true);
        }
        return toDTO(funcionarioRepository.save(funcionario));
    }

    public void alterarStatus(Long id) {
        Funcionario funcionario = buscarEntidade(id);
        funcionario.setAtivo(!funcionario.isAtivo());
        funcionarioRepository.save(funcionario);
    }

    private void copiar(FuncionarioDTO dto, Funcionario funcionario) {
        funcionario.setNome(dto.getNome().trim());
        funcionario.setTelefone(blankToNull(dto.getTelefone()));
        funcionario.setEmail(blankToNull(dto.getEmail()));
        funcionario.setCargo(dto.getCargo().trim());
        funcionario.setDataAdmissao(dto.getDataAdmissao());
        funcionario.setPercentualComissao(dto.getPercentualComissao());
    }

    private FuncionarioDTO toDTO(Funcionario funcionario) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(funcionario.getId());
        dto.setNome(funcionario.getNome());
        dto.setTelefone(funcionario.getTelefone());
        dto.setEmail(funcionario.getEmail());
        dto.setCargo(funcionario.getCargo());
        dto.setDataAdmissao(funcionario.getDataAdmissao());
        dto.setPercentualComissao(funcionario.getPercentualComissao());
        dto.setAtivo(funcionario.isAtivo());
        return dto;
    }

    private String blankToNull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
