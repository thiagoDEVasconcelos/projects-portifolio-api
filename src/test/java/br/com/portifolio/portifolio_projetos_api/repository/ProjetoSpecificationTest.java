package br.com.portifolio.portifolio_projetos_api.repository;

import br.com.portifolio.portifolio_projetos_api.dto.ProjetoFiltro;
import br.com.portifolio.portifolio_projetos_api.model.Membro;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjetoSpecificationTest {

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private MembroRepository membroRepository;

    private Long gerenteId;

    @BeforeEach
    void prepararDados() {
        Membro gerente = new Membro();
        gerente.setIdExterno(900101L);
        gerente.setNome("Gerente de Teste");
        gerente.setAtribuicao("gerente");
        gerente = membroRepository.save(gerente);
        gerenteId = gerente.getId();

        projetoRepository.save(novoProjeto("Sistema Financeiro", StatusProjeto.EM_ANALISE, "50000.00", gerente));
        projetoRepository.save(novoProjeto("Portal do Cliente", StatusProjeto.INICIADO, "250000.00", gerente));
        projetoRepository.save(novoProjeto("Aplicativo Móvel", StatusProjeto.INICIADO, "900000.00", gerente));
    }

    @Test
    void semFiltros_deveRetornarTodosOsProjetosDoGerente() {
        assertThat(buscar(filtro(null, null, null, null))).hasSize(3);
    }

    @Test
    void filtroPorNome_deveIgnorarCaixaEBuscarTrechos() {
        List<Projeto> resultado = buscar(filtro("FINANCEIRO", null, null, null));

        assertThat(resultado).extracting(Projeto::getNome).containsExactly("Sistema Financeiro");
    }

    @Test
    void filtroPorStatus_deveRetornarApenasOStatusInformado() {
        List<Projeto> resultado = buscar(filtro(null, StatusProjeto.INICIADO, null, null));

        assertThat(resultado).extracting(Projeto::getNome)
                .containsExactlyInAnyOrder("Portal do Cliente", "Aplicativo Móvel");
    }

    @Test
    void filtroPorFaixaDeOrcamento_deveIncluirOsLimites() {
        List<Projeto> resultado = buscar(
                filtro(null, null, new BigDecimal("50000.00"), new BigDecimal("250000.00")));

        assertThat(resultado).extracting(Projeto::getNome)
                .containsExactlyInAnyOrder("Sistema Financeiro", "Portal do Cliente");
    }

    @Test
    void filtrosCombinados_devemSerAplicadosComE() {
        List<Projeto> resultado = buscar(
                filtro(null, StatusProjeto.INICIADO, new BigDecimal("500000"), null));

        assertThat(resultado).extracting(Projeto::getNome).containsExactly("Aplicativo Móvel");
    }

    private List<Projeto> buscar(ProjetoFiltro filtro) {
        return projetoRepository.findAll(ProjetoSpecification.comFiltros(filtro));
    }

    private ProjetoFiltro filtro(String nome, StatusProjeto status, BigDecimal min, BigDecimal max) {
        return new ProjetoFiltro(nome, status, gerenteId, min, max);
    }

    private Projeto novoProjeto(String nome, StatusProjeto status, String orcamento, Membro gerente) {
        Projeto projeto = new Projeto();
        projeto.setNome(nome);
        projeto.setDataInicio(LocalDate.of(2026, 1, 1));
        projeto.setPrevisaoTermino(LocalDate.of(2026, 3, 1));
        projeto.setOrcamentoTotal(new BigDecimal(orcamento));
        projeto.setStatus(status);
        projeto.setGerente(gerente);
        return projeto;
    }
}