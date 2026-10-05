package br.com.portifolio.portifolio_projetos_api.mock;

import br.com.portifolio.portifolio_projetos_api.client.MembroExternoRequest;
import br.com.portifolio.portifolio_projetos_api.client.MembroExternoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api-externa/membros")
public class MembroMockController {

    private final Map<Long, MembroExternoResponse> membros = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    @PostMapping
    public ResponseEntity<MembroExternoResponse> criar(@RequestBody MembroExternoRequest request) {
        long id = sequencia.incrementAndGet();
        MembroExternoResponse membro = new MembroExternoResponse(id, request.nome(), request.atribuicao());
        membros.put(id, membro);
        return ResponseEntity.status(HttpStatus.CREATED).body(membro);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembroExternoResponse> buscarPorId(@PathVariable Long id) {
        MembroExternoResponse membro = membros.get(id);
        return membro != null ? ResponseEntity.ok(membro) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<MembroExternoResponse> listar() {
        return List.copyOf(membros.values());
    }
}