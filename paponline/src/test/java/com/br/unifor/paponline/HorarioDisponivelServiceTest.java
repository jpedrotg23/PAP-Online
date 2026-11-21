package com.br.unifor.paponline;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.dto.HorarioDisponivelRequest;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.repository.HorarioDisponivelRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.service.HorarioDisponivelService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class HorarioDisponivelServiceTest {

    private final HorarioDisponivelRepository horarios = mock(HorarioDisponivelRepository.class);
    private final ProfissionalRepository profissionais = mock(ProfissionalRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final HorarioDisponivelService service =
            new HorarioDisponivelService(horarios, profissionais, entityManager);

    @AfterEach
    void clearContext() {
        SessionContext.clear();
    }

    @Test
    void rejectsOverlappingPublishedSlots() {
        SessionContext.set(20L, "PROFISSIONAL");
        when(profissionais.findById(20L)).thenReturn(Optional.of(new Profissional()));
        when(horarios.existsByProfissionalIdAndDataAndHoraInicioLessThanAndHoraFimGreaterThan(
                eq(20L), any(LocalDate.class), eq(LocalTime.of(10, 0)), eq(LocalTime.of(9, 0))))
                .thenReturn(true);
        HorarioDisponivelRequest request = new HorarioDisponivelRequest(
                999L, LocalDate.of(2026, 12, 1), LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThrows(ResponseStatusException.class, () -> service.criar(request));
        verify(horarios, never()).save(any());
    }

    @Test
    void studentCannotPublishProfessionalAvailability() {
        SessionContext.set(7L, "ALUNO");
        HorarioDisponivelRequest request = new HorarioDisponivelRequest(
                20L, LocalDate.of(2026, 12, 1), LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThrows(ResponseStatusException.class, () -> service.criar(request));
        verify(profissionais, never()).findById(any());
    }
}
