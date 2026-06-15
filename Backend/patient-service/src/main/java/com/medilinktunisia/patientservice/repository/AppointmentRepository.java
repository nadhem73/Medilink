package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByDateTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByDateTimeDesc(Long doctorId);

    /**
     * Récupère les rendez-vous d'un médecin dans une plage de dates (pour la vue calendrier).
     */
    List<Appointment> findByDoctorIdAndDateTimeBetweenOrderByDateTimeAsc(
            Long doctorId, LocalDateTime start, LocalDateTime end);
}
