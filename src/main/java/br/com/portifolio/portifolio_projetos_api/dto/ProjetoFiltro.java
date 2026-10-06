package br.com.portifolio.portifolio_projetos_api.dto;

import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;

import java.math.BigDecimal;

public record ProjetoFiltro(
    String nome,
    StatusProjeto status,
    Long gerenteId,
    BigDecimal orcamentoMin,
    BigDecimal orcamentoMax
) {
}