package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.dto.RelatorioPortfolioResponse;
import br.com.portifolio.portifolio_projetos_api.dto.ResumoStatusResponse;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import br.com.portifolio.portifolio_projetos_api.repository.ProjetoRepository;
import br.com.portifolio.portifolio_projetos_api.repository.ResumoPorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private RelatorioService service;

    @Test
    void gerar_deveListarTodosOsStatusComZeroParaOsAusentes() {
        when(projetoRepository.resumirPorStatus()).thenReturn(List.of(
                resumo(StatusProjeto.EM_ANALISE, 2L, "150000.00"),
                resumo(StatusProjeto.ENCERRADO, 1L, "80000.00")
        ));

        RelatorioPortfolioResponse relatorio = service.gerar();

        assertThat(relatorio.porStatus()).hasSize(StatusProjeto.values().length);
        assertThat(buscar(relatorio, StatusProjeto.EM_ANALISE).quantidade()).isEqualTo(2L);
        assertThat(buscar(relatorio, StatusProjeto.EM_ANALISE).totalOrcado()).isEqualByComparingTo("150000.00");
        assertThat(buscar(relatorio, StatusProjeto.CANCELADO).quantidade()).isZero();
        assertThat(buscar(relatorio, StatusProjeto.CANCELADO).totalOrcado()).isEqualByComparingTo("0");
    }

    @Test
    void gerar_deveCalcularMediaDeDuracaoDosProjetosEncerrados() {
        when(projetoRepository.findByStatusAndDataRealTerminoIsNotNull(StatusProjeto.ENCERRADO)).thenReturn(List.of(
                encerrado(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 11)),
                encerrado(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 21))
        ));

        RelatorioPortfolioResponse relatorio = service.gerar();

        assertThat(relatorio.mediaDuracaoDiasEncerrados()).isEqualByComparingTo("15.00");
    }

    @Test
    void gerar_deveArredondarAMediaParaDuasCasas() {
        when(projetoRepository.findByStatusAndDataRealTerminoIsNotNull(StatusProjeto.ENCERRADO)).thenReturn(List.of(
                encerrado(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 11)),
                encerrado(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 11)),
                encerrado(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 12))
        ));

        RelatorioPortfolioResponse relatorio = service.gerar();

        assertThat(relatorio.mediaDuracaoDiasEncerrados()).isEqualByComparingTo("10.33");
    }

    @Test
    void gerar_semProjetosEncerrados_deveRetornarMediaNula() {
        RelatorioPortfolioResponse relatorio = service.gerar();

        assertThat(relatorio.mediaDuracaoDiasEncerrados()).isNull();
    }

    @Test
    void gerar_deveInformarOTotalDeMembrosUnicosAlocados() {
        when(projetoRepository.contarMembrosUnicosAlocados()).thenReturn(7L);

        RelatorioPortfolioResponse relatorio = service.gerar();

        assertThat(relatorio.totalMembrosUnicosAlocados()).isEqualTo(7L);
    }

    private ResumoStatusResponse buscar(RelatorioPortfolioResponse relatorio, StatusProjeto status) {
        return relatorio.porStatus().stream()
                .filter(resumo -> resumo.status() == status)
                .findFirst()
                .orElseThrow();
    }

    private ResumoPorStatus resumo(StatusProjeto status, Long quantidade, String total) {
        return new ResumoPorStatus() {
            @Override
            public StatusProjeto getStatus() {
                return status;
            }

            @Override
            public Long getQuantidade() {
                return quantidade;
            }

            @Override
            public BigDecimal getTotalOrcado() {
                return new BigDecimal(total);
            }
        };
    }

    private Projeto encerrado(LocalDate inicio, LocalDate fim) {
        Projeto projeto = new Projeto();
        projeto.setStatus(StatusProjeto.ENCERRADO);
        projeto.setDataInicio(inicio);
        projeto.setDataRealTermino(fim);
        return projeto;
    }
}