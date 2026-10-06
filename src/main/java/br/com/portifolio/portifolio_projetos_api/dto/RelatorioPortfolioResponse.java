package br.com.portifolio.portifolio_projetos_api.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioPortfolioResponse(
        List<ResumoStatusResponse> porStatus,
        BigDecimal mediaDuracaoDiasEncerrados,
        long totalMembrosUnicosAlocados
) {
}