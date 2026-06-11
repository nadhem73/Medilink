package com.medilinktunisia.ambulanceservice.repository;

import com.medilinktunisia.ambulanceservice.model.entity.Emergency;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {

    Optional<Emergency> findByEmergencyCode(String emergencyCode);

    List<Emergency> findByStatus(EmergencyStatus status);

    List<Emergency> findByAssignedAmbulanceId(Long ambulanceId);

    List<Emergency> findByStatusIn(List<EmergencyStatus> statuses);

    @Query("SELECT e FROM Emergency e WHERE e.status IN ('PENDING', 'ASSIGNED', 'EN_ROUTE', 'AT_SCENE', 'TRANSPORTING') ORDER BY e.priority DESC, e.requestedAt ASC")
    List<Emergency> findActiveEmergencies();

    List<Emergency> findByRequestedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT e FROM Emergency e WHERE e.patientId = :patientId ORDER BY e.requestedAt DESC")
    List<Emergency> findByPatientId(Long patientId);

    @Query("SELECT COUNT(e) FROM Emergency e WHERE e.status = :status")
    Long countByStatus(EmergencyStatus status);

    @Query("SELECT AVG(e.actualResponseTimeMinutes) FROM Emergency e WHERE e.completedAt BETWEEN :start AND :end")
    Double getAverageResponseTime(LocalDateTime start, LocalDateTime end);
}
