package com.br.unifor.paponline.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

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
            "/areas",
            "/relatorios"
    );
    private static final Set<String> METODOS_SEM_MUTACAO = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        boolean precisaSessao = PREFIXOS_PROTEGIDOS.stream().anyMatch(path::startsWith);

        HttpSession session = request.getSession(false);
        boolean cadastroPublicoAluno = "POST".equalsIgnoreCase(request.getMethod())
                && "/alunos".equals(path);
        if (precisaSessao && !cadastroPublicoAluno
                && (session == null || session.getAttribute("usuarioId") == null)) {
            escreverErro(response, HttpStatus.UNAUTHORIZED,
                    "Sua sessão expirou. Entre novamente no PAP Online.");
            return false;
        }

        if (session != null && session.getAttribute("usuarioId") != null) {
            Long userId = ((Number) session.getAttribute("usuarioId")).longValue();
            String perfil = (String) session.getAttribute("perfil");
            SessionContext.set(userId, perfil);
            if (!METODOS_SEM_MUTACAO.contains(request.getMethod().toUpperCase())
                    && !validarOrigem(request)) {
                SessionContext.clear();
                escreverErro(response, HttpStatus.FORBIDDEN,
                        "A origem da requisição não foi validada. Atualize a página e tente novamente.");
                return false;
            }
        }

        if (!autorizado(request, path, session)) {
            SessionContext.clear();
            escreverErro(response, HttpStatus.FORBIDDEN, "Você não tem permissão para esta operação.");
            return false;
        }

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

    private boolean autorizado(HttpServletRequest request, String path, HttpSession session) {
        if (session == null || session.getAttribute("usuarioId") == null) {
            return true;
        }
        String perfil = (String) session.getAttribute("perfil");
        String method = request.getMethod().toUpperCase();
        boolean admin = "ADMIN".equals(perfil);
        boolean profissional = "PROFISSIONAL".equals(perfil);
        boolean aluno = "ALUNO".equals(perfil);

        if (path.startsWith("/admins")) return admin;
        if (path.equals("/profissionais") && "POST".equals(method)) return admin;
        if (path.startsWith("/profissionais/")
                && !path.matches("/profissionais/\\d+/senha")
                && !Set.of("GET", "HEAD").contains(method)) {
            return admin || donoDaRota(request, session);
        }
        if (path.startsWith("/alunos/")
                && !path.matches("/alunos/\\d+/senha")
                && !Set.of("GET", "HEAD").contains(method)) {
            return admin || donoDaRota(request, session);
        }
        if (path.equals("/alunos") && "POST".equals(method)) return admin;
        if (path.equals("/alunos") && !"GET".equals(method)) {
            return admin;
        }
        if (path.equals("/alunos") && "GET".equals(method)) return admin;
        if (path.matches("/alunos/\\d+") && "GET".equals(method)) {
            return admin || profissional || donoDaRota(request, session);
        }
        if (path.startsWith("/relatorios")) return admin;
        if (path.startsWith("/horarios") && !Set.of("GET", "HEAD").contains(method)) {
            return admin || profissional;
        }
        if (path.startsWith("/areas") && !Set.of("GET", "HEAD").contains(method)) return admin;
        if (path.startsWith("/avisos") && !Set.of("GET", "HEAD").contains(method)) return admin;
        if (path.equals("/notificacoes") && "POST".equals(method)) return admin;
        if (path.startsWith("/atendimentos") && "ADMIN".equals(perfil)
                && !Set.of("GET", "HEAD").contains(method)) return false;
        return true;
    }

    private boolean donoDaRota(HttpServletRequest request, HttpSession session) {
        String[] partes = request.getRequestURI().split("/");
        if (partes.length < 3) return false;
        try {
            return Long.parseLong(partes[2])
                    == ((Number) session.getAttribute("usuarioId")).longValue();
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean validarOrigem(HttpServletRequest request) {
        if ("cross-site".equalsIgnoreCase(request.getHeader("Sec-Fetch-Site"))) return false;
        String origem = request.getHeader("Origin");
        if (origem == null || origem.isBlank()) {
            String referer = request.getHeader("Referer");
            if (referer == null || referer.isBlank()) return false;
            origem = referer;
        }
        try {
            URI uri = URI.create(origem);
            int portaOrigem = uri.getPort() < 0 ? portaPadrao(uri.getScheme()) : uri.getPort();
            int portaRequisicao = request.getServerPort();
            return uri.getHost() != null
                    && uri.getHost().equalsIgnoreCase(request.getServerName())
                    && uri.getScheme() != null
                    && uri.getScheme().equalsIgnoreCase(request.getScheme())
                    && portaOrigem == portaRequisicao;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private int portaPadrao(String esquema) {
        return "https".equalsIgnoreCase(esquema) ? 443 : 80;
    }
}