package br.com.portifolio.portifolio_projetos_api.repository;

import br.com.portifolio.portifolio_projetos_api.model.Projeto;
import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long>, JpaSpecificationExecutor<Projeto> {

    @Query("""
            select count(p) from Projeto p
            join p.equipe m
            where m.id = :membroId
              and p.status not in :statusFinalizados
            """)
    long contarProjetosAtivosDoMembro(@Param("membroId") Long membroId, @Param("statusFinalizados") Collection<StatusProjeto> statusFinalizados);

    @Query("""
            select p.status as status, count(p) as quantidade, sum(p.orcamentoTotal) as totalOrcado
            from Projeto p
            group by p.status
            """)
    List<ResumoPorStatus> resumirPorStatus();

    List<Projeto> findByStatusAndDataRealTerminoIsNotNull(StatusProjeto status);

    @Query("select count(distinct m.id) from Projeto p join p.equipe m")
    long contarMembrosUnicosAlocados();
}