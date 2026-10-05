package br.com.portifolio.portifolio_projetos_api.mapper;

import br.com.portifolio.portifolio_projetos_api.client.MembroExternoResponse;
import br.com.portifolio.portifolio_projetos_api.dto.MembroResumoResponse;
import br.com.portifolio.portifolio_projetos_api.model.Membro;
import org.springframework.stereotype.Component;

@Component
public class MembroMapper {

    public Membro toEntity(MembroExternoResponse externo) {
        Membro membro = new Membro();
        membro.setIdExterno(externo.id());
        membro.setNome(externo.nome());
        membro.setAtribuicao(externo.atribuicao());
        return membro;
    }

    public MembroResumoResponse toResumo(Membro membro) {
        return new MembroResumoResponse(membro.getId(), membro.getNome(), membro.getAtribuicao());
    }
}