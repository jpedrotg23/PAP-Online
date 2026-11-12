package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.Notificacao;
import com.br.unifor.paponline.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository repository;

    public Notificacao criar(Long usuarioId, String titulo, String mensagem) {
        Notificacao n = new Notificacao();
        n.setUsuarioId(usuarioId);
        n.setTitulo(titulo);
        n.setMensagem(mensagem);
        return repository.save(n);
    }

    public List<Notificacao> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId);
    }

    public long contarNaoLidas(Long usuarioId) {
        return repository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    public void marcarComoLida(Long id) {
        Notificacao n = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        n.setLida(true);
        n.setLidaEm(LocalDateTime.now());
        repository.save(n);
    }
}