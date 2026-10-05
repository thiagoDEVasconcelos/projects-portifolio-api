package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.client.MembroClient;
import br.com.portifolio.portifolio_projetos_api.client.MembroExternoRequest;
import br.com.portifolio.portifolio_projetos_api.client.MembroExternoResponse;
import br.com.portifolio.portifolio_projetos_api.dto.MembroRequest;
import br.com.portifolio.portifolio_projetos_api.dto.MembroResumoResponse;
import br.com.portifolio.portifolio_projetos_api.exception.RecursoNaoEncontradoException;
import br.com.portifolio.portifolio_projetos_api.mapper.MembroMapper;
import br.com.portifolio.portifolio_projetos_api.model.Membro;
import br.com.portifolio.portifolio_projetos_api.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembroService {

    private final MembroClient membroClient;
    private final MembroRepository membroRepository;
    private final MembroMapper mapper;

    // Sem @Transactional de propósito: não faz sentido segurar uma conexão do banco durante a chamada HTTP.
    public MembroResumoResponse criar(MembroRequest request) {
        MembroExternoResponse externo = membroClient.criar(
                new MembroExternoRequest(request.nome(), request.atribuicao()));
        Membro copiaLocal = mapper.toEntity(externo);
        return mapper.toResumo(membroRepository.save(copiaLocal));
    }

    @Transactional(readOnly = true)
    public MembroResumoResponse buscarPorId(Long id) {
        return mapper.toResumo(membroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<MembroResumoResponse> listar(Pageable pageable) {
        return membroRepository.findAll(pageable).map(mapper::toResumo);
    }
}