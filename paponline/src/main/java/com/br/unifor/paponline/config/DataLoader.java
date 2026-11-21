package com.br.unifor.paponline.config;

import com.br.unifor.paponline.entidades.*;
import com.br.unifor.paponline.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private static final ZoneId FUSO_PAP = ZoneId.of("America/Fortaleza");

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AdministradorRepository administradorRepository;
    private final AreaServicoRepository areaServicoRepository;
    private final HorarioDisponivelRepository horarioRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final AvisoInstitucionalRepository avisoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${pap.demo.seed:false}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled || !environment.matchesProfiles("local")
                || environment.matchesProfiles("prod")) {
            log.info("Dados de demonstração desabilitados.");
            return;
        }

        AreaServico psicologia = obterOuCriarArea("PSICOLOGIA", "Apoio emocional e saúde mental");
        AreaServico psicopedagogia = obterOuCriarArea("PSICOPEDAGOGIA", "Apoio à aprendizagem acadêmica");

        boolean criouConta = criarAdminSeAusente();
        SeedResult<Aluno> aluno1 = criarAlunoSeAusente("Aluno1", "aluno@edu.unifor.br", "(85) 99999-9999",
                "2412345", "Ciência da Computação");
        SeedResult<Aluno> aluno2 = criarAlunoSeAusente("Aluno2", "aluno2@edu.unifor.br", "(85) 98888-8888",
                "2412346", "Engenharia de Software");
        SeedResult<Aluno> aluno3 = criarAlunoSeAusente("Aluno3", "aluno3@edu.unifor.br", "(85) 97777-1111",
                "2412347", "Design");
        SeedResult<Profissional> profissional1 = criarProfissionalSeAusente("Profissional1", "profissional@unifor.br",
                "(85) 99876-5432", "CRP 11/54321", "Psicóloga e Psicopedagoga",
                psicologia.getId());
        SeedResult<Profissional> profissional2 = criarProfissionalSeAusente("Profissional2", "prof2@unifor.br",
                "(85) 97777-7777", "CRP 11/12345", "Psicopedagoga",
                psicopedagogia.getId());

        criouConta |= aluno1.criado() || aluno2.criado() || aluno3.criado()
                || profissional1.criado() || profissional2.criado();

        LocalDate hoje = LocalDate.now(FUSO_PAP);
        if (profissional1.usuario() != null) criarHorariosFuturos(profissional1.usuario(), hoje);
        if (profissional2.usuario() != null) criarHorariosFuturos(profissional2.usuario(), hoje);

        criarAviso("Semana de Saúde Mental Unifor",
                "De 12 a 16 de outubro, o PAP oferece rodas de conversa e oficinas abertas a todos os estudantes.",
                "EVENTO", true, "TODOS");
        criarAviso("Novos horários em psicopedagogia",
                "Horários de outubro abertos para agendamento. Confira a disponibilidade na área de agendamento.",
                "AVISO", false, "ALUNOS");

        if (criouConta && atendimentoRepository.count() == 0
                && aluno1.usuario() != null && aluno2.usuario() != null && aluno3.usuario() != null
                && profissional1.usuario() != null && profissional2.usuario() != null) {
            seedAtendimentosAluno(aluno1.usuario(), aluno2.usuario(), profissional1.usuario(),
                    profissional2.usuario(), psicologia, psicopedagogia);
            seedAtendimentosProfissional(aluno3.usuario(), profissional1.usuario(), psicologia);
        }
    }

    // ---------------------------------------------------------------
    // Helpers de criação
    // ---------------------------------------------------------------

    private AreaServico obterOuCriarArea(String nome, String descricao) {
        AreaServico existente = areaServicoRepository.findAll().stream()
                .filter(area -> nome.equalsIgnoreCase(area.getNome()))
                .findFirst()
                .orElse(null);
        if (existente != null) return existente;

        AreaServico a = new AreaServico();
        a.setNome(nome);
        a.setDescricao(descricao);
        return areaServicoRepository.save(a);
    }

    private boolean criarAdminSeAusente() {
        var existente = usuarioRepository.findByEmailIgnoreCase("admin@unifor.br");
        if (existente.isPresent()) {
            if (!(existente.get() instanceof Administrador)) {
                log.warn("Conta de demonstração admin existente com tipo incompatível; preservada.");
            }
            return false;
        }

        Administrador admin = new Administrador();
        admin.setNome("Coordenação PAP");
        admin.setEmail("admin@unifor.br");
        admin.setTelefone("(85) 3477-3000");
        admin.setSenha(passwordEncoder.encode("Senha@123"));
        admin.setCargo("Coordenador(a) do PAP");
        admin.setAtivo(true);
        admin.setDataCadastro(LocalDateTime.now());
        administradorRepository.save(admin);
        return true;
    }

    private SeedResult<Aluno> criarAlunoSeAusente(String nome, String email, String telefone,
                                                   String matricula, String curso) {
        var existentePorEmail = usuarioRepository.findByEmailIgnoreCase(email);
        if (existentePorEmail.isPresent()) {
            if (existentePorEmail.get() instanceof Aluno aluno) {
                return new SeedResult<>(aluno, false);
            }
            log.warn("Conta de demonstração de aluno existente com tipo incompatível; preservada.");
            return new SeedResult<>(null, false);
        }
        var existentePorMatricula = alunoRepository.findByMatricula(matricula);
        if (existentePorMatricula.isPresent()) {
            return new SeedResult<>(existentePorMatricula.get(), false);
        }

        Aluno a = new Aluno();
        a.setNome(nome);
        a.setEmail(email);
        a.setTelefone(telefone);
        a.setSenha(passwordEncoder.encode("Senha@123"));
        a.setMatricula(matricula);
        a.setCurso(curso);
        a.setVinculo("GRADUACAO");
        a.setAtivo(true);
        a.setDataCadastro(LocalDateTime.now());
        Aluno criado = alunoRepository.save(a);
        return new SeedResult<>(criado, true);
    }

    private SeedResult<Profissional> criarProfissionalSeAusente(String nome, String email, String telefone,
                                                                 String crp, String cargo, Long areaId) {
        var existentePorEmail = usuarioRepository.findByEmailIgnoreCase(email);
        if (existentePorEmail.isPresent()) {
            if (existentePorEmail.get() instanceof Profissional profissional) {
                return new SeedResult<>(profissional, false);
            }
            log.warn("Conta de demonstração profissional existente com tipo incompatível; preservada.");
            return new SeedResult<>(null, false);
        }
        var existentePorCrp = profissionalRepository.findByCrp(crp);
        if (existentePorCrp.isPresent()) {
            return new SeedResult<>(existentePorCrp.get(), false);
        }

        Profissional p = new Profissional();
        p.setNome(nome);
        p.setEmail(email);
        p.setTelefone(telefone);
        p.setSenha(passwordEncoder.encode("Senha@123"));
        p.setCrp(crp);
        p.setCargo(cargo);
        p.setAreaId(areaId);
        p.setAtivo(true);
        p.setDataCadastro(LocalDateTime.now());
        Profissional criado = profissionalRepository.save(p);
        return new SeedResult<>(criado, true);
    }

    private record SeedResult<T>(T usuario, boolean criado) {
    }

    private void criarHorariosFuturos(Profissional prof, LocalDate hoje) {
        if (!horarioRepository.findByProfissionalId(prof.getId()).isEmpty()) return;

        for (int i = 1; i <= 5; i++) {
            LocalDate dia = hoje.plusDays(i);
            for (int h = 8; h <= 16; h += 2) {
                HorarioDisponivel horario = new HorarioDisponivel();
                horario.setProfissionalId(prof.getId());
                horario.setData(dia);
                horario.setHoraInicio(LocalTime.of(h, 0));
                horario.setHoraFim(LocalTime.of(h + 1, 0));
                horario.setDisponivel(true);
                horario.setBloqueado(false);
                horarioRepository.save(horario);
            }
        }
    }

    private void criarAviso(String titulo, String conteudo, String categoria,
                            boolean destaque, String publicoAlvo) {
        boolean existe = avisoRepository.findAll().stream()
                .anyMatch(aviso -> Objects.equals(titulo, aviso.getTitulo()));
        if (existe) return;

        AvisoInstitucional a = new AvisoInstitucional();
        a.setTitulo(titulo);
        a.setConteudo(conteudo);
        a.setCategoria(categoria);
        a.setDestaque(destaque);
        a.setPublicoAlvo(publicoAlvo);
        a.setStatus("PUBLICADO");
        a.setPublicadoEm(LocalDateTime.now());
        a.setCriadoEm(LocalDateTime.now());
        avisoRepository.save(a);
    }

    // ---------------------------------------------------------------
    // Seeds
    // ---------------------------------------------------------------

    private void seedAtendimentosAluno(Aluno aluno1, Aluno aluno2, Profissional profissional1,
                                       Profissional profissional2, AreaServico psicologia,
                                       AreaServico psicopedagogia) {
        LocalDateTime agora = LocalDateTime.now(FUSO_PAP).withMinute(0).withSecond(0).withNano(0);

        criarAtendimentoSemHorario(aluno1, profissional1, psicologia,
                agora.minusDays(5), agora.minusDays(5).plusHours(1), "REALIZADO");
        criarAtendimentoSemHorario(aluno1, profissional1, psicologia,
                agora.minusDays(7), agora.minusDays(7).plusHours(1), "FALTA");
        criarAtendimentoSemHorario(aluno1, profissional1, psicologia,
                agora.minusDays(9), agora.minusDays(9).plusHours(1), "CANCELADO");

        criarAtendimentoComHorario(aluno2, profissional1, psicologia,
                agora.minusHours(1), agora.minusHours(1).plusHours(1), "AGENDADO");
        criarAtendimentoComHorario(aluno2, profissional2, psicopedagogia,
                agora.minusHours(2), agora.minusHours(2).plusHours(1), "AGENDADO");
        criarAtendimentoComHorario(aluno2, profissional1, psicologia,
                agora.minusHours(25), agora.minusHours(25).plusHours(1), "AGENDADO");

        criarAtendimentoSemHorario(aluno2, profissional1, psicologia,
                agora.minusDays(10), agora.minusDays(10).plusHours(1), "FALTA");
        criarAtendimentoSemHorario(aluno2, profissional1, psicologia,
                agora.minusDays(12), agora.minusDays(12).plusHours(1), "FALTA");
        criarAtendimentoSemHorario(aluno2, profissional1, psicologia,
                agora.minusDays(14), agora.minusDays(14).plusHours(1), "FALTA");
    }

    private void seedAtendimentosProfissional(Aluno aluno3, Profissional profissional1,
                                              AreaServico psicologia) {
        LocalDate hoje = LocalDate.now(FUSO_PAP);
        int horaReferencia = Math.max(8, Math.min(20, LocalDateTime.now(FUSO_PAP).getHour()));
        LocalDateTime referencia = hoje.atTime(horaReferencia, 0);

        criarAtendimentoComHorario(aluno3, profissional1, psicologia,
                referencia.minusHours(1), referencia, "AGENDADO");
        criarAtendimentoComHorario(aluno3, profissional1, psicologia,
                referencia.minusHours(3), referencia.minusHours(2), "AGENDADO");
        criarAtendimentoComHorario(aluno3, profissional1, psicologia,
                referencia.minusHours(5), referencia.minusHours(4), "AGENDADO");
        criarAtendimentoComHorario(aluno3, profissional1, psicologia,
                hoje.plusDays(1).atTime(14, 0), hoje.plusDays(1).atTime(15, 0), "AGENDADO");
        criarAtendimentoComHorario(aluno3, profissional1, psicologia,
                hoje.plusDays(2).atTime(10, 0), hoje.plusDays(2).atTime(11, 0), "AGENDADO");
        criarAtendimentoComHorario(aluno3, profissional1, psicologia,
                referencia.minusHours(25), referencia.minusHours(24), "AGENDADO");
        criarAtendimentoComHorario(aluno3, profissional1, psicologia,
                referencia.minusDays(2), referencia.minusDays(2).plusHours(1), "REALIZADO");
    }

    // ---------------------------------------------------------------
    // Criação de atendimentos
    // ---------------------------------------------------------------

    private Atendimento criarAtendimentoComHorario(Aluno aluno, Profissional prof,
                                                   AreaServico area, LocalDateTime inicio,
                                                   LocalDateTime fim, String status) {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setProfissionalId(prof.getId());
        horario.setData(inicio.toLocalDate());
        horario.setHoraInicio(inicio.toLocalTime());
        horario.setHoraFim(fim.toLocalTime());
        horario.setDisponivel(false);
        horario.setBloqueado(false);
        horarioRepository.save(horario);

        Atendimento a = new Atendimento();
        a.setAlunoId(aluno.getId());
        a.setProfissionalId(prof.getId());
        a.setAreaId(area.getId());
        a.setHorarioId(horario.getId());
        a.setDataHoraInicio(inicio);
        a.setDataHoraFim(fim);
        a.setModalidade("PRESENCIAL");
        a.setLocal("Bloco D · Sala 204");
        a.setStatus(status);
        a.setDataCriacao(LocalDateTime.now());
        return atendimentoRepository.save(a);
    }

    private Atendimento criarAtendimentoSemHorario(Aluno aluno, Profissional prof,
                                                   AreaServico area, LocalDateTime inicio,
                                                   LocalDateTime fim, String status) {
        Atendimento a = new Atendimento();
        a.setAlunoId(aluno.getId());
        a.setProfissionalId(prof.getId());
        a.setAreaId(area.getId());
        a.setHorarioId(null);
        a.setDataHoraInicio(inicio);
        a.setDataHoraFim(fim);
        a.setModalidade("PRESENCIAL");
        a.setLocal("Bloco D · Sala 204");
        a.setStatus(status);
        a.setDataCriacao(LocalDateTime.now());
        return atendimentoRepository.save(a);
    }
}