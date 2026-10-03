package br.com.portifolio.portifolio_projetos_api.repository;

import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
}
