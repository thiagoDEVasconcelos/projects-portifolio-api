package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.dto.AlterarStatusRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoFiltro;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoResponse;
import br.com.portifolio.portifolio_projetos_api.exception.RecursoNaoEncontradoException;
import br.com.portifolio.portifolio_projetos_api.exception.RegraNegocioException;
import br.com.portifolio.portifolio_projetos_api.mapper.ProjetoMapper;
import br.com.portifolio.portifolio_projetos_api.model.ClassificacaoRisco;
import br.com.portifolio.portifolio_projetos_api.model.Membro;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import br.com.portifolio.portifolio_projetos_api.repository.MembroRepository;
import br.com.portifolio.portifolio_projetos_api.repository.ProjetoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MembroRepository membroRepository;

    @Spy
    private ProjetoMapper mapper = new ProjetoMapper(new RiscoCalculator());

    @InjectMocks
    private ProjetoService service;

    @Test
    void criar_deveSalvarProjetoComStatusEmAnaliseEGerenteInformado() {
        Membro gerente = gerente();
        when(membroRepository.findById(1L)).thenReturn(Optional.of(gerente));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        ProjetoResponse response = service.criar(
                request(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), null, 1L));

        ArgumentCaptor<Projeto> captor = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository).save(captor.capture());
        assertThat(captor.getValue().getGerente()).isSameAs(gerente);
        assertThat(response.status()).isEqualTo(StatusProjeto.EM_ANALISE);
        assertThat(response.gerente().nome()).isEqualTo("Thiago");
        assertThat(response.risco()).isEqualTo(ClassificacaoRisco.BAIXO);
    }

    @Test
    void criar_comPrevisaoAnteriorAoInicio_deveLancarRegraNegocio() {
        ProjetoRequest request = request(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 1, 1), null, 1L);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("previsão de término");
        verify(projetoRepository, never()).save(any());
    }

    @Test
    void criar_comDataRealAnteriorAoInicio_deveLancarRegraNegocio() {
        ProjetoRequest request = request(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 2, 1), 1L);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("data real de término");
        verify(projetoRepository, never()).save(any());
    }

    @Test
    void criar_comGerenteInexistente_deveLancarNaoEncontrado() {
        when(membroRepository.findById(99L)).thenReturn(Optional.empty());
        ProjetoRequest request = request(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), null, 99L);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Gerente não encontrado");
        verify(projetoRepository, never()).save(any());
    }

    @Test
    void buscarPorId_comProjetoExistente_deveRetornarResponse() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoComStatus(StatusProjeto.EM_ANALISE)));

        ProjetoResponse response = service.buscarPorId(1L);

        assertThat(response.nome()).isEqualTo("Projeto Teste");
        assertThat(response.status()).isEqualTo(StatusProjeto.EM_ANALISE);
    }

    @Test
    void buscarPorId_comProjetoInexistente_deveLancarNaoEncontrado() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Projeto não encontrado");
    }

    @Test
    void listar_deveConverterAPaginaParaResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Projeto> pagina = new PageImpl<>(List.of(projetoComStatus(StatusProjeto.PLANEJADO)), pageable, 1);
        when(projetoRepository.findAll(ArgumentMatchers.<Specification<Projeto>>any(), eq(pageable)))
                .thenReturn(pagina);

        Page<ProjetoResponse> resultado = service.listar(new ProjetoFiltro(null, null, null, null, null), pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).status()).isEqualTo(StatusProjeto.PLANEJADO);
    }

    @Test
    void listar_comOrcamentoMinimoMaiorQueMaximo_deveLancarRegraNegocio() {
        ProjetoFiltro filtro = new ProjetoFiltro(null, null, null, new BigDecimal("500000"), new BigDecimal("100000"));

        assertThatThrownBy(() -> service.listar(filtro, PageRequest.of(0, 10)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("orçamento mínimo");
        verify(projetoRepository, never())
                .findAll(ArgumentMatchers.<Specification<Projeto>>any(), any(Pageable.class));
    }

    @Test
    void atualizar_deveAlterarOsDadosSemMudarOStatus() {
        Projeto projeto = projetoComStatus(StatusProjeto.PLANEJADO);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(membroRepository.findById(1L)).thenReturn(Optional.of(gerente()));

        ProjetoResponse response = service.atualizar(1L,
                request(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1), null, 1L));

        assertThat(projeto.getNome()).isEqualTo("Projeto Novo");
        assertThat(projeto.getDataInicio()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(response.status()).isEqualTo(StatusProjeto.PLANEJADO);
    }

    @Test
    void atualizar_comProjetoInexistente_deveLancarNaoEncontrado() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());
        ProjetoRequest request = request(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), null, 1L);

        assertThatThrownBy(() -> service.atualizar(99L, request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizar_comPrevisaoAnteriorAoInicio_deveLancarRegraNegocio() {
        ProjetoRequest request = request(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 1, 1), null, 1L);

        assertThatThrownBy(() -> service.atualizar(1L, request))
                .isInstanceOf(RegraNegocioException.class);
        verify(projetoRepository, never()).findById(any());
    }

    @Test
    void alterarStatus_deveAvancarParaOProximoStatus() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        ProjetoResponse response = service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.ANALISE_REALIZADA));

        assertThat(response.status()).isEqualTo(StatusProjeto.ANALISE_REALIZADA);
    }

    @Test
    void alterarStatus_deveRecusarPularEtapa() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.INICIADO)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Transição de status inválida");
        assertThat(projeto.getStatus()).isEqualTo(StatusProjeto.EM_ANALISE);
    }

    @Test
    void alterarStatus_aoEncerrar_deveRegistrarDataRealDeTermino() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANDAMENTO);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.ENCERRADO));

        assertThat(projeto.getDataRealTermino()).isEqualTo(LocalDate.now());
    }

    @Test
    void alterarStatus_aoEncerrarComDataRealJaInformada_naoDeveSobrescrever() {
        Projeto projeto = projetoComStatus(StatusProjeto.EM_ANDAMENTO);
        projeto.setDataRealTermino(LocalDate.of(2026, 2, 20));
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.ENCERRADO));

        assertThat(projeto.getDataRealTermino()).isEqualTo(LocalDate.of(2026, 2, 20));
    }

    @Test
    void alterarStatus_comProjetoInexistente_deveLancarNaoEncontrado() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alterarStatus(99L, new AlterarStatusRequest(StatusProjeto.CANCELADO)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @ParameterizedTest
    @EnumSource(value = StatusProjeto.class, names = {"INICIADO", "EM_ANDAMENTO", "ENCERRADO"})
    void excluir_comStatusBloqueado_deveLancarRegraNegocioENaoExcluir(StatusProjeto status) {
        Projeto projeto = projetoComStatus(status);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não pode ser excluído");
        verify(projetoRepository, never()).delete(any(Projeto.class));
    }

    @ParameterizedTest
    @EnumSource(value = StatusProjeto.class, names = {"INICIADO", "EM_ANDAMENTO", "ENCERRADO"},
            mode = EnumSource.Mode.EXCLUDE)
    void excluir_comStatusPermitido_deveExcluir(StatusProjeto status) {
        Projeto projeto = projetoComStatus(status);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        service.excluir(1L);

        verify(projetoRepository).delete(projeto);
    }

    @Test
    void excluir_comProjetoInexistente_deveLancarNaoEncontrado() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(projetoRepository, never()).delete(any(Projeto.class));
    }

    private Membro gerente() {
        Membro gerente = new Membro();
        gerente.setNome("Thiago");
        gerente.setAtribuicao("gerente");
        return gerente;
    }

    private Projeto projetoComStatus(StatusProjeto status) {
        Projeto projeto = new Projeto();
        projeto.setNome("Projeto Teste");
        projeto.setDataInicio(LocalDate.of(2026, 1, 1));
        projeto.setPrevisaoTermino(LocalDate.of(2026, 3, 1));
        projeto.setOrcamentoTotal(new BigDecimal("50000.00"));
        projeto.setStatus(status);
        projeto.setGerente(gerente());
        return projeto;
    }

    private ProjetoRequest request(LocalDate inicio, LocalDate previsao, LocalDate dataReal, Long gerenteId) {
        return new ProjetoRequest(
                "Projeto Novo",
                "Descrição de teste",
                inicio,
                previsao,
                dataReal,
                new BigDecimal("50000.00"),
                gerenteId
        );
    }

    @Test
    void alterarStatus_aoIniciarSemMembros_deveLancarRegraNegocio() {
        Projeto projeto = projetoComStatus(StatusProjeto.ANALISE_APROVADA);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.INICIADO)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ao menos 1 membro");
        assertThat(projeto.getStatus()).isEqualTo(StatusProjeto.ANALISE_APROVADA);
    }

    @Test
    void alterarStatus_aoIniciarComMembro_deveIniciar() {
        Projeto projeto = projetoComStatus(StatusProjeto.ANALISE_APROVADA);
        projeto.getMembros().add(gerente());
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        ProjetoResponse response = service.alterarStatus(1L, new AlterarStatusRequest(StatusProjeto.INICIADO));

        assertThat(response.status()).isEqualTo(StatusProjeto.INICIADO);
    }
}