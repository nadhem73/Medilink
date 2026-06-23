package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.Appointment;
import com.medilinktunisia.patientservice.model.AppointmentMode;
import com.medilinktunisia.patientservice.model.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository repository;

    private final LocalDateTime now = LocalDateTime.of(2026, 6, 17, 10, 0);

    private Appointment createAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, AppointmentMode mode, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setDateTime(dateTime);
        appointment.setMode(mode);
        appointment.setStatus(status);
        return repository.saveAndFlush(appointment);
    }

    @Test
    void findByPatientIdOrderByDateTimeDesc_shouldReturnPatientAppointmentsDesc() {
        createAppointment(1L, 10L, now.plusDays(1), AppointmentMode.PRESENTIEL, AppointmentStatus.PENDING);
        createAppointment(1L, 20L, now, AppointmentMode.TELECONSULTATION, AppointmentStatus.CONFIRMED);

        List<Appointment> result = repository.findByPatientIdOrderByDateTimeDesc(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDateTime()).isAfter(result.get(1).getDateTime());
    }

    @Test
    void findByPatientIdOrderByDateTimeDesc_whenNoAppointments_shouldReturnEmpty() {
        List<Appointment> result = repository.findByPatientIdOrderByDateTimeDesc(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByDoctorIdOrderByDateTimeDesc_shouldReturnDoctorAppointmentsDesc() {
        createAppointment(1L, 10L, now.plusDays(1), AppointmentMode.PRESENTIEL, AppointmentStatus.PENDING);
        createAppointment(1L, 20L, now, AppointmentMode.TELECONSULTATION, AppointmentStatus.CONFIRMED);
        createAppointment(2L, 10L, now.plusHours(2), AppointmentMode.PRESENTIEL, AppointmentStatus.CANCELLED);

        List<Appointment> result = repository.findByDoctorIdOrderByDateTimeDesc(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDateTime()).isAfter(result.get(1).getDateTime());
    }

    @Test
    void findByDoctorIdAndDateTimeBetween_shouldReturnAppointmentsInRange() {
        createAppointment(1L, 10L, now.plusHours(1), AppointmentMode.PRESENTIEL, AppointmentStatus.PENDING);
        createAppointment(2L, 10L, now.plusHours(2), AppointmentMode.PRESENTIEL, AppointmentStatus.CANCELLED);
        createAppointment(3L, 20L, now.plusHours(1), AppointmentMode.PRESENTIEL, AppointmentStatus.PENDING);

        LocalDateTime start = now;
        LocalDateTime end = now.plusHours(3);

        List<Appointment> result = repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(10L, start, end);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDateTime()).isBefore(result.get(1).getDateTime());
    }

    @Test
    void findByDoctorIdAndDateTimeBetween_whenOutsideRange_shouldReturnEmpty() {
        createAppointment(1L, 10L, now.plusHours(1), AppointmentMode.PRESENTIEL, AppointmentStatus.PENDING);

        LocalDateTime start = now.plusDays(10);
        LocalDateTime end = now.plusDays(11);

        List<Appointment> result = repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(10L, start, end);

        assertThat(result).isEmpty();
    }

    @Test
    void findByPatientIdOrderByDateTimeDesc_shouldReturnAppointments() {
        Appointment appointment = new Appointment();
        appointment.setPatientId(1L);
        appointment.setDoctorId(2L);
        appointment.setDateTime(LocalDateTime.of(2025, 6, 20, 10, 0));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setMode(AppointmentMode.PRESENTIEL);

        repository.save(appointment);

        List<Appointment> found = repository.findByPatientIdOrderByDateTimeDesc(1L);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDoctorId()).isEqualTo(2L);
        assertThat(found.get(0).getPatientId()).isEqualTo(1L);
        assertThat(found.get(0).getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(found.get(0).getMode()).isEqualTo(AppointmentMode.PRESENTIEL);
    }

    @Test
    void findByDoctorIdOrderByDateTimeDesc_shouldReturnAppointments() {
        Appointment appointment = new Appointment();
        appointment.setPatientId(3L);
        appointment.setDoctorId(4L);
        appointment.setDateTime(LocalDateTime.of(2025, 6, 21, 14, 30));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setMode(AppointmentMode.TELECONSULTATION);

        repository.save(appointment);

        List<Appointment> found = repository.findByDoctorIdOrderByDateTimeDesc(4L);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPatientId()).isEqualTo(3L);
        assertThat(found.get(0).getDoctorId()).isEqualTo(4L);
        assertThat(found.get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(found.get(0).getMode()).isEqualTo(AppointmentMode.TELECONSULTATION);
    }

    @Test
    void findByPatientId_whenNoneExist_shouldReturnEmpty() {
        List<Appointment> found = repository.findByPatientIdOrderByDateTimeDesc(999L);
        assertThat(found).isEmpty();
    }
}
