package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.client.MembroClient;
import br.com.portifolio.portifolio_projetos_api.client.MembroExternoResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AlocacaoService {

    private static final int MAX_MEMBROS_POR_PROJETO = 10;
    private static final int MAX_PROJETOS_ATIVOS_POR_MEMBRO = 3;
    private static final String ATRIBUICAO_ALOCAVEL = "funcionario";

    private final ProjetoRepository projetoRepository;
    private final MembroRepository membroRepository;
    private final MembroClient membroClient;
    private final ProjetoMapper mapper;

    @Transactional
    public ProjetoResponse alocar(Long projetoId, Long membroId) {
        Projeto projeto = buscarProjeto(projetoId);
        exigirProjetoAtivo(projeto);
        Membro membro = buscarMembro(membroId);

        if (estaNaEquipe(projeto, membroId)) {
            throw new RegraNegocioException("O membro já está alocado neste projeto");
        }
        if (projeto.getMembros().size() >= MAX_MEMBROS_POR_PROJETO) {
            throw new RegraNegocioException(
                    "O projeto já atingiu o limite de " + MAX_MEMBROS_POR_PROJETO + " membros");
        }
        validarAtribuicao(membro);
        validarLimiteDeProjetosAtivos(membro);

        projeto.getMembros().add(membro);
        return mapper.toResponse(projeto);
    }

    @Transactional
    public ProjetoResponse desalocar(Long projetoId, Long membroId) {
        Projeto projeto = buscarProjeto(projetoId);
        exigirProjetoAtivo(projeto);

        if (!estaNaEquipe(projeto, membroId)) {
            throw new RecursoNaoEncontradoException(
                    "O membro " + membroId + " não está alocado no projeto " + projetoId);
        }
        if (projeto.getMembros().size() <= 1) {
            throw new RegraNegocioException("O projeto deve manter ao menos 1 membro alocado");
        }

        projeto.getMembros().removeIf(membro -> membroId.equals(membro.getId()));
        return mapper.toResponse(projeto);
    }

    private void validarAtribuicao(Membro membro) {
        MembroExternoResponse externo = membroClient.buscarPorId(membro.getIdExterno())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Membro não encontrado na API externa: " + membro.getIdExterno()));

        if (!ATRIBUICAO_ALOCAVEL.equals(normalizar(externo.atribuicao()))) {
            throw new RegraNegocioException("Apenas membros com a atribuição \"funcionário\" podem ser alocados");
        }
    }

    private void validarLimiteDeProjetosAtivos(Membro membro) {
        long ativos = projetoRepository.contarProjetosAtivosDoMembro(membro.getId(), StatusProjeto.finalizados());
        if (ativos >= MAX_PROJETOS_ATIVOS_POR_MEMBRO) {
            throw new RegraNegocioException("O membro já está alocado em " + ativos
                    + " projetos ativos (limite: " + MAX_PROJETOS_ATIVOS_POR_MEMBRO + ")");
        }
    }

    private void exigirProjetoAtivo(Projeto projeto) {
        if (!projeto.getStatus().isAtivo()) {
            throw new RegraNegocioException(
                    "Não é possível alterar a equipe de um projeto " + projeto.getStatus());
        }
    }

    private boolean estaNaEquipe(Projeto projeto, Long membroId) {
        return projeto.getMembros().stream().anyMatch(membro -> membroId.equals(membro.getId()));
    }

    private Projeto buscarProjeto(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado: " + id));
    }

    private Membro buscarMembro(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado: " + id));
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}