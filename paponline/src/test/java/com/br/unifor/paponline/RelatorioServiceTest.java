package com.br.unifor.paponline;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.dto.RelatorioResumoResponse;
import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.entidades.AreaServico;
import com.br.unifor.paponline.entidades.Atendimento;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.repository.AlunoRepository;
import com.br.unifor.paponline.repository.AreaServicoRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.repository.SolicitacaoAdaptacaoRepository;
import com.br.unifor.paponline.service.RelatorioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioServiceTest {

    @AfterEach
    void limparSessao() {
        SessionContext.clear();
    }

    @Test
    void agregaAtendimentosFaltasSolicitacoesAreasEProfissionais() {
        SessionContext.set(1L, "ADMIN");

        Atendimento realizado = new Atendimento();
        realizado.setStatus("REALIZADO");
        realizado.setDataHoraInicio(LocalDateTime.of(2026, 10, 5, 10, 0));
        realizado.setAreaId(2L);
        realizado.setProfissionalId(9L);
        Atendimento falta = new Atendimento();
        falta.setStatus("FALTA");
        falta.setDataHoraInicio(LocalDateTime.of(2026, 10, 8, 10, 0));
        falta.setAreaId(2L);
        falta.setProfissionalId(9L);

        SolicitacaoAdaptacao solicitacao = new SolicitacaoAdaptacao();
        solicitacao.setStatus("DEFERIDA");
        solicitacao.setDataEnvio(LocalDateTime.of(2026, 10, 7, 10, 0));
        solicitacao.setAreaId(2L);

        Aluno aluno = new Aluno();
        aluno.setAtivo(true);
        Profissional profissional = new Profissional();
        profissional.setId(9L);
        profissional.setNome("Profissional PAP");
        profissional.setAtivo(true);
        AreaServico area = new AreaServico();
        area.setId(2L);
        area.setNome("Psicologia");

        AtendimentoRepository atendimentos = Mockito.mock(AtendimentoRepository.class);
        Mockito.when(atendimentos.findAll()).thenReturn(List.of(realizado, falta));
        SolicitacaoAdaptacaoRepository solicitacoes = Mockito.mock(SolicitacaoAdaptacaoRepository.class);
        Mockito.when(solicitacoes.findAll()).thenReturn(List.of(solicitacao));
        AlunoRepository alunos = Mockito.mock(AlunoRepository.class);
        Mockito.when(alunos.findByAtivoTrue()).thenReturn(List.of(aluno));
        ProfissionalRepository profissionais = Mockito.mock(ProfissionalRepository.class);
        Mockito.when(profissionais.findByAtivoTrue()).thenReturn(List.of(profissional));
        AreaServicoRepository areas = Mockito.mock(AreaServicoRepository.class);
        Mockito.when(areas.findById(2L)).thenReturn(Optional.of(area));

        RelatorioService service = new RelatorioService(
                atendimentos, solicitacoes, alunos, profissionais, areas);

        RelatorioResumoResponse resumo = service.resumo(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                2L);

        assertEquals(1L, resumo.atendimentosPorStatus().get("REALIZADO"));
        assertEquals(1L, resumo.atendimentosPorStatus().get("FALTA"));
        assertEquals(1L, resumo.faltas());
        assertEquals(1L, resumo.solicitacoesPorStatus().get("DEFERIDA"));
        assertEquals(1L, resumo.totalAlunosAtivos());
        assertEquals(1L, resumo.totalProfissionaisAtivos());
        assertEquals("Psicologia", resumo.atendimentosPorArea().get(0).nome());
        assertEquals(9L, resumo.profissionais().get(0).id());
    }

    @Test
    void naoAdminOuSessaoAusenteRecebeForbiddenEmResumoECsv() {
        RelatorioService service = serviceVazio();
        for (String perfil : List.of("ALUNO", "PROFISSIONAL")) {
            SessionContext.set(10L, perfil);
            assertForbidden(() -> service.resumo(null, null, null));
            assertForbidden(() -> service.csvAtendimentos(null, null, null));
        }

        SessionContext.clear();
        assertForbidden(() -> service.resumo(null, null, null));
        assertForbidden(() -> service.csvAtendimentos(null, null, null));
    }

    @Test
    void csvContemCabecalhoAgregadoESemDadosPessoaisDeAluno() {
        SessionContext.set(1L, "ADMIN");
        Aluno aluno = new Aluno();
        aluno.setNome("Nome Privado");
        aluno.setEmail("privado@example.edu");
        aluno.setMatricula("202612345");
        Atendimento atendimento = new Atendimento();
        atendimento.setAlunoId(45L);
        atendimento.setProfissionalId(9L);
        atendimento.setAreaId(2L);
        atendimento.setDataHoraInicio(LocalDateTime.of(2026, 10, 5, 10, 0));
        atendimento.setStatus("REALIZADO");

        AtendimentoRepository atendimentos = Mockito.mock(AtendimentoRepository.class);
        Mockito.when(atendimentos.findAll()).thenReturn(List.of(atendimento));
        AreaServicoRepository areas = Mockito.mock(AreaServicoRepository.class);
        AreaServico area = new AreaServico();
        area.setId(2L);
        area.setNome("Psicologia");
        Mockito.when(areas.findById(2L)).thenReturn(Optional.of(area));
        AlunoRepository alunos = Mockito.mock(AlunoRepository.class);
        Mockito.when(alunos.findByAtivoTrue()).thenReturn(List.of(aluno));
        RelatorioService service = new RelatorioService(
                atendimentos,
                Mockito.mock(SolicitacaoAdaptacaoRepository.class),
                alunos,
                Mockito.mock(ProfissionalRepository.class),
                areas);

        String csv = service.csvAtendimentos(null, null, null);

        assertTrue(csv.startsWith("periodo;area;status;quantidade\r\n"));
        assertTrue(csv.contains("\"2026-10\";\"Psicologia\";\"REALIZADO\";1"));
        assertFalse(csv.contains(aluno.getNome()));
        assertFalse(csv.contains(aluno.getEmail()));
        assertFalse(csv.contains(aluno.getMatricula()));
    }

    private RelatorioService serviceVazio() {
        return new RelatorioService(
                Mockito.mock(AtendimentoRepository.class),
                Mockito.mock(SolicitacaoAdaptacaoRepository.class),
                Mockito.mock(AlunoRepository.class),
                Mockito.mock(ProfissionalRepository.class),
                Mockito.mock(AreaServicoRepository.class));
    }

    private void assertForbidden(OperacaoRelatorio operacao) {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class, operacao::executar);
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
    }

    @FunctionalInterface
    private interface OperacaoRelatorio {
        Object executar();
    }
}
