package br.com.portifolio.portifolio_projetos_api.controller;

import br.com.portifolio.portifolio_projetos_api.dto.MembroRequest;
import br.com.portifolio.portifolio_projetos_api.dto.MembroResumoResponse;
import br.com.portifolio.portifolio_projetos_api.service.MembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Membros", description = "Cadastro (via API externa) e consulta de membros")
@RestController
@RequestMapping("/membros")
@RequiredArgsConstructor
public class MembroController {

    private final MembroService service;

    @Operation(summary = "Cadastra um membro",
            description = "O membro é criado na API externa (mockada) e uma cópia local é guardada "
                    + "para os relacionamentos com os projetos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membro cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (nome ou atribuição ausentes)")
    })
    @PostMapping
    public ResponseEntity<MembroResumoResponse> criar(@Valid @RequestBody MembroRequest request) {
        MembroResumoResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Busca um membro por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro encontrado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MembroResumoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Lista os membros com paginação")
    @GetMapping
    public ResponseEntity<Page<MembroResumoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }
}