package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.dto.BloqueioRequest;
import com.br.unifor.paponline.dto.HorarioDisponivelRequest;
import com.br.unifor.paponline.entidades.HorarioDisponivel;
import com.br.unifor.paponline.repository.HorarioDisponivelRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.entidades.Profissional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HorarioDisponivelService {

    private final HorarioDisponivelRepository repository;
    private final ProfissionalRepository profissionalRepository;
    private final EntityManager entityManager;

    @Transactional
    public HorarioDisponivel criar(HorarioDisponivelRequest req) {
        validarIntervalo(req);
        Long profissionalId = resolverProfissionalId(req.profissionalId());
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Profissional não encontrado."));
        entityManager.lock(profissional, LockModeType.PESSIMISTIC_WRITE);
        validarSemSobreposicao(profissionalId, req.data(), req.horaInicio(), req.horaFim(), null);

        HorarioDisponivel h = new HorarioDisponivel();
        h.setProfissionalId(profissionalId);
        h.setData(req.data());
        h.setHoraInicio(req.horaInicio());
        h.setHoraFim(req.horaFim());
        h.setDisponivel(true);
        h.setBloqueado(false);
        return repository.save(h);
    }

    @Transactional
    public HorarioDisponivel editar(Long id, HorarioDisponivelRequest req) {
        HorarioDisponivel h = buscarParaAtualizacao(id);
        exigirDonoOuAdmin(h);
        if (Boolean.FALSE.equals(h.getDisponivel())) {
            throw erro(HttpStatus.CONFLICT,
                    "Horário já reservado. Cancele o atendimento antes de editar.");
        }
        trancarAgendaProfissional(h.getProfissionalId());
        validarIntervalo(req);
        validarSemSobreposicao(h.getProfissionalId(), req.data(), req.horaInicio(), req.horaFim(), id);
        h.setData(req.data());
        h.setHoraInicio(req.horaInicio());
        h.setHoraFim(req.horaFim());
        // profissionalId NÃO muda (evita reatribuição indevida)
        return repository.save(h);
    }

    @Transactional
    public HorarioDisponivel bloquear(Long id, BloqueioRequest req) {
        HorarioDisponivel h = buscarParaAtualizacao(id);
        exigirDonoOuAdmin(h);
        if (Boolean.FALSE.equals(h.getDisponivel())) {
            throw erro(HttpStatus.CONFLICT,
                    "Não é possível bloquear um horário já reservado.");
        }
        trancarAgendaProfissional(h.getProfissionalId());
        h.setBloqueado(true);
        h.setMotivoBloqueio(req.motivo());
        return repository.save(h);
    }

    @Transactional
    public HorarioDisponivel liberar(Long id) {
        HorarioDisponivel h = buscarParaAtualizacao(id);
        exigirDonoOuAdmin(h);
        trancarAgendaProfissional(h.getProfissionalId());
        h.setBloqueado(false);
        h.setMotivoBloqueio(null);
        return repository.save(h);
    }

    public List<HorarioDisponivel> listar() {
        return repository.findAll();
    }

    public List<HorarioDisponivel> listarPorProfissional(Long profissionalId) {
        return repository.findByProfissionalId(profissionalId);
    }

    public List<HorarioDisponivel> listarDisponiveis(Long profissionalId) {
        return repository.findByProfissionalIdAndDisponivelTrueAndBloqueadoFalse(profissionalId);
    }

    public HorarioDisponivel buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Horário não encontrado"));
    }

    @Transactional
    public void excluir(Long id) {
        HorarioDisponivel h = buscarParaAtualizacao(id);
        exigirDonoOuAdmin(h);
        if (Boolean.FALSE.equals(h.getDisponivel())) {
            throw erro(HttpStatus.CONFLICT,
                    "Horário já reservado. Cancele o atendimento antes.");
        }
        trancarAgendaProfissional(h.getProfissionalId());
        repository.delete(h);
    }

    // ===== Helpers =====

    private void validarIntervalo(HorarioDisponivelRequest req) {
        if (req.horaFim().isBefore(req.horaInicio()) || req.horaFim().equals(req.horaInicio())) {
            throw erro(HttpStatus.BAD_REQUEST, "Hora final deve ser depois da hora de início");
        }
    }

    private void validarSemSobreposicao(Long profissionalId, java.time.LocalDate data,
                                        java.time.LocalTime inicio, java.time.LocalTime fim, Long idIgnorado) {
        boolean sobrepoe = idIgnorado == null
                ? repository.existsByProfissionalIdAndDataAndHoraInicioLessThanAndHoraFimGreaterThan(
                profissionalId, data, fim, inicio)
                : repository.existsByProfissionalIdAndDataAndHoraInicioLessThanAndHoraFimGreaterThanAndIdNot(
                profissionalId, data, fim, inicio, idIgnorado);
        if (sobrepoe) {
            throw erro(HttpStatus.CONFLICT, "Já existe um horário sobreposto nesta agenda.");
        }
    }

    private HorarioDisponivel buscarParaAtualizacao(Long id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Horário não encontrado"));
    }

    private void trancarAgendaProfissional(Long profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Profissional não encontrado."));
        entityManager.lock(profissional, LockModeType.PESSIMISTIC_WRITE);
    }

    private Long resolverProfissionalId(Long profissionalIdSolicitado) {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null) {
            // Sem contexto (ex.: teste) — exige explicitamente
            if (profissionalIdSolicitado == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Profissional é obrigatório");
            }
            return profissionalIdSolicitado;
        }
        if ("ADMIN".equals(sessao.perfil())) {
            if (profissionalIdSolicitado == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Profissional é obrigatório");
            }
            return profissionalIdSolicitado;
        }
        if ("PROFISSIONAL".equals(sessao.perfil())) {
            // Força o próprio id — ignora o que veio do body
            return sessao.userId();
        }
        throw erro(HttpStatus.FORBIDDEN,
                "Somente profissionais ou administradores podem publicar horários.");
    }

    private void exigirDonoOuAdmin(HorarioDisponivel h) {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null) return;
        if ("ADMIN".equals(sessao.perfil())) return;
        if ("PROFISSIONAL".equals(sessao.perfil())
                && Objects.equals(sessao.userId(), h.getProfissionalId())) return;
        throw erro(HttpStatus.FORBIDDEN, "Você só pode gerenciar seus próprios horários.");
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}