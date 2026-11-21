package com.br.unifor.paponline;

import com.br.unifor.paponline.config.DataLoader;
import com.br.unifor.paponline.repository.*;
import com.br.unifor.paponline.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:paponline-demo-seed-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "pap.demo.seed=true"
})
@ActiveProfiles("local")
class DataLoaderIntegrationTest {

    @Autowired
    private DataLoader dataLoader;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private AreaServicoRepository areaServicoRepository;

    @Autowired
    private HorarioDisponivelRepository horarioRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AvisoInstitucionalRepository avisoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void localProfileSeedPersistsAuthenticatableAccountsAndAreas() {
        assertEquals(6, usuarioRepository.count());
        assertEquals(3, alunoRepository.count());
        assertEquals(2, profissionalRepository.count());
        assertEquals(1, administradorRepository.count());
        assertEquals(2, areaServicoRepository.count());
        assertTrue(areaServicoRepository.findAll().stream()
                .anyMatch(area -> area.getNome().equals("PSICOLOGIA")));
        assertTrue(areaServicoRepository.findAll().stream()
                .anyMatch(area -> area.getNome().equals("PSICOPEDAGOGIA")));

        assertTrue(usuarioService.autenticar("admin@unifor.br", "Senha@123").isPresent());
        assertTrue(usuarioService.autenticar("aluno@edu.unifor.br", "Senha@123").isPresent());
        assertTrue(usuarioService.autenticar("aluno2@edu.unifor.br", "Senha@123").isPresent());
        assertTrue(usuarioService.autenticar("aluno3@edu.unifor.br", "Senha@123").isPresent());
        assertTrue(usuarioService.autenticar("profissional@unifor.br", "Senha@123").isPresent());
        assertTrue(usuarioService.autenticar("prof2@unifor.br", "Senha@123").isPresent());
    }

    @Test
    void rerunningSeedDoesNotDuplicatePersistedDemoData() {
        long users = usuarioRepository.count();
        long areas = areaServicoRepository.count();
        long students = alunoRepository.count();
        long professionals = profissionalRepository.count();
        long administrators = administradorRepository.count();
        long schedules = horarioRepository.count();
        long appointments = atendimentoRepository.count();
        long notices = avisoRepository.count();

        dataLoader.run();

        assertEquals(users, usuarioRepository.count());
        assertEquals(areas, areaServicoRepository.count());
        assertEquals(students, alunoRepository.count());
        assertEquals(professionals, profissionalRepository.count());
        assertEquals(administrators, administradorRepository.count());
        assertEquals(schedules, horarioRepository.count());
        assertEquals(appointments, atendimentoRepository.count());
        assertEquals(notices, avisoRepository.count());
    }
}
