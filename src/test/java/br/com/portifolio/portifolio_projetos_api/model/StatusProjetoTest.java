package br.com.portifolio.portifolio_projetos_api.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static br.com.portifolio.portifolio_projetos_api.model.StatusProjeto.*;
import static org.assertj.core.api.Assertions.assertThat;

class StatusProjetoTest {

    @Test
    void devePermitirAvancarParaOProximoDaSequencia() {
        StatusProjeto[] sequencia = {EM_ANALISE, ANALISE_REALIZADA, ANALISE_APROVADA,
                INICIADO, PLANEJADO, EM_ANDAMENTO, ENCERRADO};

        for (int i = 0; i < sequencia.length - 1; i++) {
            assertThat(sequencia[i].podeTransicionarPara(sequencia[i + 1])).isTrue();
        }
    }

    @Test
    void naoDevePermitirPularEtapas() {
        assertThat(EM_ANALISE.podeTransicionarPara(ANALISE_APROVADA)).isFalse();
        assertThat(INICIADO.podeTransicionarPara(EM_ANDAMENTO)).isFalse();
    }

    @Test
    void naoDevePermitirVoltarNaSequencia() {
        assertThat(PLANEJADO.podeTransicionarPara(INICIADO)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = StatusProjeto.class, names = {"ENCERRADO", "CANCELADO"}, mode = EnumSource.Mode.EXCLUDE)
    void devePermitirCancelarDeQualquerStatusAtivo(StatusProjeto origem) {
        assertThat(origem.podeTransicionarPara(CANCELADO)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = StatusProjeto.class, names = {"ENCERRADO", "CANCELADO"})
    void estadosFinaisNaoDevemTransicionarParaNenhumStatus(StatusProjeto origem) {
        for (StatusProjeto destino : StatusProjeto.values()) {
            assertThat(origem.podeTransicionarPara(destino)).isFalse();
        }
    }

    @ParameterizedTest
    @EnumSource(value = StatusProjeto.class, names = {"INICIADO", "EM_ANDAMENTO", "ENCERRADO"})
    void naoDevePermitirExclusaoNosStatusBloqueados(StatusProjeto status) {
        assertThat(status.permiteExclusao()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = StatusProjeto.class, names = {"INICIADO", "EM_ANDAMENTO", "ENCERRADO"}, mode = EnumSource.Mode.EXCLUDE)
    void devePermitirExclusaoNosDemaisStatus(StatusProjeto status) {
        assertThat(status.permiteExclusao()).isTrue();
    }
}