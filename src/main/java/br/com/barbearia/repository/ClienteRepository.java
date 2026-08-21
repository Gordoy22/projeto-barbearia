package br.com.barbearia.repository;

import br.com.barbearia.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    long countByAtivoTrue();

    List<Cliente> findByAtivoTrueOrderByNomeAsc();

    @Query("""
            SELECT c FROM Cliente c
            WHERE :busca IS NULL OR :busca = ''
               OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
               OR c.telefone LIKE CONCAT('%', :busca, '%')
               OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :busca, '%'))
            """)
    Page<Cliente> buscar(@Param("busca") String busca, Pageable pageable);
}
