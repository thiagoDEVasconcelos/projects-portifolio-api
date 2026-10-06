package br.com.portifolio.portifolio_projetos_api.mapper;

import br.com.portifolio.portifolio_projetos_api.dto.MembroResumoResponse;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoResponse;
import br.com.portifolio.portifolio_projetos_api.model.Membro;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import br.com.portifolio.portifolio_projetos_api.service.RiscoCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjetoMapper {

    private final RiscoCalculator riscoCalculator;

    public Projeto toEntity(ProjetoRequest request, Membro gerente) {
        Projeto projeto = new Projeto();
        projeto.setStatus(StatusProjeto.EM_ANALISE);
        atualizarCampos(projeto, request, gerente);
        return projeto;
    }

    public void atualizarEntidade(Projeto projeto, ProjetoRequest request, Membro gerente) {
        atualizarCampos(projeto, request, gerente);
    }

    public ProjetoResponse toResponse(Projeto projeto) {
        List<MembroResumoResponse> membros = projeto.getMembros().stream()
                .map(this::toMembroResumo)
                .toList();

        return new ProjetoResponse(
                projeto.getId(),
                projeto.getNome(),
                projeto.getDescricao(),
                projeto.getDataInicio(),
                projeto.getPrevisaoTermino(),
                projeto.getDataRealTermino(),
                projeto.getOrcamentoTotal(),
                projeto.getStatus(),
                toMembroResumo(projeto.getGerente()),
                riscoCalculator.calcular(projeto),
                membros
        );
    }

    public MembroResumoResponse toMembroResumo(Membro membro) {
        return new MembroResumoResponse(
                membro.getId(),
                membro.getNome(),
                membro.getAtribuicao()
        );
    }

    private void atualizarCampos(Projeto projeto, ProjetoRequest request, Membro gerente) {
        projeto.setNome(request.nome());
        projeto.setDescricao(request.descricao());
        projeto.setDataInicio(request.dataInicio());
        projeto.setPrevisaoTermino(request.previsaoTermino());
        projeto.setDataRealTermino(request.dataRealTermino());
        projeto.setOrcamentoTotal(request.orcamentoTotal());
        projeto.setGerente(gerente);
    }
}