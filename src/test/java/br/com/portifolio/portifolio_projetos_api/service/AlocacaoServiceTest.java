package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.client.MembroClient;
import br.com.portifolio.portifolio_projetos_api.client.MembroExternoResponse;
import br.com.portifolio.portifolio_projetos_api.dto.MembroResumoResponse;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlocacaoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MembroRepository membroRepository;

    @Mock
    private MembroClient membroClient;

    @Spy
    private ProjetoMapper mapper = new ProjetoMapper(new RiscoCalculator());

    @InjectMocks
    private AlocacaoService service;

    @Test
    void alocar_comFuncionario_deveAdicionarMembroNaEquipe() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        stubProjetoEMembro(projeto);
        stubApiExterna("funcionário");
        stubProjetosAtivos(0L);

        ProjetoResponse response = service.alocar(1L, 5L);

        assertThat(projeto.getMembros()).hasSize(1);
        assertThat(response.membros()).extracting(MembroResumoResponse::id).containsExactly(5L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"funcionário", "Funcionario", "FUNCIONÁRIO", "  funcionário  "})
    void alocar_deveAceitarAtribuicaoIndependenteDeAcentoECaixa(String atribuicao) {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        stubProjetoEMembro(projeto);
        stubApiExterna(atribuicao);
        stubProjetosAtivos(0L);

        service.alocar(1L, 5L);

        assertThat(projeto.getMembros()).hasSize(1);
    }

    @Test
    void alocar_comMembroEmDoisProjetosAtivos_devePermitir() {
        Projeto projeto = projetoComStatus(StatusProjeto.PLANEJADO);
        stubProjetoEMembro(projeto);
        stubApiExterna("funcionário");
        stubProjetosAtivos(2L);

        service.alocar(1L, 5L);

        assertThat(projeto.getMembros()).hasSize(1);
    }

    @Test
    void alocar_comNoveMembros_devePermitirODecimo() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        preencherEquipe(projeto, 9);
        stubProjetoEMembro(projeto);
        stubApiExterna("funcionário");
        stubProjetosAtivos(0L);

        service.alocar(1L, 5L);

        assertThat(projeto.getMembros()).hasSize(10);
    }

    @Test
    void alocar_comAtribuicaoDiferenteDeFuncionario_deveLancarRegraNegocio() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        stubProjetoEMembro(projeto);
        stubApiExterna("gerente");

        assertThatThrownBy(() -> service.alocar(1L, 5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("funcionário");
        assertThat(projeto.getMembros()).isEmpty();
    }

    @Test
    void alocar_comMembroDesconhecidoNaApiExterna_deveLancarNaoEncontrado() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        stubProjetoEMembro(projeto);
        when(membroClient.buscarPorId(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alocar(1L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("API externa");
        assertThat(projeto.getMembros()).isEmpty();
    }

    @Test
    void alocar_comMembroJaNaEquipe_deveLancarRegraNegocioSemConsultarApiExterna() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        projeto.getMembros().add(membro(5L, 50L));
        stubProjetoEMembro(projeto);

        assertThatThrownBy(() -> service.alocar(1L, 5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já está alocado");
        verify(membroClient, never()).buscarPorId(any());
    }

    @Test
    void alocar_comEquipeCompleta_deveLancarRegraNegocioSemConsultarApiExterna() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        preencherEquipe(projeto, 10);
        stubProjetoEMembro(projeto);

        assertThatThrownBy(() -> service.alocar(1L, 5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("limite de 10");
        assertThat(projeto.getMembros()).hasSize(10);
        verify(membroClient, never()).buscarPorId(any());
    }

    @Test
    void alocar_comMembroEmTresProjetosAtivos_deveLancarRegraNegocio() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        stubProjetoEMembro(projeto);
        stubApiExterna("funcionário");
        stubProjetosAtivos(3L);

        assertThatThrownBy(() -> service.alocar(1L, 5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("projetos ativos");
        assertThat(projeto.getMembros()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = StatusProjeto.class, names = {"ENCERRADO", "CANCELADO"})
    void alocar_emProjetoFinalizado_deveLancarRegraNegocio(StatusProjeto status) {
        Projeto projeto = projetoComStatus(status);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.alocar(1L, 5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("equipe");
        verify(membroRepository, never()).findById(any());
    }

    @Test
    void alocar_comProjetoInexistente_deveLancarNaoEncontrado() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alocar(99L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Projeto não encontrado");
    }

    @Test
    void alocar_comMembroInexistente_deveLancarNaoEncontrado() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoComStatus(StatusProjeto.EM_ANALISE)));
        when(membroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alocar(1L, 99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Membro não encontrado");
    }

    @Test
    void desalocar_deveRemoverMembroDaEquipe() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        projeto.getMembros().add(membro(5L, 50L));
        projeto.getMembros().add(membro(6L, 60L));
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        ProjetoResponse response = service.desalocar(1L, 5L);

        assertThat(projeto.getMembros()).extracting(Membro::getId).containsExactly(6L);
        assertThat(response.membros()).hasSize(1);
    }

    @Test
    void desalocar_doUltimoMembro_deveLancarRegraNegocio() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        projeto.getMembros().add(membro(5L, 50L));
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.desalocar(1L, 5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ao menos 1 membro");
        assertThat(projeto.getMembros()).hasSize(1);
    }

    @Test
    void desalocar_comMembroForaDaEquipe_deveLancarNaoEncontrado() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        projeto.getMembros().add(membro(6L, 60L));
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.desalocar(1L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("não está alocado");
    }

    @Test
    void desalocar_emProjetoEncerrado_deveLancarRegraNegocio() {
        Projeto projeto = projetoComStatus(StatusProjeto.ENCERRADO);
        projeto.getMembros().add(membro(5L, 50L));
        projeto.getMembros().add(membro(6L, 60L));
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.desalocar(1L, 5L))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(projeto.getMembros()).hasSize(2);
    }

    private void stubProjetoEMembro(Projeto projeto) {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroRepository.findById(5L)).thenReturn(Optional.of(membro(5L, 50L)));
    }

    private void stubApiExterna(String atribuicao) {
        when(membroClient.buscarPorId(50L))
                .thenReturn(Optional.of(new MembroExternoResponse(50L, "Bruno", atribuicao)));
    }

    private void stubProjetosAtivos(long quantidade) {
        when(projetoRepository.contarProjetosAtivosDoMembro(5L, StatusProjeto.finalizados()))
                .thenReturn(quantidade);
    }

    private void preencherEquipe(Projeto projeto, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            projeto.getMembros().add(membro(100L + i, 1000L + i));
        }
    }

    private Membro membro(Long id, Long idExterno) {
        Membro membro = new Membro();
        membro.setId(id);
        membro.setIdExterno(idExterno);
        membro.setNome("Membro " + id);
        membro.setAtribuicao("funcionário");
        return membro;
    }

    private Projeto projetoComStatus(StatusProjeto status) {
        Membro gerente = new Membro();
        gerente.setId(1L);
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