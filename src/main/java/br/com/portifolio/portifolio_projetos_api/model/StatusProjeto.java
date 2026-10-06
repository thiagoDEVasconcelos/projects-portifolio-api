package br.com.portifolio.portifolio_projetos_api.model;

import java.util.List;

public enum StatusProjeto {
    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    public boolean podeTransicionarPara(StatusProjeto destino) {
        if (this == ENCERRADO || this == CANCELADO) {
            return false;
        }
        if (destino == CANCELADO) {
            return true;
        }
        return destino.ordinal() == this.ordinal() + 1;
    }

    public boolean permiteExclusao() {
        return this != INICIADO && this != EM_ANDAMENTO && this != ENCERRADO;
    }

    public boolean isAtivo() {
        return this != ENCERRADO && this != CANCELADO;
    }

    public static List<StatusProjeto> finalizados() {
        return List.of(ENCERRADO, CANCELADO);
    }
}