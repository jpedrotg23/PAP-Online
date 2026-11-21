package com.br.unifor.paponline;

import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.repository.AlunoRepository;
import com.br.unifor.paponline.repository.AdministradorRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AlunoCadastroAutenticacaoIntegrationTest {

    private static final String VALID_STUDENT_BODY = """
            {
              "nome": "Estudante Teste",
              "email": "  estudante.teste@example.edu ",
              "telefone": "(85) 99999-9999",
              "senha": "Teste1234",
              "matricula": "202612345",
              "curso": "Psicologia",
              "vinculo": "ATIVO",
              "consentimentoAceito": true
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrationPersistsBcryptAndNewStudentCanLoginAndLogout() throws Exception {
        MvcResult registration = mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_STUDENT_BODY))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(registration.getResponse().getContentAsString());
        assertFalse(response.has("senha"));
        assertFalse(registration.getResponse().getContentAsString().contains("$2"));

        Aluno persisted = alunoRepository.findById(response.get("id").asLong())
                .orElseThrow();
        assertNotNull(persisted.getId());
        assertTrue(passwordEncoder.matches("Teste1234", persisted.getSenha()));
        assertNotEquals("Teste1234", persisted.getSenha());
        assertTrue(persisted.getConsentimentoAceito());
        assertNotNull(persisted.getConsentimentoEm());
        assertNotNull(persisted.getConsentimentoVersao());

        mockMvc.perform(loginRequest(
                        "estudante.teste@example.edu", "Errada1234", "student"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(loginRequest(
                        "estudante.teste@example.edu", "Teste1234", "professional"))
                .andExpect(status().isUnauthorized());

        MockHttpSession anonymousSession = new MockHttpSession();
        String oldSessionId = anonymousSession.getId();
        MvcResult successfulLogin = mockMvc.perform(loginRequest(
                        "estudante.teste@example.edu", "Teste1234", "student")
                        .session(anonymousSession))
                .andExpect(status().isOk())
                .andReturn();
        HttpSession session = successfulLogin.getRequest().getSession(false);
        assertNotNull(session);
        assertNotEquals(oldSessionId, session.getId());
        assertEquals(persisted.getId(), session.getAttribute("usuarioId"));
        assertEquals("ALUNO", session.getAttribute("perfil"));

        mockMvc.perform(get("/auth/session").session((MockHttpSession) session))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/logout")
                        .header("Origin", "http://localhost")
                        .session((MockHttpSession) session))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/auth/session").session((MockHttpSession) session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailAndMatriculaAndMissingConsentAreRejectedAsBadRequest() throws Exception {
        registerStudent(VALID_STUDENT_BODY).andExpect(status().isOk());

        String duplicateEmail = VALID_STUDENT_BODY
                .replace("202612345", "202699999");
        registerStudent(duplicateEmail).andExpect(status().isBadRequest());

        String duplicateMatricula = VALID_STUDENT_BODY
                .replace("  estudante.teste@example.edu ", "novo@example.edu");
        registerStudent(duplicateMatricula).andExpect(status().isBadRequest());

        String noConsent = VALID_STUDENT_BODY.replace("\"consentimentoAceito\": true",
                "\"consentimentoAceito\": false");
        registerStudent(noConsent).andExpect(status().isBadRequest());
    }

    @Test
    void beanAndPasswordValidationErrorsReturnBadRequest() throws Exception {
        String invalidPassword = VALID_STUDENT_BODY.replace("Teste1234", "teste1234");
        registerStudent(invalidPassword).andExpect(status().isBadRequest());

        String invalidEmail = VALID_STUDENT_BODY.replace("estudante.teste@example.edu", "email-invalido");
        registerStudent(invalidEmail).andExpect(status().isBadRequest());

        String missingConsent = VALID_STUDENT_BODY.replace(",\n  \"consentimentoAceito\": true", "");
        registerStudent(missingConsent).andExpect(status().isBadRequest());
    }

    @Test
    void professionalAndAdministratorCreationRemainProtectedByAdminAuthorization() throws Exception {
        mockMvc.perform(post("/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Profissional","email":"prof@example.edu",
                                 "telefone":"(85) 99999-9999","senha":"Teste1234",
                                 "crp":"12345","cargo":"Psicólogo","areaId":1}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Administrador","email":"admin@example.edu",
                                 "senha":"Teste1234","cargo":"Coordenação"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveStudentCannotLogin() throws Exception {
        MvcResult registration = registerStudent(VALID_STUDENT_BODY)
                .andExpect(status().isOk())
                .andReturn();
        long id = objectMapper.readTree(registration.getResponse().getContentAsString())
                .get("id").asLong();
        Aluno aluno = alunoRepository.findById(id).orElseThrow();
        aluno.setAtivo(false);
        alunoRepository.save(aluno);

        mockMvc.perform(loginRequest("estudante.teste@example.edu", "Teste1234", "student"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateProfessionalAndAdministratorThroughPasswordInputDtos() throws Exception {
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("usuarioId", 900L);
        adminSession.setAttribute("perfil", "ADMIN");

        MvcResult professionalResult = mockMvc.perform(post("/profissionais")
                        .header("Origin", "http://localhost")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Profissional Teste","email":"prof@example.edu",
                                 "telefone":"(85) 99999-9999","senha":"Teste1234",
                                 "crp":"12345","cargo":"Psicólogo","areaId":1}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode professionalJson = objectMapper.readTree(professionalResult.getResponse().getContentAsString());
        assertFalse(professionalJson.has("senha"));
        Profissional professional = profissionalRepository.findById(professionalJson.get("id").asLong())
                .orElseThrow();
        assertTrue(passwordEncoder.matches("Teste1234", professional.getSenha()));

        MvcResult administratorResult = mockMvc.perform(post("/admins")
                        .header("Origin", "http://localhost")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Administrador Teste","email":"admin@example.edu",
                                 "senha":"Teste1234","cargo":"Coordenação"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode administratorJson = objectMapper.readTree(administratorResult.getResponse().getContentAsString());
        assertFalse(administratorJson.has("senha"));
        Administrador administrator = administradorRepository.findById(administratorJson.get("id").asLong())
                .orElseThrow();
        assertTrue(passwordEncoder.matches("Teste1234", administrator.getSenha()));
    }

    private org.springframework.test.web.servlet.ResultActions registerStudent(String json) throws Exception {
        return mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private MockHttpServletRequestBuilder loginRequest(String email, String password, String requestedRole) {
        return post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, password, requestedRole));
    }

    private String loginBody(String email, String password, String requestedRole) {
        return "{\"identifier\":\"" + email + "\",\"password\":\"" + password
                + "\",\"requestedRole\":\"" + requestedRole + "\"}";
    }
}
