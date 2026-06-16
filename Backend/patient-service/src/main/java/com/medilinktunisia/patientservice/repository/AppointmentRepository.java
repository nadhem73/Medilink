package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.Appointment;
import com.medilinktunisia.patientservice.model.AppointmentStatus;
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

    /**
     * Vérifie si un patient a un rendez-vous actif (PENDING ou CONFIRMED) chez un médecin donné.
     */
    boolean existsByPatientIdAndDoctorIdAndStatusIn(Long patientId, Long doctorId, List<AppointmentStatus> statuses);

    /**
     * Retourne les IDs des médecins chez qui le patient a un rendez-vous actif.
     */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT a.doctorId FROM Appointment a WHERE a.patientId = :patientId AND a.status IN :statuses")
    List<Long> findActiveDoctorIdsByPatientId(@org.springframework.data.repository.query.Param("patientId") Long patientId,
                                               @org.springframework.data.repository.query.Param("statuses") List<AppointmentStatus> statuses);
}
