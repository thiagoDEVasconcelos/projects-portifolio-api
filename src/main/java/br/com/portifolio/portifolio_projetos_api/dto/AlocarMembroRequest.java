package br.com.portifolio.portifolio_projetos_api.dto;

import jakarta.validation.constraints.NotNull;

public record AlocarMembroRequest(
        @NotNull(message = "O membro é obrigatório")
        Long membroId
) {
}