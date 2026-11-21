package com.br.unifor.paponline;

import com.br.unifor.paponline.config.AuthorizationInterceptor;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationInterceptorTest {

    private final AuthorizationInterceptor interceptor = new AuthorizationInterceptor();

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
