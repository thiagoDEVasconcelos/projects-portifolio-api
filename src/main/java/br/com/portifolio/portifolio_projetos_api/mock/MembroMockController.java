package br.com.portifolio.portifolio_projetos_api.mock;

import br.com.portifolio.portifolio_projetos_api.client.MembroExternoRequest;
import br.com.portifolio.portifolio_projetos_api.client.MembroExternoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Tag(name = "API externa (mock)", description = "Simula o sistema externo de membros; dados em memória")
@RestController
@RequestMapping("/api-externa/membros")
public class MembroMockController {

    private final Map<Long, MembroExternoResponse> membros = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    @Operation(summary = "Cria um membro no sistema externo",
            description = "Recebe nome e atribuição (cargo) e devolve o membro com o id gerado.")
    @PostMapping
    public ResponseEntity<MembroExternoResponse> criar(@RequestBody MembroExternoRequest request) {
        long id = sequencia.incrementAndGet();
        MembroExternoResponse membro = new MembroExternoResponse(id, request.nome(), request.atribuicao());
        membros.put(id, membro);
        return ResponseEntity.status(HttpStatus.CREATED).body(membro);
    }

    @Operation(summary = "Consulta um membro no sistema externo")
    @GetMapping("/{id}")
    public ResponseEntity<MembroExternoResponse> buscarPorId(@PathVariable Long id) {
        MembroExternoResponse membro = membros.get(id);
        return membro != null ? ResponseEntity.ok(membro) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Lista os membros do sistema externo")
    @GetMapping
    public List<MembroExternoResponse> listar() {
        return List.copyOf(membros.values());
    }
}