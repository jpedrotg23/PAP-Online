package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.dto.RelatorioResumoResponse;
import com.br.unifor.paponline.entidades.AreaServico;
import com.br.unifor.paponline.entidades.Atendimento;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.repository.AlunoRepository;
import com.br.unifor.paponline.repository.AreaServicoRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.repository.SolicitacaoAdaptacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final AtendimentoRepository atendimentoRepository;
    private final SolicitacaoAdaptacaoRepository solicitacaoRepository;
    private final AlunoRepository alunoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AreaServicoRepository areaRepository;

    public RelatorioResumoResponse resumo(LocalDate de, LocalDate ate, Long areaId) {
        exigirAdministrador();
        List<Atendimento> atendimentos = filtrarAtendimentos(de, ate, areaId);
        List<SolicitacaoAdaptacao> solicitacoes = filtrarSolicitacoes(de, ate, areaId);

        Map<String, Long> porStatus = new LinkedHashMap<>();
        Map<String, Long> porPeriodo = new LinkedHashMap<>();
        Map<Long, Long> porArea = new LinkedHashMap<>();
        Map<Long, Map<String, Long>> porProfissional = new LinkedHashMap<>();
        long faltas = 0;

        for (Atendimento atendimento : atendimentos) {
            String status = atendimento.getStatus() == null ? "SEM_STATUS" : atendimento.getStatus();
            porStatus.merge(status, 1L, Long::sum);
            if ("FALTA".equals(status)) faltas++;

            if (atendimento.getDataHoraInicio() != null) {
                String periodo = atendimento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                porPeriodo.merge(periodo, 1L, Long::sum);
            }
            if (atendimento.getAreaId() != null) {
                porArea.merge(atendimento.getAreaId(), 1L, Long::sum);
            }
            if (atendimento.getProfissionalId() != null) {
                porProfissional.computeIfAbsent(atendimento.getProfissionalId(), id -> new LinkedHashMap<>())
                        .merge(status, 1L, Long::sum);
            }
        }

        Map<String, Long> solicitacoesPorStatus = new LinkedHashMap<>();
        for (SolicitacaoAdaptacao solicitacao : solicitacoes) {
            String status = solicitacao.getStatus() == null ? "SEM_STATUS" : solicitacao.getStatus();
            solicitacoesPorStatus.merge(status, 1L, Long::sum);
        }

        List<RelatorioResumoResponse.ContagemArea> contagemAreas = porArea.entrySet().stream()
                .map(entry -> {
                    String nome = areaRepository.findById(entry.getKey())
                            .map(AreaServico::getNome)
                            .orElse("Área não informada");
                    return new RelatorioResumoResponse.ContagemArea(entry.getKey(), nome, entry.getValue());
                })
                .toList();

        List<RelatorioResumoResponse.ContagemProfissional> profissionais = profissionalRepository.findByAtivoTrue()
                .stream()
                .filter(p -> porProfissional.containsKey(p.getId()))
                .map(p -> {
                    Map<String, Long> status = porProfissional.get(p.getId());
                    long total = status.values().stream().mapToLong(Long::longValue).sum();
                    long faltasProfissional = status.getOrDefault("FALTA", 0L);
                    return new RelatorioResumoResponse.ContagemProfissional(
                            p.getId(), p.getNome(), total, faltasProfissional, status);
                })
                .toList();

        return new RelatorioResumoResponse(
                porStatus,
                contagemAreas,
                porPeriodo,
                faltas,
                solicitacoesPorStatus,
                alunoRepository.findByAtivoTrue().size(),
                profissionalRepository.findByAtivoTrue().size(),
                profissionais
        );
    }

    public String csvAtendimentos(LocalDate de, LocalDate ate, Long areaId) {
        exigirAdministrador();
        Map<String, Map<Long, Map<String, Long>>> agrupado = new LinkedHashMap<>();
        for (Atendimento atendimento : filtrarAtendimentos(de, ate, areaId)) {
            String periodo = atendimento.getDataHoraInicio() == null
                    ? "SEM_PERIODO"
                    : atendimento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Long area = atendimento.getAreaId();
            String status = atendimento.getStatus() == null ? "SEM_STATUS" : atendimento.getStatus();
            agrupado.computeIfAbsent(periodo, key -> new LinkedHashMap<>())
                    .computeIfAbsent(area == null ? -1L : area, key -> new LinkedHashMap<>())
                    .merge(status, 1L, Long::sum);
        }

        StringBuilder csv = new StringBuilder("periodo;area;status;quantidade\r\n");
        for (var periodoEntry : agrupado.entrySet()) {
            for (var areaEntry : periodoEntry.getValue().entrySet()) {
                String areaNome = areaEntry.getKey() == -1L
                        ? "SEM_AREA"
                        : areaRepository.findById(areaEntry.getKey()).map(AreaServico::getNome).orElse("SEM_AREA");
                for (var statusEntry : areaEntry.getValue().entrySet()) {
                    csv.append(escapar(periodoEntry.getKey())).append(';')
                            .append(escapar(areaNome)).append(';')
                            .append(escapar(statusEntry.getKey())).append(';')
                            .append(statusEntry.getValue()).append("\r\n");
                }
            }
        }
        return csv.toString();
    }

    private List<Atendimento> filtrarAtendimentos(LocalDate de, LocalDate ate, Long areaId) {
        LocalDateTime inicio = de == null ? null : de.atStartOfDay();
        LocalDateTime fim = ate == null ? null : ate.atTime(LocalTime.MAX);
        return atendimentoRepository.findAll().stream()
                .filter(a -> areaId == null || Objects.equals(a.getAreaId(), areaId))
                .filter(a -> inicio == null || (a.getDataHoraInicio() != null && !a.getDataHoraInicio().isBefore(inicio)))
                .filter(a -> fim == null || (a.getDataHoraInicio() != null && !a.getDataHoraInicio().isAfter(fim)))
                .toList();
    }

    private List<SolicitacaoAdaptacao> filtrarSolicitacoes(LocalDate de, LocalDate ate, Long areaId) {
        LocalDateTime inicio = de == null ? null : de.atStartOfDay();
        LocalDateTime fim = ate == null ? null : ate.atTime(LocalTime.MAX);
        return solicitacaoRepository.findAll().stream()
                .filter(s -> areaId == null || Objects.equals(s.getAreaId(), areaId))
                .filter(s -> inicio == null || (s.getDataEnvio() != null && !s.getDataEnvio().isBefore(inicio)))
                .filter(s -> fim == null || (s.getDataEnvio() != null && !s.getDataEnvio().isAfter(fim)))
                .toList();
    }

    private void exigirAdministrador() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || !"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente administradores podem consultar relatórios.");
        }
    }

    private String escapar(String valor) {
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}
