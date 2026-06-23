package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.dto.AppointmentDto;
import com.medilinktunisia.patientservice.dto.AppointmentRequest;
import com.medilinktunisia.patientservice.model.Appointment;
import com.medilinktunisia.patientservice.model.AppointmentMode;
import com.medilinktunisia.patientservice.model.AppointmentStatus;
import com.medilinktunisia.patientservice.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @InjectMocks
    private AppointmentService service;

    private final LocalDateTime now = LocalDateTime.of(2026, 6, 17, 10, 0);

    @Test
    void createAppointment_whenSuccess_shouldCreateAndReturnDto() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDateTime(now);
        request.setMode("PRESENTIEL");
        request.setNotes("Checkup");

        when(repository.existsByPatientIdAndDoctorIdAndStatusIn(
                eq(2L), eq(1L), anyList())).thenReturn(false);
        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        Appointment saved = new Appointment();
        saved.setId(10L);
        saved.setPatientId(2L);
        saved.setDoctorId(1L);
        saved.setDateTime(now);
        saved.setMode(AppointmentMode.PRESENTIEL);
        saved.setStatus(AppointmentStatus.PENDING);
        saved.setNotes("Checkup");
        saved.setCreatedAt(now);
        saved.setUpdatedAt(now);
        when(repository.save(any(Appointment.class))).thenReturn(saved);

        AppointmentDto result = service.createAppointment(2L, request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getPatientId()).isEqualTo(2L);
        assertThat(result.getDoctorId()).isEqualTo(1L);
        assertThat(result.getDateTime()).isEqualTo(now);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getMode()).isEqualTo("PRESENTIEL");
        assertThat(result.getNotes()).isEqualTo("Checkup");
    }

    @Test
    void createAppointment_whenDuplicateBooking_shouldThrowException() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDateTime(now);
        request.setMode("TELECONSULTATION");

        when(repository.existsByPatientIdAndDoctorIdAndStatusIn(
                eq(2L), eq(1L), anyList())).thenReturn(true);

        assertThatThrownBy(() -> service.createAppointment(2L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà un rendez-vous en cours");
    }

    @Test
    void createAppointment_withInvalidMode_shouldThrowException() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDateTime(now);
        request.setMode("INVALID");

        when(repository.existsByPatientIdAndDoctorIdAndStatusIn(
                eq(2L), eq(1L), anyList())).thenReturn(false);
        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.createAppointment(2L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mode de consultation invalide");

        verify(repository, never()).save(any());
    }

    @Test
    void createAppointment_shouldSaveAndReturnDto() {
        Long patientId = 1L;
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(2L);
        request.setDateTime(LocalDateTime.of(2025, 6, 20, 10, 0));
        request.setMode("PRESENTIEL");
        request.setNotes("Check-up");

        when(repository.existsByPatientIdAndDoctorIdAndStatusIn(
                eq(patientId), eq(2L), anyList())).thenReturn(false);
        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            return a;
        });

        AppointmentDto result = service.createAppointment(patientId, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getDoctorId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getMode()).isEqualTo("PRESENTIEL");
        assertThat(result.getNotes()).isEqualTo("Check-up");

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPatientId()).isEqualTo(1L);
        assertThat(captor.getValue().getMode()).isEqualTo(AppointmentMode.PRESENTIEL);
    }

    @Test
    void createAppointment_duplicate_shouldThrow() {
        Long patientId = 1L;
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(2L);
        request.setDateTime(LocalDateTime.of(2025, 6, 20, 10, 0));
        request.setMode("PRESENTIEL");

        when(repository.existsByPatientIdAndDoctorIdAndStatusIn(
                eq(patientId), eq(2L), anyList())).thenReturn(true);

        assertThatThrownBy(() -> service.createAppointment(patientId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà un rendez-vous");

        verify(repository, never()).save(any());
    }

    @Test
    void createAppointment_withInvalidMode_shouldThrow() {
        Long patientId = 1L;
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(2L);
        request.setDateTime(LocalDateTime.of(2025, 6, 20, 10, 0));
        request.setMode("INVALID");

        when(repository.existsByPatientIdAndDoctorIdAndStatusIn(
                eq(patientId), eq(2L), anyList())).thenReturn(false);
        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.createAppointment(patientId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mode de consultation invalide");

        verify(repository, never()).save(any());
    }

    @Test
    void getPatientAppointments_shouldReturnListOrderedByDateDesc() {
        Appointment a1 = new Appointment();
        a1.setId(1L);
        a1.setPatientId(2L);
        a1.setDoctorId(1L);
        a1.setDateTime(now.minusDays(1));
        a1.setMode(AppointmentMode.PRESENTIEL);
        a1.setStatus(AppointmentStatus.PENDING);
        a1.setCreatedAt(now.minusDays(1));
        a1.setUpdatedAt(now.minusDays(1));

        Appointment a2 = new Appointment();
        a2.setId(2L);
        a2.setPatientId(2L);
        a2.setDoctorId(3L);
        a2.setDateTime(now);
        a2.setMode(AppointmentMode.TELECONSULTATION);
        a2.setStatus(AppointmentStatus.CONFIRMED);
        a2.setCreatedAt(now);
        a2.setUpdatedAt(now);

        when(repository.findByPatientIdOrderByDateTimeDesc(2L))
                .thenReturn(List.of(a2, a1));

        List<AppointmentDto> result = service.getPatientAppointments(2L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }

    @Test
    void getDoctorAppointments_shouldReturnListOrderedByDateDesc() {
        Appointment a1 = new Appointment();
        a1.setId(1L);
        a1.setPatientId(2L);
        a1.setDoctorId(1L);
        a1.setDateTime(now.minusHours(2));
        a1.setMode(AppointmentMode.PRESENTIEL);
        a1.setStatus(AppointmentStatus.PENDING);
        a1.setCreatedAt(now.minusHours(2));
        a1.setUpdatedAt(now.minusHours(2));

        Appointment a2 = new Appointment();
        a2.setId(2L);
        a2.setPatientId(3L);
        a2.setDoctorId(1L);
        a2.setDateTime(now);
        a2.setMode(AppointmentMode.PRESENTIEL);
        a2.setStatus(AppointmentStatus.CONFIRMED);
        a2.setCreatedAt(now);
        a2.setUpdatedAt(now);

        when(repository.findByDoctorIdOrderByDateTimeDesc(1L))
                .thenReturn(List.of(a2, a1));

        List<AppointmentDto> result = service.getDoctorAppointments(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }

    @Test
    void cancelAppointment_whenPatientOwns_shouldCancel() {
        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatientId(2L);
        appointment.setDoctorId(1L);
        appointment.setDateTime(now);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        when(repository.findById(5L)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentDto result = service.cancelAppointment(2L, 5L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelAppointment_whenPatientDoesNotOwn_shouldThrowException() {
        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatientId(99L);
        appointment.setDoctorId(1L);
        when(repository.findById(5L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancelAppointment(2L, 5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pas autorisé");
    }

    @Test
    void getPatientAppointments_shouldReturnList() {
        Appointment a1 = new Appointment();
        a1.setId(1L);
        a1.setPatientId(1L);
        a1.setDoctorId(2L);
        a1.setStatus(AppointmentStatus.PENDING);
        a1.setMode(AppointmentMode.PRESENTIEL);

        Appointment a2 = new Appointment();
        a2.setId(2L);
        a2.setPatientId(1L);
        a2.setDoctorId(3L);
        a2.setStatus(AppointmentStatus.CONFIRMED);
        a2.setMode(AppointmentMode.TELECONSULTATION);

        when(repository.findByPatientIdOrderByDateTimeDesc(1L)).thenReturn(List.of(a1, a2));

        List<AppointmentDto> result = service.getPatientAppointments(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getDoctorAppointments_shouldReturnList() {
        Appointment a1 = new Appointment();
        a1.setId(1L);
        a1.setDoctorId(2L);
        a1.setPatientId(3L);
        a1.setStatus(AppointmentStatus.PENDING);
        a1.setMode(AppointmentMode.PRESENTIEL);

        when(repository.findByDoctorIdOrderByDateTimeDesc(2L)).thenReturn(List.of(a1));

        List<AppointmentDto> result = service.getDoctorAppointments(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctorId()).isEqualTo(2L);
    }

    @Test
    void cancelAppointment_byOwner_shouldSucceed() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatientId(1L);
        appointment.setDoctorId(2L);
        appointment.setDateTime(LocalDateTime.now());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setCreatedAt(LocalDateTime.now());

        when(repository.findById(1L)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentDto result = service.cancelAppointment(1L, 1L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(repository).save(appointment);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelAppointment_byWrongUser_shouldThrow() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatientId(1L);
        appointment.setDoctorId(2L);

        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancelAppointment(99L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pas autorisé");

        verify(repository, never()).save(any());
    }

    @Test
    void confirmAppointment_byDoctor_shouldSucceed() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatientId(1L);
        appointment.setDoctorId(2L);
        appointment.setDateTime(LocalDateTime.now());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setMode(AppointmentMode.TELECONSULTATION);
        appointment.setCreatedAt(LocalDateTime.now());

        when(repository.findById(1L)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentDto result = service.confirmAppointment(2L, 1L);

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void confirmAppointment_byWrongDoctor_shouldThrow() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctorId(2L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.confirmAppointment(99L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pas autorisé");
    }

    @Test
    void confirmAppointment_whenDoctorOwnsAndPending_shouldConfirm() {
        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatientId(2L);
        appointment.setDoctorId(1L);
        appointment.setDateTime(now);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        when(repository.findById(5L)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentDto result = service.confirmAppointment(1L, 5L);

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void confirmAppointment_whenWrongDoctor_shouldThrowException() {
        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatientId(2L);
        appointment.setDoctorId(1L);
        appointment.setDateTime(now);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        when(repository.findById(5L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.confirmAppointment(99L, 5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pas autorisé");
    }

    @Test
    void confirmAppointment_whenAlreadyCancelled_shouldThrowException() {
        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatientId(2L);
        appointment.setDoctorId(1L);
        appointment.setDateTime(now);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        when(repository.findById(5L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.confirmAppointment(1L, 5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Impossible de confirmer");
    }

    @Test
    void cancelAppointmentByDoctor_whenDoctorOwns_shouldCancel() {
        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatientId(2L);
        appointment.setDoctorId(1L);
        appointment.setDateTime(now);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        when(repository.findById(5L)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentDto result = service.cancelAppointmentByDoctor(1L, 5L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void confirmAppointment_alreadyCancelled_shouldThrow() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctorId(1L);
        appointment.setPatientId(2L);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.confirmAppointment(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("annulé");

        verify(repository, never()).save(any());
    }

    @Test
    void getActiveDoctorIdsForPatient_shouldReturnList() {
        when(repository.findActiveDoctorIdsByPatientId(
                anyLong(), anyList())).thenReturn(List.of(1L, 3L));

        List<Long> result = service.getActiveDoctorIdsForPatient(2L);

        assertThat(result).hasSize(2);
        assertThat(result).contains(1L, 3L);
    }

    @Test
    void getAvailableSlots_shouldReturnFreeSlotsExcludingBooked() {
        String date = "2026-06-17";
        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<String> slots = service.getAvailableSlots(1L, date, "09:00", "10:00", "14:00", "15:00");

        assertThat(slots).containsExactly("09:00", "09:30", "14:00", "14:30");
    }

    @Test
    void getAvailableSlots_shouldExcludeBookedSlots() {
        String date = "2026-06-17";

        Appointment booked = new Appointment();
        booked.setId(1L);
        booked.setPatientId(2L);
        booked.setDoctorId(1L);
        booked.setDateTime(LocalDateTime.parse(date + "T09:30:00"));
        booked.setMode(AppointmentMode.PRESENTIEL);
        booked.setStatus(AppointmentStatus.CONFIRMED);
        booked.setCreatedAt(now);
        booked.setUpdatedAt(now);

        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booked));

        List<String> slots = service.getAvailableSlots(1L, date, "09:00", "10:00", "14:00", "15:00");

        assertThat(slots)
                .contains("09:00", "14:00", "14:30")
                .doesNotContain("09:30");
    }

    @Test
    void validateTimeSlot_whenFree_shouldNotThrow() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 17, 10, 0);
        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.validateTimeSlot(1L, dateTime);
    }

    @Test
    void validateTimeSlot_whenOccupied_shouldThrowException() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 17, 10, 0);

        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setPatientId(3L);
        existing.setDoctorId(1L);
        existing.setDateTime(dateTime);
        existing.setMode(AppointmentMode.PRESENTIEL);
        existing.setStatus(AppointmentStatus.PENDING);
        existing.setCreatedAt(now);
        existing.setUpdatedAt(now);

        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.validateTimeSlot(1L, dateTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("créneau est déjà réservé");
    }

    @Test
    void getAvailableSlots_shouldReturnFilteredSlots() {
        Long doctorId = 1L;
        String date = "2025-06-20";
        String debutMatin = "09:00";
        String finMatin = "10:00";
        String debutApresMidi = "14:00";
        String finApresMidi = "15:00";

        Appointment booked = new Appointment();
        booked.setDateTime(LocalDateTime.parse("2025-06-20T09:30:00"));
        booked.setStatus(AppointmentStatus.CONFIRMED);

        when(repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
                eq(doctorId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booked));

        List<String> slots = service.getAvailableSlots(
                doctorId, date, debutMatin, finMatin, debutApresMidi, finApresMidi);

        assertThat(slots)
                .hasSize(3)
                .contains("09:00", "14:00", "14:30")
                .doesNotContain("09:30");
    }
}
