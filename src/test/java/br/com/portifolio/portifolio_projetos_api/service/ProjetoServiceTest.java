package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.dto.AlterarStatusRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoResponse;
import br.com.portifolio.portifolio_projetos_api.exception.RecursoNaoEncontradoException;
import br.com.portifolio.portifolio_projetos_api.exception.RegraNegocioException;
import br.com.portifolio.portifolio_projetos_api.mapper.ProjetoMapper;
import br.com.portifolio.portifolio_projetos_api.model.Membro;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import br.com.portifolio.portifolio_projetos_api.repository.MembroRepository;
import br.com.portifolio.portifolio_projetos_api.repository.ProjetoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MembroRepository membroRepository;

    @Mock
    private RiscoCalculator riscoCalculator;

    @Spy
    @InjectMocks
    private ProjetoMapper mapper = new ProjetoMapper(riscoCalculator);

    @InjectMocks
    private ProjetoService service;

    @Test
    void alterarStatusDeveAvancarParaOProximoStatus() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        ProjetoResponse response = service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.ANALISE_REALIZADA));

        assertThat(response.status()).isEqualTo(StatusProjeto.ANALISE_REALIZADA);
    }

    @Test
    void alterarStatusDeveRecusarPularEtapa() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.INICIADO)))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(projeto.getStatus()).isEqualTo(StatusProjeto.EM_ANALISE);
    }

    @Test
    void alterarStatusAoEncerrarDeveRegistrarDataRealDeTermino() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANDAMENTO);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.ENCERRADO));

        assertThat(projeto.getDataRealTermino()).isEqualTo(LocalDate.now());
    }

    @Test
    void alterarStatusComProjetoInexistenteDeveLancarNaoEncontrado() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alterarStatus(99L, new AlterarStatusRequest(StatusProjeto.CANCELADO)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private Projeto projetoComStatus(StatusProjeto status) {
        Membro gerente = new Membro();
        gerente.setNome("Thiago");
        gerente.setAtribuicao("gerente");

        Projeto projeto = new Projeto();
        projeto.setNome("Projeto Teste");
        projeto.setDataInicio(LocalDate.of(2026, 1, 1));
        projeto.setPrevisaoTermino(LocalDate.of(2026, 3, 1));
        projeto.setOrcamentoTotal(new BigDecimal("50000.00"));
        projeto.setStatus(status);
        projeto.setGerente(gerente);
        return projeto;
    }
}