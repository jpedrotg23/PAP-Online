package com.br.unifor.paponline.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    /**
     * Apenas rotas que começam com esses prefixos exigem sessão autenticada.
     * Tudo o mais (/, /index.html, /login.html, /css/**, /js/**, /aluno/*.html,
     * /profissional/*.html, /auth/** e o H2 console) passa sem verificação.
     */
    private static final List<String> PREFIXOS_PROTEGIDOS = List.of(
            "/alunos",
            "/profissionais",
            "/admins",
            "/atendimentos",
            "/horarios",
            "/solicitacoes",
            "/avisos",
            "/conversas",
            "/notificacoes",
            "/areas"
    );

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        boolean precisaSessao = PREFIXOS_PROTEGIDOS.stream().anyMatch(path::startsWith);
        if (!precisaSessao) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            escreverErro(response, HttpStatus.UNAUTHORIZED,
                    "Sua sessão expirou. Entre novamente no PAP Online.");
            return false;
        }

        Long userId = ((Number) session.getAttribute("usuarioId")).longValue();
        String perfil = (String) session.getAttribute("perfil");
        SessionContext.set(userId, perfil);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        SessionContext.clear();
    }

    private void escreverErro(HttpServletResponse response,
                              HttpStatus status,
                              String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + mensagem + "\"}");
    }
}