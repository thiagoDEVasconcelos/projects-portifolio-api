package br.com.portifolio.portifolio_projetos_api.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MembroRestClient implements MembroClient {

    private final RestClient restClient;

    @Override
    public MembroExternoResponse criar(MembroExternoRequest request) {
        return restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MembroExternoResponse.class);
    }

    @Override
    public Optional<MembroExternoResponse> buscarPorId(Long idExterno) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/{id}", idExterno)
                    .retrieve()
                    .body(MembroExternoResponse.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
