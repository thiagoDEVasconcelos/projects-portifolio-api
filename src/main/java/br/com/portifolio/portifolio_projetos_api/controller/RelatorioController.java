package br.com.portifolio.portifolio_projetos_api.controller;

import br.com.portifolio.portifolio_projetos_api.dto.RelatorioPortfolioResponse;
import br.com.portifolio.portifolio_projetos_api.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService service;

    @GetMapping("/portfolio")
    public ResponseEntity<RelatorioPortfolioResponse> gerarRelatorioDoPortfolio() {
        return ResponseEntity.ok(service.gerar());
    }
}