package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.AvisoInstitucional;
import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.repository.AvisoInstitucionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvisoInstitucionalService {

    private final AvisoInstitucionalRepository repository;
    private final NotificacaoService notificacaoService;

    @Transactional
    public AvisoInstitucional criar(AvisoInstitucional a) {
        exigirAdministrador();
        a.setCriadoEm(LocalDateTime.now());
        if (a.getStatus() == null) {
            a.setStatus("RASCUNHO");
        }

        boolean publicadoAgora = "PUBLICADO".equals(a.getStatus());
        if (publicadoAgora && a.getPublicadoEm() == null) {
            a.setPublicadoEm(LocalDateTime.now());
        }

        AvisoInstitucional salvo = repository.save(a);
        if (publicadoAgora) {
            notificacaoService.notificarAvisoPublicado(salvo);
        }
        return salvo;
    }

    public List<AvisoInstitucional> listar() {
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("ADMIN".equals(sessao.perfil())) return repository.findAll();
        return repository.findAll().stream()
                .filter(aviso -> "PUBLICADO".equals(aviso.getStatus()))
                .toList();
    }

    public AvisoInstitucional buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aviso não encontrado"));
    }

    @Transactional
    public AvisoInstitucional atualizar(Long id, AvisoInstitucional dados) {
        exigirAdministrador();
        AvisoInstitucional a = buscar(id);
        String statusAnterior = a.getStatus();

        a.setTitulo(dados.getTitulo());
        a.setConteudo(dados.getConteudo());
        a.setCategoria(dados.getCategoria());
        a.setDestaque(dados.getDestaque());
        a.setPublicoAlvo(dados.getPublicoAlvo());
        a.setStatus(dados.getStatus());

        boolean recemPublicado = "PUBLICADO".equals(dados.getStatus()) && !"PUBLICADO".equals(statusAnterior);
        if ("PUBLICADO".equals(dados.getStatus()) && a.getPublicadoEm() == null) {
            a.setPublicadoEm(LocalDateTime.now());
        }

        AvisoInstitucional salvo = repository.save(a);
        if (recemPublicado) {
            notificacaoService.notificarAvisoPublicado(salvo);
        }
        return salvo;
    }

    @Transactional
    public AvisoInstitucional publicar(Long id) {
        exigirAdministrador();
        AvisoInstitucional a = buscar(id);
        boolean recemPublicado = !"PUBLICADO".equals(a.getStatus());
        a.setStatus("PUBLICADO");
        if (a.getPublicadoEm() == null) {
            a.setPublicadoEm(LocalDateTime.now());
        }
        AvisoInstitucional salvo = repository.save(a);
        if (recemPublicado) {
            notificacaoService.notificarAvisoPublicado(salvo);
        }
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        exigirAdministrador();
        AvisoInstitucional a = buscar(id);
        repository.delete(a);
    }

    private void exigirAdministrador() {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente administradores podem gerenciar avisos institucionais.");
        }
    }

    private SessionContext.SessionInfo exigirSessao() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada.");
        }
        return sessao;
    }
}