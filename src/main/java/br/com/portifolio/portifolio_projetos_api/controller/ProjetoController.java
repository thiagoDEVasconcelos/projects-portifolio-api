package br.com.portifolio.portifolio_projetos_api.controller;

import br.com.portifolio.portifolio_projetos_api.dto.AlterarStatusRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoFiltro;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoRequest;
import br.com.portifolio.portifolio_projetos_api.dto.ProjetoResponse;
import br.com.portifolio.portifolio_projetos_api.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Projetos", description = "CRUD, mudança de status e listagem com filtros")
@RestController
@RequestMapping("/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService service;

    @Operation(summary = "Cria um projeto", description = "O projeto nasce com o status EM_ANALISE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projeto criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Gerente não encontrado"),
            @ApiResponse(responseCode = "422", description = "Datas inconsistentes")
    })
    @PostMapping
    public ResponseEntity<ProjetoResponse> criar(@Valid @RequestBody ProjetoRequest request) {
        ProjetoResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Busca um projeto por id")
    @GetMapping("/{id}")
    public ResponseEntity<ProjetoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Lista projetos com paginação e filtros",
            description = "Filtros opcionais: nome (trecho, sem diferenciar maiúsculas), status, gerenteId e faixa de orçamento.")
    @GetMapping
    public ResponseEntity<Page<ProjetoResponse>> listar(@ParameterObject ProjetoFiltro filtro,
                                                        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.listar(filtro, pageable));
    }

    @Operation(summary = "Atualiza os dados de um projeto",
            description = "Não altera o status. Para isso, use PATCH /projetos/{id}/status.")
    @PutMapping("/{id}")
    public ResponseEntity<ProjetoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ProjetoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @Operation(summary = "Altera o status do projeto",
            description = "Só é permitido avançar para o próximo status da sequência, ou cancelar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
            @ApiResponse(responseCode = "422", description = "Transição inválida, ou projeto sem membros ao iniciar")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjetoResponse> alterarStatus(@PathVariable Long id,
                                                         @Valid @RequestBody AlterarStatusRequest request) {
        return ResponseEntity.ok(service.alterarStatus(id, request));
    }

    @Operation(summary = "Exclui um projeto",
            description = "Bloqueado quando o status é INICIADO, EM_ANDAMENTO ou ENCERRADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projeto excluído"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
            @ApiResponse(responseCode = "422", description = "Status não permite exclusão")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}