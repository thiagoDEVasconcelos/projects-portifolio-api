package br.com.portifolio.portifolio_projetos_api.repository;

import br.com.portifolio.portifolio_projetos_api.model.Membro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembroRepository extends JpaRepository<Membro, Long> {
}
