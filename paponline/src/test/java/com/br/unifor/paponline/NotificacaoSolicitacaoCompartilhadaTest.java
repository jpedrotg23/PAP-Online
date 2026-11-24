package com.br.unifor.paponline;

import com.br.unifor.paponline.entidades.Notificacao;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.repository.AlunoRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.repository.NotificacaoRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.service.NotificacaoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificacaoSolicitacaoCompartilhadaTest {

    @Test
    void newRequestNotifiesEveryActiveProfessionalRegardlessOfArea() {
        NotificacaoRepository notificacoes = mock(NotificacaoRepository.class);
        ProfissionalRepository profissionais = mock(ProfissionalRepository.class);

        Profissional psicologo = new Profissional();
        psicologo.setId(10L);
        psicologo.setAreaId(1L);
        Profissional psicopedagogo = new Profissional();
        psicopedagogo.setId(20L);
        psicopedagogo.setAreaId(2L);

        when(profissionais.findByAtivoTrue()).thenReturn(List.of(psicologo, psicopedagogo));
        when(notificacoes.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificacaoService service = new NotificacaoService(
                notificacoes,
                mock(UsuarioRepository.class),
                mock(AlunoRepository.class),
                profissionais,
                mock(AtendimentoRepository.class)
        );

        SolicitacaoAdaptacao solicitacao = new SolicitacaoAdaptacao();
        solicitacao.setId(50L);
        solicitacao.setProtocolo("PAP-2026-0001");
        solicitacao.setTitulo("Solicitação de adaptação");
        solicitacao.setAreaId(null);

        service.notificarNovaSolicitacao(solicitacao);

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacoes, times(2)).save(captor.capture());
        assertEquals(List.of(10L, 20L), captor.getAllValues().stream()
                .map(Notificacao::getUsuarioId).toList());
    }
}
