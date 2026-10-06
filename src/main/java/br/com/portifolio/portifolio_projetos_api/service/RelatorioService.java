package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.dto.RelatorioPortfolioResponse;
import br.com.portifolio.portifolio_projetos_api.dto.ResumoStatusResponse;
import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import br.com.portifolio.portifolio_projetos_api.repository.ProjetoRepository;
import br.com.portifolio.portifolio_projetos_api.repository.ResumoPorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final ProjetoRepository projetoRepository;

    @Transactional(readOnly = true)
    public RelatorioPortfolioResponse gerar() {
        return new RelatorioPortfolioResponse(
                resumirPorStatus(),
                calcularMediaDuracaoDosEncerrados(),
                projetoRepository.contarMembrosUnicosAlocados()
        );
    }

    private List<ResumoStatusResponse> resumirPorStatus() {
        Map<StatusProjeto, ResumoPorStatus> resumos = projetoRepository.resumirPorStatus().stream()
                .collect(Collectors.toMap(ResumoPorStatus::getStatus, Function.identity()));

        return Arrays.stream(StatusProjeto.values())
                .map(status -> {
                    ResumoPorStatus resumo = resumos.get(status);
                    if (resumo == null) {
                        return new ResumoStatusResponse(status, 0L, BigDecimal.ZERO);
                    }
                    return new ResumoStatusResponse(status, resumo.getQuantidade(), resumo.getTotalOrcado());
                })
                .toList();
    }

    private BigDecimal calcularMediaDuracaoDosEncerrados() {
        List<Projeto> encerrados =
                projetoRepository.findByStatusAndDataRealTerminoIsNotNull(StatusProjeto.ENCERRADO);
        if (encerrados.isEmpty()) {
            return null;
        }

        long totalDias = encerrados.stream()
                .mapToLong(projeto -> ChronoUnit.DAYS.between(projeto.getDataInicio(), projeto.getDataRealTermino()))
                .sum();

        return BigDecimal.valueOf(totalDias)
                .divide(BigDecimal.valueOf(encerrados.size()), 2, RoundingMode.HALF_UP);
    }
}