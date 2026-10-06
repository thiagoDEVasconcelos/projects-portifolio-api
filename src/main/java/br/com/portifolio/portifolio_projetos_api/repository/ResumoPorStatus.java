package br.com.portifolio.portifolio_projetos_api.repository;

import br.com.portifolio.portifolio_projetos_api.model.StatusProjeto;

import java.math.BigDecimal;

public interface ResumoPorStatus {

    StatusProjeto getStatus();

    Long getQuantidade();

    BigDecimal getTotalOrcado();
}