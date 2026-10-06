package br.com.portifolio.portifolio_projetos_api.controller;

import br.com.portifolio.portifolio_projetos_api.dto.AlocarMembroRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoResponse;
import br.com.portifolio.portifolio_projetos_api.service.AlocacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projetos/{projetoId}/membros")
@RequiredArgsConstructor
public class AlocacaoController {

    private final AlocacaoService service;

    @PostMapping
    public ResponseEntity<ProjetoResponse> alocar(@PathVariable Long projetoId, @Valid @RequestBody AlocarMembroRequest request) {
        return ResponseEntity.ok(service.alocar(projetoId, request.membroId()));
    }

    @DeleteMapping("/{membroId}")
    public ResponseEntity<ProjetoResponse> desalocar(@PathVariable Long projetoId, @PathVariable Long membroId) {
        return ResponseEntity.ok(service.desalocar(projetoId, membroId));
    }
}