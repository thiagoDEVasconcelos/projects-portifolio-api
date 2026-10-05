package br.com.portifolio.portifolio_projetos_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        int status,
        String mensagem,
        LocalDateTime timestamp,
        Map<String, String> erros
) {
    public ErroResponse(int status, String mensagem) {
        this(status, mensagem, LocalDateTime.now(), null);
    }

    public ErroResponse(int status, String mensagem, Map<String, String> erros) {
        this(status, mensagem, LocalDateTime.now(), erros);
    }
}
