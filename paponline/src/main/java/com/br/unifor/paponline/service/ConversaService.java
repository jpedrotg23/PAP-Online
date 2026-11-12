package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.Conversa;
import com.br.unifor.paponline.entidades.Mensagem;
import com.br.unifor.paponline.repository.ConversaRepository;
import com.br.unifor.paponline.repository.MensagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversaService {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;

    public Conversa obterOuCriar(Long alunoId, Long profissionalId) {
        return conversaRepository.findByAlunoIdAndProfissionalId(alunoId, profissionalId)
                .orElseGet(() -> {
                    Conversa c = new Conversa();
                    c.setAlunoId(alunoId);
                    c.setProfissionalId(profissionalId);
                    return conversaRepository.save(c);
                });
    }

    public List<Conversa> listarPorAluno(Long alunoId) {
        return conversaRepository.findByAlunoId(alunoId);
    }

    public List<Conversa> listarPorProfissional(Long profissionalId) {
        return conversaRepository.findByProfissionalId(profissionalId);
    }

    public List<Mensagem> listarMensagens(Long conversaId) {
        return mensagemRepository.findByConversaIdOrderByEnviadaEmAsc(conversaId);
    }

    public Mensagem enviar(Long conversaId, Long remetenteId, String conteudo) {
        Mensagem m = new Mensagem();
        m.setConversaId(conversaId);
        m.setRemetenteId(remetenteId);
        m.setConteudo(conteudo);
        m.setEnviadaEm(LocalDateTime.now());
        Mensagem salva = mensagemRepository.save(m);

        conversaRepository.findById(conversaId).ifPresent(c -> {
            c.setAtualizadoEm(LocalDateTime.now());
            conversaRepository.save(c);
        });

        return salva;
    }

    public void marcarComoLida(Long mensagemId) {
        Mensagem m = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada"));
        m.setLida(true);
        mensagemRepository.save(m);
    }
}