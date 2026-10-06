package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.model.ClassificacaoRisco;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RiscoCalculatorTest {

    private final RiscoCalculator calculator = new RiscoCalculator();

    @ParameterizedTest
    @CsvSource({
            "10000.00,  2026-02-01, BAIXO",
            "100000.00, 2026-04-01, BAIXO",
            "100000.00, 2026-04-02, MEDIO",
            "100000.01, 2026-02-01, MEDIO",
            "500000.00, 2026-07-01, MEDIO",
            "500000.01, 2026-02-01, ALTO",
            "50000.00,  2026-07-02, ALTO"
    })
    void deveClassificarORiscoConsiderandoOsLimites(BigDecimal orcamento, LocalDate previsao, ClassificacaoRisco esperado) {
        Projeto projeto = new Projeto();
        projeto.setOrcamentoTotal(orcamento);
        projeto.setDataInicio(LocalDate.of(2026, 1, 1));
        projeto.setPrevisaoTermino(previsao);

        assertThat(calculator.calcular(projeto)).isEqualTo(esperado);
    }
}