package br.com.portifolio.portifolio_projetos_api.dto;

import br.com.portifolio.portifolio_projetos_api.model.ClassificacaoRisco;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjetoResponse(
    Long id,
    String nome,
    String descricao,
    LocalDate dataInicio,
    LocalDate previsaoTermino,
    LocalDate dataRealTermino,
    BigDecimal orcamentoTotal,
    StatusProjeto status,
    MembroResumoResponse gerente,
    ClassificacaoRisco risco
) {
}
