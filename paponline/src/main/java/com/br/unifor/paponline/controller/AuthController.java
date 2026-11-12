package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository repository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String identifier = body.get("identifier");
        String password = body.get("password");
        String requestedRole = body.get("requestedRole");

        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Informe e-mail/telefone e senha."));
        }

        Optional<Usuario> opt = repository.findByEmailOrTelefone(identifier, identifier);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciais inválidas."));
        }

        Usuario u = opt.get();

        // Senha em texto puro — para a demo. Em produção usar BCrypt.
        if (!u.getSenha().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciais inválidas."));
        }

        String tipo = detectarTipo(u);

        if (requestedRole != null && !roleBateComTipo(requestedRole, tipo)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "O perfil selecionado não corresponde à conta."));
        }

        session.setAttribute("usuarioId", u.getId());

        return ResponseEntity.ok(montarResposta(u, tipo));
    }

    @GetMapping("/session")
    public ResponseEntity<?> session(HttpSession session) {
        Object id = session.getAttribute("usuarioId");
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sessão não encontrada."));
        }
        Optional<Usuario> opt = repository.findById((Long) id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sessão inválida."));
        }
        Usuario u = opt.get();
        return ResponseEntity.ok(montarResposta(u, detectarTipo(u)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> montarResposta(Usuario u, String tipo) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", u.getId());
        resp.put("nome", u.getNome());
        resp.put("email", u.getEmail());
        resp.put("tipo", tipo);   // ALUNO | PROFISSIONAL | ADMIN
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