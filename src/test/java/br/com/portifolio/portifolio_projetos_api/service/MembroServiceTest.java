package br.com.portifolio.portifolio_projetos_api.service;

import br.com.portifolio.portifolio_projetos_api.client.MembroClient;
import br.com.portifolio.portifolio_projetos_api.client.MembroExternoRequest;
import br.com.portifolio.portifolio_projetos_api.client.MembroExternoResponse;
import br.com.portifolio.portifolio_projetos_api.dto.MembroRequest;
import br.com.portifolio.portifolio_projetos_api.dto.MembroResumoResponse;
import br.com.portifolio.portifolio_projetos_api.exception.RecursoNaoEncontradoException;
import br.com.portifolio.portifolio_projetos_api.mapper.MembroMapper;
import br.com.portifolio.portifolio_projetos_api.model.Membro;
import br.com.portifolio.portifolio_projetos_api.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroServiceTest {

    @Mock
    private MembroClient membroClient;

    @Mock
    private MembroRepository membroRepository;

    @Spy
    private MembroMapper mapper = new MembroMapper();

    @InjectMocks
    private MembroService service;

    @Test
    void criar_deveCadastrarNaApiExternaESalvarCopiaLocal() {
        when(membroClient.criar(new MembroExternoRequest("Thiago", "funcionário")))
                .thenReturn(new MembroExternoResponse(7L, "Thiago", "funcionário"));
        when(membroRepository.save(any(Membro.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        MembroResumoResponse response = service.criar(new MembroRequest("Thiago", "funcionário"));

        ArgumentCaptor<Membro> captor = ArgumentCaptor.forClass(Membro.class);
        verify(membroRepository).save(captor.capture());
        assertThat(captor.getValue().getIdExterno()).isEqualTo(7L);
        assertThat(response.nome()).isEqualTo("Thiago");
        assertThat(response.atribuicao()).isEqualTo("funcionário");
    }

    @Test
    void buscarPorId_comMembroInexistente_deveLancarNaoEncontrado() {
        when(membroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}