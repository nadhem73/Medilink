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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @InjectMocks
    private AppointmentService service;

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
