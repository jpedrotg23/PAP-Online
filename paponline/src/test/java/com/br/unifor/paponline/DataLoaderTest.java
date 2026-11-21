package com.br.unifor.paponline;

import com.br.unifor.paponline.config.DataLoader;
import com.br.unifor.paponline.entidades.*;
import com.br.unifor.paponline.repository.*;
import com.br.unifor.paponline.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DataLoaderTest {

    private final List<AreaServico> areas = new ArrayList<>();
    private final List<Aluno> alunos = new ArrayList<>();
    private final List<Profissional> profissionais = new ArrayList<>();
    private final List<Administrador> administradores = new ArrayList<>();
    private final List<HorarioDisponivel> horarios = new ArrayList<>();
    private final List<Atendimento> atendimentos = new ArrayList<>();
    private final List<AvisoInstitucional> avisos = new ArrayList<>();
    private final AtomicLong ids = new AtomicLong();

    private UsuarioRepository usuarios;
    private AlunoRepository alunoRepository;
    private ProfissionalRepository profissionalRepository;
    private AdministradorRepository administradorRepository;
    private AreaServicoRepository areaRepository;
    private HorarioDisponivelRepository horarioRepository;
    private AtendimentoRepository atendimentoRepository;
    private AvisoInstitucionalRepository avisoRepository;
    private PasswordEncoder encoder;
    private Environment environment;
    private DataLoader loader;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        alunoRepository = mock(AlunoRepository.class);
        profissionalRepository = mock(ProfissionalRepository.class);
        administradorRepository = mock(AdministradorRepository.class);
        areaRepository = mock(AreaServicoRepository.class);
        horarioRepository = mock(HorarioDisponivelRepository.class);
        atendimentoRepository = mock(AtendimentoRepository.class);
        avisoRepository = mock(AvisoInstitucionalRepository.class);
        encoder = new BCryptPasswordEncoder(4);
        environment = mock(Environment.class);

        when(areaRepository.findAll()).thenAnswer(invocation -> areas);
        when(areaRepository.save(any(AreaServico.class))).thenAnswer(invocation -> {
            AreaServico area = invocation.getArgument(0);
            area.setId(ids.incrementAndGet());
            areas.add(area);
            return area;
        });

        when(usuarios.findByEmailIgnoreCase(anyString())).thenAnswer(invocation ->
                procurarUsuario((String) invocation.getArgument(0)));

        when(alunoRepository.findByMatricula(anyString())).thenAnswer(invocation ->
                alunos.stream()
                        .filter(aluno -> invocation.getArgument(0).equals(aluno.getMatricula()))
                        .findFirst());
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno aluno = invocation.getArgument(0);
            aluno.setId(ids.incrementAndGet());
            alunos.add(aluno);
            return aluno;
        });

        when(profissionalRepository.findByCrp(anyString())).thenAnswer(invocation ->
                profissionais.stream()
                        .filter(profissional -> invocation.getArgument(0).equals(profissional.getCrp()))
                        .findFirst());
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(invocation -> {
            Profissional profissional = invocation.getArgument(0);
            profissional.setId(ids.incrementAndGet());
            profissionais.add(profissional);
            return profissional;
        });
        when(administradorRepository.save(any(Administrador.class))).thenAnswer(invocation -> {
            Administrador administrador = invocation.getArgument(0);
            administrador.setId(ids.incrementAndGet());
            administradores.add(administrador);
            return administrador;
        });

        when(horarioRepository.findByProfissionalId(anyLong())).thenAnswer(invocation ->
                horarios.stream()
                        .filter(horario -> horario.getProfissionalId().equals(invocation.getArgument(0)))
                        .toList());
        when(horarioRepository.existsByProfissionalIdAndDataAndHoraInicioLessThanAndHoraFimGreaterThan(
                anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenAnswer(invocation -> horarios.stream().anyMatch(horario ->
                        horario.getProfissionalId().equals(invocation.getArgument(0))
                                && horario.getData().equals(invocation.getArgument(1))
                                && horario.getHoraInicio().isBefore((LocalTime) invocation.getArgument(2))
                                && horario.getHoraFim().isAfter((LocalTime) invocation.getArgument(3))));
        when(horarioRepository.save(any(HorarioDisponivel.class))).thenAnswer(invocation -> {
            HorarioDisponivel horario = invocation.getArgument(0);
            horario.setId(ids.incrementAndGet());
            horarios.add(horario);
            return horario;
        });

        when(atendimentoRepository.count()).thenAnswer(invocation -> (long) atendimentos.size());
        when(atendimentoRepository.findByAlunoId(anyLong())).thenAnswer(invocation ->
                atendimentos.stream()
                        .filter(atendimento -> atendimento.getAlunoId().equals(invocation.getArgument(0)))
                        .toList());
        when(atendimentoRepository.save(any(Atendimento.class))).thenAnswer(invocation -> {
            Atendimento atendimento = invocation.getArgument(0);
            atendimento.setId(ids.incrementAndGet());
            atendimentos.add(atendimento);
            return atendimento;
        });
        when(avisoRepository.findAll()).thenAnswer(invocation -> avisos);
        when(avisoRepository.save(any(AvisoInstitucional.class))).thenAnswer(invocation -> {
            AvisoInstitucional aviso = invocation.getArgument(0);
            aviso.setId(ids.incrementAndGet());
            avisos.add(aviso);
            return aviso;
        });

        loader = new DataLoader(usuarios, alunoRepository, profissionalRepository,
                administradorRepository, areaRepository, horarioRepository,
                atendimentoRepository, avisoRepository, encoder, environment);
        ReflectionTestUtils.setField(loader, "seedEnabled", true);
    }

    @Test
    void localSeedCreatesAreasUsersAndBcryptCredentialsThatAuthenticate() {
        allowLocalProfile();

        loader.run();

        assertEquals(2, areas.size());
        assertTrue(areas.stream().anyMatch(area -> area.getNome().equals("PSICOLOGIA")));
        assertTrue(areas.stream().anyMatch(area -> area.getNome().equals("PSICOPEDAGOGIA")));
        assertEquals(3, alunos.size());
        assertEquals(2, profissionais.size());
        assertEquals(1, administradores.size());
        assertTrue(profissionais.stream().anyMatch(p -> p.getAreaId().equals(areaId("PSICOLOGIA"))));
        assertTrue(profissionais.stream().anyMatch(p -> p.getAreaId().equals(areaId("PSICOPEDAGOGIA"))));

        List<Usuario> contas = new ArrayList<>();
        contas.addAll(alunos);
        contas.addAll(profissionais);
        contas.addAll(administradores);
        UsuarioService auth = new UsuarioService(usuarios, encoder);
        for (Usuario conta : contas) {
            assertNotNull(conta.getSenha());
            assertNotEquals("Senha@123", conta.getSenha());
            assertTrue(encoder.matches("Senha@123", conta.getSenha()));
            assertTrue(auth.autenticar(conta.getEmail(), "Senha@123").isPresent());
        }
    }

    @Test
    void runningLocalSeedTwiceDoesNotDuplicateSeedRecordsOrSchedules() {
        allowLocalProfile();

        loader.run();
        int schedulesAfterFirstRun = horarios.size();
        int appointmentsAfterFirstRun = atendimentos.size();
        int noticesAfterFirstRun = avisos.size();
        loader.run();

        assertEquals(2, areas.size());
        assertEquals(3, alunos.size());
        assertEquals(2, profissionais.size());
        assertEquals(1, administradores.size());
        assertEquals(schedulesAfterFirstRun, horarios.size());
        assertEquals(appointmentsAfterFirstRun, atendimentos.size());
        assertEquals(noticesAfterFirstRun, avisos.size());
    }

    @Test
    void existingDemoAccountsAndAreasAreNeverOverwritten() {
        allowLocalProfile();
        AreaServico existingArea = new AreaServico();
        existingArea.setId(ids.incrementAndGet());
        existingArea.setNome("PSICOLOGIA");
        existingArea.setDescricao("Descrição mantida");
        areas.add(existingArea);

        Aluno existingStudent = new Aluno();
        existingStudent.setId(ids.incrementAndGet());
        existingStudent.setNome("Nome mantido");
        existingStudent.setEmail("aluno@edu.unifor.br");
        existingStudent.setSenha(encoder.encode("SenhaPersonalizada1"));
        existingStudent.setMatricula("2412345");
        existingStudent.setCurso("Curso mantido");
        alunos.add(existingStudent);

        loader.run();

        assertEquals("Descrição mantida", areas.stream()
                .filter(area -> area.getNome().equals("PSICOLOGIA"))
                .findFirst().orElseThrow().getDescricao());
        assertEquals("Nome mantido", existingStudent.getNome());
        assertEquals("Curso mantido", existingStudent.getCurso());
        assertTrue(encoder.matches("SenhaPersonalizada1", existingStudent.getSenha()));
        assertFalse(encoder.matches("Senha@123", existingStudent.getSenha()));
        assertEquals(1, alunos.stream()
                .filter(aluno -> aluno.getEmail().equalsIgnoreCase("aluno@edu.unifor.br"))
                .count());
    }

    @Test
    void testProfileNeverSeedsEvenWhenSeedFlagIsEnabled() {
        when(environment.matchesProfiles("local")).thenReturn(false);
        when(environment.matchesProfiles("prod")).thenReturn(false);

        loader.run();

        verifyNoInteractions(usuarios, alunoRepository, profissionalRepository,
                administradorRepository, areaRepository, horarioRepository,
                atendimentoRepository, avisoRepository);
    }

    @Test
    void productionProfileNeverSeedsEvenWhenLocalIsAlsoActive() {
        when(environment.matchesProfiles("local")).thenReturn(true);
        when(environment.matchesProfiles("prod")).thenReturn(true);

        loader.run();

        verifyNoInteractions(usuarios, alunoRepository, profissionalRepository,
                administradorRepository, areaRepository, horarioRepository,
                atendimentoRepository, avisoRepository);
    }

    @Test
    void localProfileStillRequiresExplicitSeedFlag() {
        when(environment.matchesProfiles("local")).thenReturn(true);
        when(environment.matchesProfiles("prod")).thenReturn(false);
        ReflectionTestUtils.setField(loader, "seedEnabled", false);

        loader.run();

        verifyNoInteractions(usuarios, alunoRepository, profissionalRepository,
                administradorRepository, areaRepository, horarioRepository,
                atendimentoRepository, avisoRepository);
    }

    private void allowLocalProfile() {
        when(environment.matchesProfiles("local")).thenReturn(true);
        when(environment.matchesProfiles("prod")).thenReturn(false);
    }

    private Long areaId(String nome) {
        return areas.stream()
                .filter(area -> area.getNome().equals(nome))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private Optional<Usuario> procurarUsuario(String email) {
        String normalized = email.toLowerCase(Locale.ROOT);
        return java.util.stream.Stream.of(alunos, profissionais, administradores)
                .flatMap(List::stream)
                .filter(usuario -> usuario.getEmail().toLowerCase(Locale.ROOT).equals(normalized))
                .map(usuario -> (Usuario) usuario)
                .findFirst();
    }
}
