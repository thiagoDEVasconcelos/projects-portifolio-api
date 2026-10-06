package br.com.portifolio.portifolio_projetos_api.repository;

import br.com.portifolio.portifolio_projetos_api.dto.ProjetoFiltro;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProjetoSpecification {

    private ProjetoSpecification() {
    }

    public static Specification<Projeto> comFiltros(ProjetoFiltro filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.nome() != null && !filtro.nome().isBlank()) {
                predicados.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + filtro.nome().trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (filtro.status() != null) {
                predicados.add(criteriaBuilder.equal(root.get("status"), filtro.status()));
            }
            if (filtro.gerenteId() != null) {
                predicados.add(criteriaBuilder.equal(root.get("gerente").get("id"), filtro.gerenteId()));
            }
            if (filtro.orcamentoMin() != null) {
                predicados.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<BigDecimal>get("orcamentoTotal"), filtro.orcamentoMin()));
            }
            if (filtro.orcamentoMax() != null) {
                predicados.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<BigDecimal>get("orcamentoTotal"), filtro.orcamentoMax()));
            }

            return criteriaBuilder.and(predicados.toArray(new Predicate[0]));
        };
    }
}