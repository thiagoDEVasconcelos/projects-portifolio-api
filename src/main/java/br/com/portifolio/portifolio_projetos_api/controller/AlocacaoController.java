package br.com.portifolio.portifolio_projetos_api.controller;

import br.com.portifolio.portifolio_projetos_api.dto.AlocarMembroRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoResponse;
import br.com.portifolio.portifolio_projetos_api.service.AlocacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alocação", description = "Equipe dos projetos")
@RestController
@RequestMapping("/projetos/{projetoId}/membros")
@RequiredArgsConstructor
public class AlocacaoController {

    private final AlocacaoService service;

    @Operation(summary = "Aloca um membro na equipe do projeto",
            description = "Regras: apenas membros com atribuição \"funcionário\" (conferida na API externa); "
                    + "máximo de 10 membros por projeto; máximo de 3 projetos ativos por membro; "
                    + "projetos encerrados ou cancelados não aceitam alterações na equipe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro alocado; devolve o projeto atualizado"),
            @ApiResponse(responseCode = "404", description = "Projeto ou membro não encontrado"),
            @ApiResponse(responseCode = "422", description = "Alguma regra de alocação foi violada")
    })
    @PostMapping
    public ResponseEntity<ProjetoResponse> alocar(@PathVariable Long projetoId,
                                                  @Valid @RequestBody AlocarMembroRequest request) {
        return ResponseEntity.ok(service.alocar(projetoId, request.membroId()));
    }

    @Operation(summary = "Remove um membro da equipe do projeto",
            description = "O projeto deve manter ao menos 1 membro alocado. "
                    + "Projetos encerrados ou cancelados não aceitam alterações na equipe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro removido; devolve o projeto atualizado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado ou membro fora da equipe"),
            @ApiResponse(responseCode = "422", description = "Remoção não permitida pelas regras do projeto")
    })
    @DeleteMapping("/{membroId}")
    public ResponseEntity<ProjetoResponse> desalocar(@PathVariable Long projetoId,
                                                     @PathVariable Long membroId) {
        return ResponseEntity.ok(service.desalocar(projetoId, membroId));
    }
}