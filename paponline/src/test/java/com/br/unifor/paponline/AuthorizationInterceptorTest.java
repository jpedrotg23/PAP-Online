package com.br.unifor.paponline;

import com.br.unifor.paponline.config.AuthorizationInterceptor;
import com.br.unifor.paponline.config.SessionContext;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationInterceptorTest {

    private final AuthorizationInterceptor interceptor = new AuthorizationInterceptor();

    @AfterEach
    void clearSessionContext() {
        SessionContext.clear();
    }

    @Test
    void reportEndpointsOnlyAllowAdminAndRequireSession() throws Exception {
        for (String path : new String[]{"/relatorios/resumo", "/relatorios/atendimentos.csv"}) {
            assertAccess(path, "ADMIN", true, 200);
            assertAccess(path, "ALUNO", false, 403);
            assertAccess(path, "PROFISSIONAL", false, 403);

            MockHttpServletRequest anonymous = new MockHttpServletRequest("GET", path);
            MockHttpServletResponse anonymousResponse = new MockHttpServletResponse();
            assertFalse(interceptor.preHandle(anonymous, anonymousResponse, new Object()));
            assertEquals(401, anonymousResponse.getStatus());
        }
    }

    private void assertAccess(String path, String perfil, boolean permitido, int status) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        HttpSession session = request.getSession();
        session.setAttribute("usuarioId", 1L);
        session.setAttribute("perfil", perfil);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertEquals(permitido, interceptor.preHandle(request, response, new Object()), perfil + " em " + path);
        assertEquals(status, response.getStatus());
    }

    @Test
    void professionalCanReadOneStudentButNotListStudents() throws Exception {
        MockHttpServletRequest individual = new MockHttpServletRequest("GET", "/alunos/15");
        HttpSession professionalSession = individual.getSession();
        professionalSession.setAttribute("usuarioId", 7L);
        professionalSession.setAttribute("perfil", "PROFISSIONAL");
        MockHttpServletResponse individualResponse = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(individual, individualResponse, new Object()));

        MockHttpServletRequest lista = new MockHttpServletRequest("GET", "/alunos");
        HttpSession session = lista.getSession();
        session.setAttribute("usuarioId", 7L);
        session.setAttribute("perfil", "PROFISSIONAL");
        MockHttpServletResponse listaResponse = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(lista, listaResponse, new Object()));
        assertEquals(403, listaResponse.getStatus());
    }

    @Test
    void studentCannotReadAnotherStudentById() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/alunos/15");
        HttpSession session = request.getSession();
        session.setAttribute("usuarioId", 42L);
        session.setAttribute("perfil", "ALUNO");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    void professionalSelfRegistrationIsNoLongerPublic() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/profissionais");
        request.setServerPort(8080);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void publicStudentRegistrationRemainsAvailable() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/alunos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void studentCannotCreateProfessionalOrCrossSiteMutate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/profissionais");
        request.setServerPort(8080);
        HttpSession session = request.getSession();
        session.setAttribute("usuarioId", 42L);
        session.setAttribute("perfil", "ALUNO");
        request.addHeader("Origin", "http://localhost:8080");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());

        MockHttpServletRequest crossSite = new MockHttpServletRequest("POST", "/avisos");
        HttpSession adminSession = crossSite.getSession();
        adminSession.setAttribute("usuarioId", 1L);
        adminSession.setAttribute("perfil", "ADMIN");
        crossSite.addHeader("Origin", "https://attacker.example");
        MockHttpServletResponse crossSiteResponse = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(crossSite, crossSiteResponse, new Object()));
        assertEquals(403, crossSiteResponse.getStatus());
    }
}
