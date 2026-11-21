package com.br.unifor.paponline;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Conversa;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.repository.ConversaRepository;
import com.br.unifor.paponline.repository.MensagemRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.service.ConversaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConversaServiceTest {

    private final ConversaRepository conversas = mock(ConversaRepository.class);
    private final MensagemRepository mensagens = mock(MensagemRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final AtendimentoRepository atendimentos = mock(AtendimentoRepository.class);
    private final ConversaService service = new ConversaService(conversas, mensagens, usuarios, atendimentos);

    @AfterEach
    void clearContext() {
        SessionContext.clear();
    }

    @Test
    void conversationRequiresExistingAppointmentAndAuthenticatedParticipant() {
        SessionContext.set(10L, "ALUNO");
        when(atendimentos.existsByAlunoIdAndProfissionalId(10L, 20L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.obterOuCriar(10L, 20L));
        verify(conversas, never()).save(any(Conversa.class));
    }

    @Test
    void onlyParticipantCanListConversationMessages() {
        SessionContext.set(99L, "ALUNO");
        Conversa conversa = new Conversa();
        conversa.setAlunoId(10L);
        conversa.setProfissionalId(20L);
        when(conversas.findById(5L)).thenReturn(Optional.of(conversa));

        assertThrows(ResponseStatusException.class, () -> service.listarMensagens(5L));
        verify(mensagens, never()).findByConversaIdOrderByEnviadaEmAsc(5L);
    }
}
