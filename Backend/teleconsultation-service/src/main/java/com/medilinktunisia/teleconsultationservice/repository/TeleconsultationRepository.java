package com.medilinktunisia.teleconsultationservice.repository;

import com.medilinktunisia.teleconsultationservice.model.entity.Teleconsultation;
import com.medilinktunisia.teleconsultationservice.model.enums.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeleconsultationRepository extends JpaRepository<Teleconsultation, Long> {

    Optional<Teleconsultation> findByConsultationCode(String consultationCode);

    Optional<Teleconsultation> findBySessionId(String sessionId);

    List<Teleconsultation> findByPatientIdOrderByScheduledTimeDesc(Long patientId);

    List<Teleconsultation> findByDoctorIdOrderByScheduledTimeDesc(Long doctorId);

    List<Teleconsultation> findByStatusOrderByScheduledTimeDesc(ConsultationStatus status);

    List<Teleconsultation> findByPatientIdAndStatusOrderByScheduledTimeDesc(Long patientId, ConsultationStatus status);

    List<Teleconsultation> findByDoctorIdAndStatusOrderByScheduledTimeDesc(Long doctorId, ConsultationStatus status);

    @Query("SELECT t FROM Teleconsultation t WHERE t.scheduledTime BETWEEN :startDate AND :endDate ORDER BY t.scheduledTime ASC")
    List<Teleconsultation> findByScheduledTimeBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Teleconsultation t WHERE t.doctorId = :doctorId AND t.scheduledTime BETWEEN :startDate AND :endDate ORDER BY t.scheduledTime ASC")
    List<Teleconsultation> findByDoctorIdAndScheduledTimeBetween(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Teleconsultation t WHERE t.patientId = :patientId AND t.scheduledTime BETWEEN :startDate AND :endDate ORDER BY t.scheduledTime ASC")
    List<Teleconsultation> findByPatientIdAndScheduledTimeBetween(
        @Param("patientId") Long patientId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(t) FROM Teleconsultation t WHERE t.doctorId = :doctorId AND t.status = :status")
    Long countByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") ConsultationStatus status);

    @Query("SELECT COUNT(t) FROM Teleconsultation t WHERE t.patientId = :patientId AND t.status = :status")
    Long countByPatientIdAndStatus(@Param("patientId") Long patientId, @Param("status") ConsultationStatus status);

    @Query("SELECT t FROM Teleconsultation t JOIN t.participants p WHERE p.userId = :userId AND t.scheduledTime > :now AND t.status = 'SCHEDULED' ORDER BY t.scheduledTime ASC")
    List<Teleconsultation> findUpcomingConsultationsForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
