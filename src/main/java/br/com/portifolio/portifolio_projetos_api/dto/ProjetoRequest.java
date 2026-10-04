package br.com.portifolio.portifolio_projetos_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjetoRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres")
        String nome,

        String descricao,

        @NotNull(message = "A data de início é obrigatória")
        LocalDate dataInicio,

        @NotNull(message = "A previsão de tempo é obrigatória")
        LocalDate previsaoTermino,

        LocalDate dataRealTermino,

        @NotNull(message = "O orçamento total é obrigatório")
        @PositiveOrZero(message = "O orçamento não pode ser negativo")
        BigDecimal orcamentoTotal,

        @NotNull(message = "O gerente é obrigatório")
        Long gerenteId
) {
}
