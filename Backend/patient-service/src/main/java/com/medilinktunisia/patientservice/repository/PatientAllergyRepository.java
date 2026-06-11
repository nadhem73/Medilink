package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.entity.PatientAllergy;
import com.medilinktunisia.patientservice.model.enums.SeverityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientAllergyRepository extends JpaRepository<PatientAllergy, Long> {

    List<PatientAllergy> findByPatientId(Long patientId);

    List<PatientAllergy> findByPatientIdAndIsActiveTrue(Long patientId);

    @Query("SELECT a FROM PatientAllergy a WHERE a.patient.id = :patientId AND a.severityLevel = :severity AND a.isActive = true")
    List<PatientAllergy> findActiveAllergiesBySeverity(Long patientId, SeverityLevel severity);

    @Query("SELECT a FROM PatientAllergy a WHERE a.patient.id = :patientId AND a.severityLevel IN ('HIGH', 'CRITICAL') AND a.isActive = true")
    List<PatientAllergy> findCriticalAllergies(Long patientId);
}
