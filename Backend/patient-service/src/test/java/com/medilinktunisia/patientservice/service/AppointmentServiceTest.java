package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.dto.AppointmentRequest;
import com.medilinktunisia.patientservice.model.Appointment;
import com.medilinktunisia.patientservice.model.AppointmentMode;
import com.medilinktunisia.patientservice.model.AppointmentStatus;
import com.medilinktunisia.patientservice.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository);
    }

    @Test
    void createAppointment_shouldReturnAppointmentDto() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDateTime(LocalDateTime.now().plusDays(1));
        request.setMode("PRESENTIEL");

        when(appointmentRepository.existsByPatientIdAndDoctorIdAndStatusIn(
                anyLong(), anyLong(), anyList())).thenReturn(false);

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        var result = appointmentService.createAppointment(2L, request);

        assertNotNull(result);
        assertEquals(1L, result.getDoctorId());
        assertEquals("PENDING", result.getStatus());
        assertEquals("PRESENTIEL", result.getMode());

        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void createAppointment_withExistingBooking_shouldThrow() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDateTime(LocalDateTime.now().plusDays(1));
        request.setMode("PRESENTIEL");

        when(appointmentRepository.existsByPatientIdAndDoctorIdAndStatusIn(
                anyLong(), anyLong(), anyList())).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> appointmentService.createAppointment(2L, request));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_withInvalidMode_shouldThrow() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setDateTime(LocalDateTime.now().plusDays(1));
        request.setMode("INVALID");

        when(appointmentRepository.existsByPatientIdAndDoctorIdAndStatusIn(
                anyLong(), anyLong(), anyList())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.createAppointment(2L, request));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void getPatientAppointments_shouldReturnList() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatientId(2L);
        appointment.setDoctorId(1L);
        appointment.setDateTime(LocalDateTime.now());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setMode(AppointmentMode.PRESENTIEL);

        when(appointmentRepository.findByPatientIdOrderByDateTimeDesc(anyLong()))
                .thenReturn(List.of(appointment));

        var result = appointmentService.getPatientAppointments(2L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void cancelAppointment_shouldCancelSuccessfully() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatientId(2L);
        appointment.setDoctorId(1L);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = appointmentService.cancelAppointment(2L, 1L);

        assertEquals("CANCELLED", result.getStatus());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    void cancelAppointmentByDoctor_shouldCancel() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctorId(1L);
        appointment.setPatientId(2L);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setDoctorId(1L);
        appointment.setMode(AppointmentMode.PRESENTIEL);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.cancelAppointment(99L, 1L));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void confirmAppointment_shouldConfirmSuccessfully() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctorId(1L);
        appointment.setPatientId(2L);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = appointmentService.confirmAppointment(1L, 1L);

        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    void confirmAppointment_alreadyCancelled_shouldThrow() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctorId(1L);
        appointment.setPatientId(2L);
        appointment.setMode(AppointmentMode.PRESENTIEL);
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.confirmAppointment(1L, 1L));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void getActiveDoctorIdsForPatient_shouldReturnList() {
        when(appointmentRepository.findActiveDoctorIdsByPatientId(
                anyLong(), anyList())).thenReturn(List.of(1L, 3L));

        var result = appointmentService.getActiveDoctorIdsForPatient(2L);

        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(3L));
    }
}
