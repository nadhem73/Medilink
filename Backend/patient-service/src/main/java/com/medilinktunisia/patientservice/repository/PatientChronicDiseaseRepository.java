package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.entity.PatientChronicDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientChronicDiseaseRepository extends JpaRepository<PatientChronicDisease, Long> {

    List<PatientChronicDisease> findByPatientId(Long patientId);

    @Query("SELECT d FROM PatientChronicDisease d WHERE d.patient.id = :patientId ORDER BY d.diagnosedDate DESC")
    List<PatientChronicDisease> findByPatientIdOrderByDiagnosedDateDesc(Long patientId);

    @Query("SELECT d FROM PatientChronicDisease d WHERE d.patient.id = :patientId AND d.isControlled = false")
    List<PatientChronicDisease> findUncontrolledDiseases(Long patientId);
}
