package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.model.ClassificacaoRisco;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class RiscoCalculator {

    private static final BigDecimal LIMITE_BAIXO = new BigDecimal("100000");
    private static final BigDecimal LIMITE_MEDIO = new BigDecimal("500000");

    public ClassificacaoRisco calcular(Projeto projeto) {
        BigDecimal orcamento = projeto.getOrcamentoTotal();
        LocalDate inicio = projeto.getDataInicio();
        LocalDate previsao = projeto.getPrevisaoTermino();

        boolean prazoMaiorQue6Meses = previsao.isAfter(inicio.plusMonths(6));
        boolean prazoMaiorQue3Meses = previsao.isAfter(inicio.plusMonths(3));

        if (orcamento.compareTo(LIMITE_MEDIO) > 0 || prazoMaiorQue6Meses) {
            return ClassificacaoRisco.ALTO;
        }
        if (orcamento.compareTo(LIMITE_BAIXO) > 0 || prazoMaiorQue3Meses) {
            return ClassificacaoRisco.MEDIO;
        }
        return ClassificacaoRisco.BAIXO;
    }
}
