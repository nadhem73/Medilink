package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.Appointment;
import com.medilinktunisia.patientservice.model.AppointmentMode;
import com.medilinktunisia.patientservice.model.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository repository;

    private final LocalDateTime now = LocalDateTime.of(2026, 6, 17, 10, 0);

    @BeforeEach
    void setUp() {
        Appointment a1 = new Appointment();
        a1.setPatientId(1L);
        a1.setDoctorId(10L);
        a1.setDateTime(now.plusDays(1));
        a1.setMode(AppointmentMode.PRESENTIEL);
        a1.setStatus(AppointmentStatus.PENDING);
        repository.saveAndFlush(a1);

        Appointment a2 = new Appointment();
        a2.setPatientId(1L);
        a2.setDoctorId(20L);
        a2.setDateTime(now);
        a2.setMode(AppointmentMode.TELECONSULTATION);
        a2.setStatus(AppointmentStatus.CONFIRMED);
        repository.saveAndFlush(a2);

        Appointment a3 = new Appointment();
        a3.setPatientId(2L);
        a3.setDoctorId(10L);
        a3.setDateTime(now.plusHours(2));
        a3.setMode(AppointmentMode.PRESENTIEL);
        a3.setStatus(AppointmentStatus.CANCELLED);
        repository.saveAndFlush(a3);

        Appointment a4 = new Appointment();
        a4.setPatientId(2L);
        a4.setDoctorId(10L);
        a4.setDateTime(now.plusHours(3));
        a4.setMode(AppointmentMode.PRESENTIEL);
        a4.setStatus(AppointmentStatus.PENDING);
        repository.saveAndFlush(a4);
    }

    @Test
    void findByPatientIdOrderByDateTimeDesc_shouldReturnPatientAppointmentsDesc() {
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
        List<Appointment> result = repository.findByDoctorIdOrderByDateTimeDesc(10L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDateTime()).isAfterOrEqualTo(result.get(1).getDateTime());
        assertThat(result.get(1).getDateTime()).isAfterOrEqualTo(result.get(2).getDateTime());
    }

    @Test
    void findByDoctorIdAndDateTimeBetween_shouldReturnAppointmentsInRange() {
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusMinutes(181);

        List<Appointment> result = repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(10L, start, end);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDateTime()).isBefore(result.get(1).getDateTime());
    }

    @Test
    void findByDoctorIdAndDateTimeBetween_whenOutsideRange_shouldReturnEmpty() {
        LocalDateTime start = now.plusDays(10);
        LocalDateTime end = now.plusDays(11);

        List<Appointment> result = repository.findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(10L, start, end);

        assertThat(result).isEmpty();
    }
}
