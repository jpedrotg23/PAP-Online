package com.br.unifor.paponline.config;

import com.br.unifor.paponline.entidades.*;
import com.br.unifor.paponline.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

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

    @Override
    public void run(String... args) {
        if (areaServicoRepository.count() > 0) return;

        String senhaCriptografada = passwordEncoder.encode("Senha@123");

        AreaServico psicologia = criarArea("PSICOLOGIA", "Apoio emocional e saúde mental");
        AreaServico psicopedagogia = criarArea("PSICOPEDAGOGIA", "Apoio à aprendizagem acadêmica");

        Administrador admin = new Administrador();
        admin.setNome("Coordenação PAP");
        admin.setEmail("admin@unifor.br");
        admin.setTelefone("(85) 3477-3000");
        admin.setSenha(senhaCriptografada);
        admin.setCargo("Coordenador(a) do PAP");
        admin.setAtivo(true);
        admin.setDataCadastro(LocalDateTime.now());
        administradorRepository.save(admin);

        Aluno aluno1 = criarAluno("Aluno1", "aluno@edu.unifor.br", "(85) 99999-9999",
                "2412345", "Ciência da Computação", senhaCriptografada);
        Aluno aluno2 = criarAluno("Aluno2", "aluno2@edu.unifor.br", "(85) 98888-8888",
                "2412346", "Engenharia de Software", senhaCriptografada);
        Aluno aluno3 = criarAluno("Aluno3", "aluno3@edu.unifor.br", "(85) 97777-1111",
                "2412347", "Design", senhaCriptografada);

        Profissional profissional1 = criarProfissional("Profissional1", "profissional@unifor.br",
                "(85) 99876-5432", "CRP 11/54321", "Psicóloga e Psicopedagoga",
                psicologia.getId(), senhaCriptografada);
        Profissional profissional2 = criarProfissional("Profissional2", "prof2@unifor.br",
                "(85) 97777-7777", "CRP 11/12345", "Psicopedagoga",
                psicopedagogia.getId(), senhaCriptografada);

        LocalDate hoje = LocalDate.now(FUSO_PAP);
        criarHorariosFuturos(profissional1, hoje);
        criarHorariosFuturos(profissional2, hoje);

        criarAviso("Semana de Saúde Mental Unifor",
                "De 12 a 16 de outubro, o PAP oferece rodas de conversa e oficinas abertas a todos os estudantes.",
                "EVENTO", true, "TODOS");
        criarAviso("Novos horários em psicopedagogia",
                "Horários de outubro abertos para agendamento. Confira a disponibilidade na área de agendamento.",
                "AVISO", false, "ALUNOS");

        seedAtendimentosAluno(aluno1, aluno2, profissional1, profissional2, psicologia, psicopedagogia);
        seedAtendimentosProfissional(aluno3, profissional1, psicologia);
    }

    // ---------------------------------------------------------------
    // Helpers de criação
    // ---------------------------------------------------------------

    private AreaServico criarArea(String nome, String descricao) {
        AreaServico a = new AreaServico();
        a.setNome(nome);
        a.setDescricao(descricao);
        return areaServicoRepository.save(a);
    }

    private Aluno criarAluno(String nome, String email, String telefone,
                             String matricula, String curso, String senha) {
        Aluno a = new Aluno();
        a.setNome(nome);
        a.setEmail(email);
        a.setTelefone(telefone);
        a.setSenha(senha);
        a.setMatricula(matricula);
        a.setCurso(curso);
        a.setVinculo("GRADUACAO");
        a.setAtivo(true);
        a.setDataCadastro(LocalDateTime.now());
        return alunoRepository.save(a);
    }

    private Profissional criarProfissional(String nome, String email, String telefone,
                                           String crp, String cargo, Long areaId, String senha) {
        Profissional p = new Profissional();
        p.setNome(nome);
        p.setEmail(email);
        p.setTelefone(telefone);
        p.setSenha(senha);
        p.setCrp(crp);
        p.setCargo(cargo);
        p.setAreaId(areaId);
        p.setAtivo(true);
        p.setDataCadastro(LocalDateTime.now());
        return profissionalRepository.save(p);
    }

    private void criarHorariosFuturos(Profissional prof, LocalDate hoje) {
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