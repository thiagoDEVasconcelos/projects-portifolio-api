package br.com.portifolio.portifolio_projetos_api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void recursoNaoEncontrado_deveRetornar404() {
        var resposta = handler.tratarNaoEncontrado(new RecursoNaoEncontradoException("Projeto não encontrado: 1"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Projeto não encontrado: 1");
    }

    @Test
    void regraDeNegocio_deveRetornar422() {
        var resposta = handler.tratarRegraNegocio(new RegraNegocioException("Regra violada"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
    }

    @Test
    void excecaoDoSpringComStatusProprio_deveManterOStatus() {
        var resposta = handler.tratarErroInesperado(new HttpRequestMethodNotSupportedException("PATCH"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(405);
    }

    @Test
    void erroInesperado_deveRetornar500SemExporDetalhes() {
        var resposta = handler.tratarErroInesperado(new RuntimeException("detalhe interno"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(500);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Erro interno do servidor");
    }
}