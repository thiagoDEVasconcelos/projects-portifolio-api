package br.com.portifolio.portifolio_projetos_api.client;

import java.util.Optional;

public interface MembroClient {

    MembroExternoResponse criar(MembroExternoRequest request);

    Optional<MembroExternoResponse> buscarPorId(Long idExterno);
}