package br.com.portifolio.portifolio_projetos_api.dto;

import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusProjeto status
) {
}
