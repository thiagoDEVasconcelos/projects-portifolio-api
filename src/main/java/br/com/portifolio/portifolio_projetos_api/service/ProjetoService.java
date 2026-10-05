package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.dto.AlterarStatusRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoResponse;
import br.com.portifolio.portifolio_projetos_api.exception.RecursoNaoEncontradoException;
import br.com.portifolio.portifolio_projetos_api.exception.RegraNegocioException;
import br.com.portifolio.portifolio_projetos_api.mapper.ProjetoMapper;
import br.com.portifolio.portifolio_projetos_api.model.Membro;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import br.com.portifolio.portifolio_projetos_api.repository.MembroRepository;
import br.com.portifolio.portifolio_projetos_api.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final MembroRepository membroRepository;
    private final ProjetoMapper mapper;

    @Transactional
    public ProjetoResponse criar(ProjetoRequest request) {
        validarDatas(request);
        Membro gerente = buscarGerente(request.gerenteId());
        Projeto projeto = mapper.toEntity(request, gerente);
        return mapper.toResponse(projetoRepository.save(projeto));
    }

    @Transactional()
    public ProjetoResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ProjetoResponse> listar(Pageable pageable) {
        return projetoRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public ProjetoResponse atualizar(Long id, ProjetoRequest request) {
        validarDatas(request);
        Projeto projeto = buscarEntidade(id);
        Membro gerente = buscarGerente(request.gerenteId());
        mapper.atualizarEntidade(projeto, request, gerente);
        return mapper.toResponse(projeto);
    }

    @Transactional
    public void excluir(Long id) {
        Projeto projeto = buscarEntidade(id);
        if (!projeto.getStatus().permiteExclusao()) {
            throw new RegraNegocioException("Projeto com status " + projeto.getStatus() + " não pode ser excluído");
        }
        projetoRepository.delete(projeto);
    }

    private Projeto buscarEntidade(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado: " + id));
    }

    private Membro buscarGerente(Long gerenteId) {
        return membroRepository.findById(gerenteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gerente não encontrado: " + gerenteId));
    }

    private void validarDatas(ProjetoRequest request) {
        if (request.previsaoTermino().isBefore(request.dataInicio())) {
            throw new RegraNegocioException("A previsão de término não pode ser anterior à data de início");
        }
        if (request.dataRealTermino() != null && request.dataRealTermino().isBefore(request.dataInicio())) {
            throw new RegraNegocioException("A data real de término não pode ser anterior à data de início");
        }
    }

    @Transactional
    public ProjetoResponse alterarStatus(Long id, AlterarStatusRequest request) {
        Projeto projeto = buscarEntidade(id);
        StatusProjeto atual = projeto.getStatus();
        StatusProjeto destino = request.status();

        if (!atual.podeTransicionarPara(destino)) {
        throw new RegraNegocioException("Transição de status inválida");
        }

        projeto.setStatus(destino);
        if (destino == StatusProjeto.ENCERRADO && projeto.getDataRealTermino() == null) {
            projeto.setDataRealTermino(LocalDate.now());
        }
        return mapper.toResponse(projeto);
    }
}
