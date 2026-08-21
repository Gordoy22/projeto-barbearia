package br.com.barbearia.repository;

import br.com.barbearia.entity.Agendamento;
import br.com.barbearia.enums.SituacaoAgendamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento> {

    long countByData(LocalDate data);

    @Override
    @EntityGraph(attributePaths = {"cliente", "funcionario", "servico"})
    Optional<Agendamento> findById(Long id);

    @EntityGraph(attributePaths = {"cliente", "funcionario", "servico"})
    List<Agendamento> findByDataGreaterThanEqualAndSituacaoNotInOrderByDataAscHoraInicioAsc(
            LocalDate data,
            Collection<SituacaoAgendamento> situacoesExcluidas,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(a) > 0 FROM Agendamento a
            WHERE a.funcionario.id = :funcionarioId
              AND a.data = :data
              AND a.situacao NOT IN :situacoesIgnoradas
              AND a.horaInicio < :horaFim
              AND a.horaFim > :horaInicio
            """)
    boolean existeConflitoNovo(
            @Param("funcionarioId") Long funcionarioId,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim,
            @Param("situacoesIgnoradas") Collection<SituacaoAgendamento> situacoesIgnoradas
    );

    @Query("""
            SELECT COUNT(a) > 0 FROM Agendamento a
            WHERE a.funcionario.id = :funcionarioId
              AND a.data = :data
              AND a.situacao NOT IN :situacoesIgnoradas
              AND a.horaInicio < :horaFim
              AND a.horaFim > :horaInicio
              AND a.id <> :id
            """)
    boolean existeConflitoEdicao(
            @Param("funcionarioId") Long funcionarioId,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim,
            @Param("id") Long id,
            @Param("situacoesIgnoradas") Collection<SituacaoAgendamento> situacoesIgnoradas
    );

    @EntityGraph(attributePaths = {"cliente", "funcionario", "servico"})
    List<Agendamento> findByDataBetweenOrderByDataAscHoraInicioAsc(LocalDate inicio, LocalDate fim);
}
