package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Atendimento;
import com.br.unifor.paponline.entidades.AuditoriaAtendimento;
import com.br.unifor.paponline.repository.AuditoriaAtendimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaAtendimentoService {

    private static final ZoneId FUSO_PAP = ZoneId.of("America/Fortaleza");

    private final AuditoriaAtendimentoRepository repository;

    @Transactional
    public void registrar(Atendimento atendimento, String acao) {
        repository.save(criarRegistro(atendimento, acao));
    }

    @Transactional
    public void registrarTodos(List<Atendimento> atendimentos, String acao) {
        repository.saveAll(atendimentos.stream()
                .map(atendimento -> criarRegistro(atendimento, acao))
                .toList());
    }

    private AuditoriaAtendimento criarRegistro(Atendimento atendimento, String acao) {
        SessionContext.SessionInfo sessao = SessionContext.get();
        AuditoriaAtendimento registro = new AuditoriaAtendimento();
        registro.setAtendimentoId(atendimento == null ? null : atendimento.getId());
        registro.setUsuarioId(sessao == null ? null : sessao.userId());
        registro.setPerfil(sessao == null ? "SISTEMA" : sessao.perfil());
        registro.setAcao(acao);
        registro.setOcorridoEm(LocalDateTime.now(FUSO_PAP));
        return registro;
    }
}
