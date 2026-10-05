package br.com.portifolio.portifolio_projetos_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MembroRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "A atribuição é obrigatória")
        @Size(max = 50, message = "A atribuição deve ter no máximo 50 caracteres")
        String atribuicao
) {
}