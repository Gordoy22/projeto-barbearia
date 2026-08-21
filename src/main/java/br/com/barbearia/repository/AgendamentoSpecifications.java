package br.com.barbearia.repository;

import br.com.barbearia.entity.Agendamento;
import br.com.barbearia.enums.SituacaoAgendamento;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class AgendamentoSpecifications {

    private AgendamentoSpecifications() {
    }

    public static Specification<Agendamento> comFiltros(
            LocalDate inicio,
            LocalDate fim,
            Long funcionarioId,
            Long clienteId,
            SituacaoAgendamento situacao
    ) {
        return (root, query, builder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("cliente");
                root.fetch("funcionario");
                root.fetch("servico");
                query.distinct(true);
            }

            var predicates = builder.conjunction();
            if (inicio != null) {
                predicates = builder.and(predicates, builder.greaterThanOrEqualTo(root.get("data"), inicio));
            }
            if (fim != null) {
                predicates = builder.and(predicates, builder.lessThanOrEqualTo(root.get("data"), fim));
            }
            if (funcionarioId != null) {
                predicates = builder.and(predicates, builder.equal(root.get("funcionario").get("id"), funcionarioId));
            }
            if (clienteId != null) {
                predicates = builder.and(predicates, builder.equal(root.get("cliente").get("id"), clienteId));
            }
            if (situacao != null) {
                predicates = builder.and(predicates, builder.equal(root.get("situacao"), situacao));
            }
            return predicates;
        };
    }
}
