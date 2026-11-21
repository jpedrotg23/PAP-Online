package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.service.EmailDeliveryException;
import com.br.unifor.paponline.service.RecuperacaoSenhaService;
import com.br.unifor.paponline.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String RESPOSTA_RECUPERACAO =
            "Se o endereço estiver cadastrado, você receberá as instruções de recuperação.";

    private final UsuarioService usuarioService;
    private final RecuperacaoSenhaService recuperacaoService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String identifier = body.get("identifier");
        String password = body.get("password");
        String requestedRole = body.get("requestedRole");

        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Informe e-mail/telefone e senha."));
        }

        Optional<Usuario> opt;
        try {
            opt = usuarioService.autenticar(identifier, password);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason() == null ? "Erro" : ex.getReason()));
        }

        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciais inválidas."));
        }

        Usuario u = opt.get();
        String tipo = detectarTipo(u);

        if (requestedRole != null && !roleBateComTipo(requestedRole, tipo)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "O perfil selecionado não corresponde à conta."));
        }

        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("usuarioId", u.getId());
        session.setAttribute("perfil", tipo);

        return ResponseEntity.ok(montarResposta(u, tipo));
    }

    @GetMapping("/session")
    public ResponseEntity<?> session(HttpSession session) {
        Object id = session.getAttribute("usuarioId");
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sessão não encontrada."));
        }
        Optional<Usuario> opt;
        try {
            opt = Optional.of(usuarioService.buscar((Long) id));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sessão inválida."));
        }
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sessão inválida."));
        }
        Usuario u = opt.get();
        if (Boolean.FALSE.equals(u.getAtivo())) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Conta desativada."));
        }

        String tipo = detectarTipo(u);
        if (session.getAttribute("perfil") == null) {   // <- NOVO (defensivo)
            session.setAttribute("perfil", tipo);
        }

        return ResponseEntity.ok(montarResposta(u, tipo));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recuperar")
    public Map<String, String> recuperar(@RequestBody Map<String, String> body) {
        String identificador = body.get("identifier");
        if (identificador == null || identificador.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe e-mail ou telefone");
        }
        try {
            recuperacaoService.solicitarCodigo(identificador);
        } catch (EmailDeliveryException ex) {
            log.warn("Falha na entrega da recuperação de senha ({})", ex.getDiagnosticCode());
        }
        return Map.of("mensagem", RESPOSTA_RECUPERACAO);
    }

    @PostMapping("/validar-codigo")
    public Map<String, Object> validarCodigo(@RequestBody Map<String, String> body) {
        recuperacaoService.validarCodigo(body.get("identifier"), body.get("codigo"));
        return Map.of("valido", true);
    }

    @PostMapping("/redefinir-senha")
    public Map<String, String> redefinirSenha(@RequestBody Map<String, String> body) {
        String identificador = body.get("identifier");
        String codigo = body.get("codigo");
        String novaSenha = body.get("novaSenha");

        try {
            com.br.unifor.paponline.util.SenhaValidator.exigirValida(novaSenha);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        recuperacaoService.redefinir(identificador, codigo, novaSenha);
        return Map.of("mensagem", "Senha redefinida com sucesso");
    }

    private Map<String, Object> montarResposta(Usuario u, String tipo) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", u.getId());
        resp.put("nome", u.getNome());
        resp.put("email", u.getEmail());
        resp.put("tipo", tipo);
        return resp;
    }

    private String detectarTipo(Usuario u) {
        if (u instanceof Aluno) return "ALUNO";
        if (u instanceof Profissional) return "PROFISSIONAL";
        if (u instanceof Administrador) return "ADMIN";
        return "DESCONHECIDO";
    }

    private boolean roleBateComTipo(String role, String tipo) {
        return switch (role.toLowerCase()) {
            case "student" -> "ALUNO".equals(tipo);
            case "professional" -> "PROFISSIONAL".equals(tipo);
            case "admin" -> "ADMIN".equals(tipo);
            default -> false;
        };
    }
}