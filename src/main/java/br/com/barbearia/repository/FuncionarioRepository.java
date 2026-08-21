package br.com.barbearia.repository;

import br.com.barbearia.entity.Funcionario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    long countByAtivoTrue();

    List<Funcionario> findByAtivoTrueOrderByNomeAsc();

    @Query("""
            SELECT f FROM Funcionario f
            WHERE :busca IS NULL OR :busca = ''
               OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
               OR LOWER(COALESCE(f.cargo, '')) LIKE LOWER(CONCAT('%', :busca, '%'))
               OR COALESCE(f.telefone, '') LIKE CONCAT('%', :busca, '%')
            """)
    Page<Funcionario> buscar(@Param("busca") String busca, Pageable pageable);
}
