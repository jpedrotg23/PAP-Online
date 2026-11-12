package com.br.unifor.paponline.config;

import com.br.unifor.paponline.entidades.*;
import com.br.unifor.paponline.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final AreaServicoRepository areaRepository;
    private final AdministradorRepository administradorRepository;
    private final AlunoRepository alunoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final HorarioDisponivelRepository horarioRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final AvisoInstitucionalRepository avisoRepository;

    @Override
    public void run(String... args) {
        // Só popula se o banco estiver vazio
        if (areaRepository.count() > 0) return;

        // 1. Áreas
        AreaServico psicologia = new AreaServico();
        psicologia.setNome("PSICOLOGIA");
        psicologia.setDescricao("Apoio emocional e saúde mental");
        areaRepository.save(psicologia);

        AreaServico psicopedagogia = new AreaServico();
        psicopedagogia.setNome("PSICOPEDAGOGIA");
        psicopedagogia.setDescricao("Apoio à aprendizagem acadêmica");
        areaRepository.save(psicopedagogia);

        // 2. Aluno
        Aluno aluno = new Aluno();
        aluno.setNome("Aluno");
        aluno.setEmail("aluno@edu.unifor.br");
        aluno.setTelefone("(85) 99999-9999");
        aluno.setSenha("Senha@123");
        aluno.setMatricula("2412345");
        aluno.setCurso("Ciência da Computação");
        aluno.setVinculo("GRADUACAO");
        alunoRepository.save(aluno);

        // 3. Profissional
        Profissional prof = new Profissional();
        prof.setNome("Dra. profissional");
        prof.setEmail("profissional@unifor.br");
        prof.setTelefone("(85) 99876-5432");
        prof.setSenha("Senha@123");
        prof.setCrp("CRP 11/54321");
        prof.setCargo("Psicólogo e Psicopedagogo");
        profissionalRepository.save(prof);

        // 4. Horários disponíveis (próximos 5 dias úteis)
        LocalDate hoje = LocalDate.now();
        for (int i = 1; i <= 5; i++) {
            LocalDate dia = hoje.plusDays(i);
            for (int h = 8; h <= 16; h += 2) {
                HorarioDisponivel horario = new HorarioDisponivel();
                horario.setProfissionalId(prof.getId());
                horario.setData(dia);
                horario.setHoraInicio(LocalTime.of(h, 0));
                horario.setHoraFim(LocalTime.of(h + 1, 0));
                horario.setDisponivel(true);
                horarioRepository.save(horario);
            }
        }

        // 5. Atendimento de exemplo
        Atendimento atendimento = new Atendimento();
        atendimento.setAlunoId(aluno.getId());
        atendimento.setProfissionalId(prof.getId());
        atendimento.setDataHoraInicio(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
        atendimento.setDataHoraFim(LocalDateTime.now().plusDays(2).withHour(15).withMinute(0));
        atendimento.setModalidade("PRESENCIAL");
        atendimento.setLocal("Bloco D, sala 204");
        atendimento.setStatus("AGENDADO");
        atendimentoRepository.save(atendimento);

        // 6. Avisos
        AvisoInstitucional aviso1 = new AvisoInstitucional();
        aviso1.setTitulo("Semana de Saúde Mental Unifor");
        aviso1.setConteudo("De 12 a 16 de outubro, o PAP oferece rodas de conversa e oficinas abertas a todos os estudantes.");
        aviso1.setCategoria("EVENTO");
        aviso1.setDestaque(true);
        aviso1.setPublicoAlvo("TODOS");
        aviso1.setStatus("PUBLICADO");
        aviso1.setCriadoEm(LocalDateTime.now());
        avisoRepository.save(aviso1);

        AvisoInstitucional aviso2 = new AvisoInstitucional();
        aviso2.setTitulo("Novos horários em psicopedagogia");
        aviso2.setConteudo("Horários de outubro abertos para agendamento. Confira a disponibilidade.");
        aviso2.setCategoria("AVISO");
        aviso2.setDestaque(false);
        aviso2.setPublicoAlvo("ALUNOS");
        aviso2.setStatus("PUBLICADO");
        aviso2.setCriadoEm(LocalDateTime.now());
        avisoRepository.save(aviso2);

        //Admin
        Administrador admin = new Administrador();
        admin.setNome("Coordenação PAP");
        admin.setEmail("admin@unifor.br");
        admin.setTelefone("(85) 3477-3000");
        admin.setSenha("Senha@123");
        admin.setCargo("Coordenador(a) do PAP");
        administradorRepository.save(admin);
    }
}