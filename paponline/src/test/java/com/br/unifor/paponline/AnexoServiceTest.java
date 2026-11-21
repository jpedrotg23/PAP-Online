package com.br.unifor.paponline;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.repository.AnexoRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.repository.SolicitacaoAdaptacaoRepository;
import com.br.unifor.paponline.service.AnexoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnexoServiceTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void clearContext() {
        SessionContext.clear();
    }

    @Test
    void acceptsOnlyPdfWithMatchingExtensionAndContentType() {
        AnexoRepository anexos = mock(AnexoRepository.class);
        SolicitacaoAdaptacaoRepository solicitacoes = mock(SolicitacaoAdaptacaoRepository.class);
        ProfissionalRepository profissionais = mock(ProfissionalRepository.class);
        AnexoService service = new AnexoService(anexos, solicitacoes, profissionais);
        ReflectionTestUtils.setField(service, "pastaUploads", tempDir.toString());
        SessionContext.set(7L, "ALUNO");
        SolicitacaoAdaptacao solicitacao = new SolicitacaoAdaptacao();
        solicitacao.setAlunoId(7L);
        solicitacao.setStatus("ENVIADA");
        when(solicitacoes.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(anexos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile valid = new MockMultipartFile("arquivo", "documento.pdf",
                "application/pdf", "%PDF-1.7 conteúdo".getBytes());
        assertEquals("application/pdf", service.salvar(1L, valid).getTipoMime());

        MockMultipartFile spoofed = new MockMultipartFile("arquivo", "documento.pdf",
                "application/pdf", "conteúdo não PDF".getBytes());
        assertThrows(ResponseStatusException.class, () -> service.salvar(1L, spoofed));
    }

    @Test
    void studentCannotReadDocumentsFromAnotherStudentsRequest() {
        AnexoRepository anexos = mock(AnexoRepository.class);
        SolicitacaoAdaptacaoRepository solicitacoes = mock(SolicitacaoAdaptacaoRepository.class);
        ProfissionalRepository profissionais = mock(ProfissionalRepository.class);
        AnexoService service = new AnexoService(anexos, solicitacoes, profissionais);
        SessionContext.set(7L, "ALUNO");
        SolicitacaoAdaptacao solicitacao = new SolicitacaoAdaptacao();
        solicitacao.setAlunoId(8L);
        when(solicitacoes.findById(1L)).thenReturn(Optional.of(solicitacao));

        assertThrows(ResponseStatusException.class, () -> service.listarPorSolicitacao(1L));
    }
}
