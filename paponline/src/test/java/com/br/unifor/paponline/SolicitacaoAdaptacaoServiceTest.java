package com.br.unifor.paponline;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.repository.AreaServicoRepository;
import com.br.unifor.paponline.repository.SolicitacaoAdaptacaoRepository;
import com.br.unifor.paponline.service.NotificacaoService;
import com.br.unifor.paponline.service.SolicitacaoAdaptacaoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class SolicitacaoAdaptacaoServiceTest {

    @AfterEach
    void clearContext() {
        SessionContext.clear();
    }

    @Test
    void professionalCanListRequestsFromAllAreasAndAssignments() {
        SolicitacaoAdaptacaoRepository repository = mock(SolicitacaoAdaptacaoRepository.class);
        SolicitacaoAdaptacaoService service = newService(repository);

        SolicitacaoAdaptacao semResponsavel = new SolicitacaoAdaptacao();
        semResponsavel.setId(1L);
        semResponsavel.setAreaId(10L);

        SolicitacaoAdaptacao deOutraArea = new SolicitacaoAdaptacao();
        deOutraArea.setId(2L);
        deOutraArea.setAreaId(20L);
        deOutraArea.setProfissionalId(99L);

        when(repository.findAll()).thenReturn(List.of(semResponsavel, deOutraArea));
        SessionContext.set(7L, "PROFISSIONAL");

        assertEquals(List.of(semResponsavel, deOutraArea), service.listar());
        verify(repository).findAll();
        verify(repository, never()).findByProfissionalId(anyLong());
    }

    @Test
    void professionalCanOpenRequestFromAnotherArea() {
        SolicitacaoAdaptacaoRepository repository = mock(SolicitacaoAdaptacaoRepository.class);
        SolicitacaoAdaptacaoService service = newService(repository);
        SolicitacaoAdaptacao solicitacao = new SolicitacaoAdaptacao();
        solicitacao.setId(1L);
        solicitacao.setAreaId(20L);
        solicitacao.setProfissionalId(null);

        when(repository.findById(1L)).thenReturn(Optional.of(solicitacao));
        SessionContext.set(7L, "PROFISSIONAL");

        assertSame(solicitacao, service.buscar(1L));
    }

    @Test
    void professionalCannotChangeRequestAssignedToAnotherProfessional() {
        SolicitacaoAdaptacaoRepository repository = mock(SolicitacaoAdaptacaoRepository.class);
        SolicitacaoAdaptacaoService service = newService(repository);
        SolicitacaoAdaptacao solicitacao = new SolicitacaoAdaptacao();
        solicitacao.setId(1L);
        solicitacao.setStatus("EM_ANALISE");
        solicitacao.setProfissionalId(99L);

        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(solicitacao));
        SessionContext.set(7L, "PROFISSIONAL");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.analisar(1L, Map.of("status", "DEFERIDA", "parecer", "Parecer de teste")));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    private SolicitacaoAdaptacaoService newService(SolicitacaoAdaptacaoRepository repository) {
        return new SolicitacaoAdaptacaoService(
                repository,
                mock(NotificacaoService.class),
                mock(AreaServicoRepository.class)
        );
    }
}
