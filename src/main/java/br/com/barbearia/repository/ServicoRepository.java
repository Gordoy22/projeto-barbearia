package br.com.barbearia.repository;

import br.com.barbearia.entity.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    long countByAtivoTrue();

    List<Servico> findByAtivoTrueOrderByNomeAsc();

    @Query("""
            SELECT s FROM Servico s
            WHERE :busca IS NULL OR :busca = ''
               OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
            """)
    Page<Servico> buscar(@Param("busca") String busca, Pageable pageable);
}
