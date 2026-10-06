package br.com.portifolio.portifolio_projetos_api.controller;

import br.com.portifolio.portifolio_projetos_api.dto.RelatorioPortfolioResponse;
import br.com.portifolio.portifolio_projetos_api.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Relatórios", description = "Resumo do portfólio")
@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService service;

    @Operation(summary = "Gera o relatório resumido do portfólio",
            description = "Devolve a quantidade de projetos e o total orçado por status, a média de duração "
                    + "(em dias) dos projetos encerrados e o total de membros únicos alocados.")
    @GetMapping("/portfolio")
    public ResponseEntity<RelatorioPortfolioResponse> gerarRelatorioDoPortfolio() {
        return ResponseEntity.ok(service.gerar());
    }
}