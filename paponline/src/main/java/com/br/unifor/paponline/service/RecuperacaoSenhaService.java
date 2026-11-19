package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.TokenRecuperacaoSenha;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.repository.TokenRecuperacaoSenhaRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.util.SenhaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;                       // ← import novo
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j                                                   // ← anotação nova
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public String solicitarCodigo(String identificador) {
        Optional<Usuario> opt = usuarioRepository.findByEmailOrTelefone(identificador, identificador);
        if (opt.isEmpty()) {
            return null;  // resposta genérica, sem revelar existência
        }

        String codigo = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setIdentificador(identificador);
        token.setCodigo(codigo);
        token.setExpiraEm(LocalDateTime.now().plusMinutes(15));
        token.setTentativas(0);
        tokenRepository.save(token);

        // ⬇⬇ NOVO: código aparece nos logs do servidor (console local e Render)
        log.info("[RECUPERACAO] Código para {}: {}", identificador, codigo);

        // TODO em produção: enviar por e-mail/SMS. Nada de System.out.
        return codigo;
    }

    public void validarCodigo(String identificador, String codigo) {
        TokenRecuperacaoSenha token = tokenRepository
                .findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc(identificador)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido"));

        if (token.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado. Solicite um novo.");
        }

        if (!token.getCodigo().equals(codigo)) {
            int t = token.getTentativas() + 1;
            token.setTentativas(t);
            if (t >= 5) {
                token.setUsado(true);
                tokenRepository.save(token);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Muitas tentativas incorretas. Solicite um novo código.");
            }
            tokenRepository.save(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Código inválido. Tentativas restantes: " + (5 - t));
        }
    }

    public void redefinir(String identificador, String codigo, String novaSenha) {
        SenhaValidator.exigirValida(novaSenha);
        validarCodigo(identificador, codigo);

        Usuario usuario = usuarioRepository.findByEmailOrTelefone(identificador, identificador)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        tokenRepository
                .findFirstByIdentificadorAndCodigoAndUsadoFalseOrderByCriadoEmDesc(identificador, codigo)
                .ifPresent(t -> {
                    t.setUsado(true);
                    tokenRepository.save(t);
                });
    }
}