package com.br.unifor.paponline.dto;

import java.util.List;
import java.util.Map;

public record RelatorioResumoResponse(
        Map<String, Long> atendimentosPorStatus,
        List<ContagemArea> atendimentosPorArea,
        Map<String, Long> atendimentosPorPeriodo,
        long faltas,
        Map<String, Long> solicitacoesPorStatus,
        long totalAlunosAtivos,
        long totalProfissionaisAtivos,
        List<ContagemProfissional> profissionais
) {
    public record ContagemArea(Long id, String nome, long quantidade) {}

    public record ContagemProfissional(
            Long id,
            String nome,
            long quantidadeTotal,
            long faltas,
            Map<String, Long> quantidadesPorStatus
    ) {}
}
