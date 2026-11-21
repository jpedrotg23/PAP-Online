package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.TokenRecuperacaoSenha;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.repository.TokenRecuperacaoSenhaRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.util.SenhaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration INTERVALO_REENVIO = Duration.ofSeconds(60);
    private static final int LIMITE_DIARIO = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ZoneId FUSO_PAP = ZoneId.of("America/Fortaleza");

    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResendEmailService emailService;

    @Transactional(noRollbackFor = EmailDeliveryException.class)
    public void solicitarCodigo(String identificador) {
        Usuario usuario = localizarUsuario(identificador).orElse(null);
        if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return;
        }

        String chave = normalizarEmail(usuario.getEmail());
        LocalDateTime agora = LocalDateTime.now(FUSO_PAP);
        LocalDate hoje = agora.toLocalDate();
        if (hoje.equals(usuario.getDataSolicitacoesRecuperacao())
                && usuario.getUltimaSolicitacaoRecuperacao() != null
                && Duration.between(usuario.getUltimaSolicitacaoRecuperacao(), agora)
                .compareTo(INTERVALO_REENVIO) < 0) {
            return;
        }
        int quantidadeHoje = hoje.equals(usuario.getDataSolicitacoesRecuperacao())
                ? Optional.ofNullable(usuario.getSolicitacoesRecuperacaoHoje()).orElse(0) : 0;
        if (quantidadeHoje >= LIMITE_DIARIO) return;
        Optional<TokenRecuperacaoSenha> ultimo = tokenRepository
                .findFirstByIdentificadorOrderByCriadoEmDesc(chave);
        if (ultimo.isPresent()
                && Duration.between(ultimo.get().getCriadoEm(), agora).compareTo(INTERVALO_REENVIO) < 0) {
            return;
        }

        usuario.setDataSolicitacoesRecuperacao(hoje);
        usuario.setSolicitacoesRecuperacaoHoje(quantidadeHoje + 1);
        usuario.setUltimaSolicitacaoRecuperacao(agora);
        usuarioRepository.save(usuario);

        String codigo = String.format(java.util.Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        emailService.enviarCodigoRecuperacao(usuario.getEmail(), codigo);

        tokenRepository.findByIdentificadorAndUsadoFalse(chave).forEach(token -> {
            token.setUsado(true);
            tokenRepository.save(token);
        });

        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setIdentificador(chave);
        token.setCodigo(passwordEncoder.encode(codigo));
        token.setExpiraEm(agora.plusMinutes(15));
        token.setTentativas(0);
        token.setUsado(false);
        token.setCriadoEm(agora);
        tokenRepository.save(token);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void validarCodigo(String identificador, String codigo) {
        localizarTokenValido(identificador, codigo);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void redefinir(String identificador, String codigo, String novaSenha) {
        SenhaValidator.exigirValida(novaSenha);
        TokenRecuperacaoSenha token = localizarTokenValido(identificador, codigo);
        Usuario usuario = localizarUsuario(identificador)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido."));

        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A nova senha deve ser diferente da senha atual.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        token.setUsado(true);
        tokenRepository.save(token);
    }

    private TokenRecuperacaoSenha localizarTokenValido(String identificador, String codigo) {
        String email = localizarUsuario(identificador)
                .map(Usuario::getEmail)
                .filter(value -> !value.isBlank())
                .map(this::normalizarEmail)
                .orElse("");
        TokenRecuperacaoSenha token = tokenRepository
                .findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido."));

        if (!token.getExpiraEm().isAfter(LocalDateTime.now(FUSO_PAP))) {
            token.setUsado(true);
            tokenRepository.save(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado. Solicite um novo.");
        }
        if (token.getTentativas() >= MAX_TENTATIVAS) {
            token.setUsado(true);
            tokenRepository.save(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Muitas tentativas incorretas. Solicite um novo código.");
        }
        if (codigo == null || !codigo.matches("\\d{6}")
                || !passwordEncoder.matches(codigo, token.getCodigo())) {
            int tentativas = token.getTentativas() + 1;
            token.setTentativas(tentativas);
            if (tentativas >= MAX_TENTATIVAS) {
                token.setUsado(true);
            }
            tokenRepository.save(token);
            if (tentativas >= MAX_TENTATIVAS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Muitas tentativas incorretas. Solicite um novo código.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido.");
        }
        return token;
    }

    private Optional<Usuario> localizarUsuario(String identificador) {
        if (identificador == null || identificador.isBlank()) return Optional.empty();
        String valor = identificador.trim();
        if (valor.contains("@")) {
            return usuarioRepository.findByEmailIgnoreCase(normalizarEmail(valor));
        }
        String telefone = valor.replaceAll("\\D", "");
        if (telefone.isEmpty()) return Optional.empty();
        List<Usuario> encontrados = usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getTelefone() != null
                        && telefone.equals(usuario.getTelefone().replaceAll("\\D", "")))
                .toList();
        return encontrados.size() == 1 ? Optional.of(encontrados.get(0)) : Optional.empty();
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
