package com.br.unifor.paponline;

import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.repository.AdministradorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCanRequestResumoWithIsoDateParameters() throws Exception {
        Administrador admin = new Administrador();
        admin.setNome("Administrador Relatório");
        admin.setEmail("relatorio.admin@example.edu");
        admin.setSenha(passwordEncoder.encode("Teste1234"));
        admin.setCargo("Coordenação");
        administradorRepository.save(admin);

        MvcResult login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"relatorio.admin@example.edu",
                                 "password":"Teste1234","requestedRole":"admin"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/relatorios/resumo")
                        .param("de", "2026-05-01")
                        .param("ate", "2026-05-31")
                        .session(session))
                .andExpect(status().isOk());
    }
}
