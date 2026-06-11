package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.entity.Patient;
import com.medilinktunisia.patientservice.model.entity.PatientMedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientMedicalRecordRepository extends JpaRepository<PatientMedicalRecord, Long> {

    Optional<PatientMedicalRecord> findByPatient(Patient patient);

    Optional<PatientMedicalRecord> findByPatientId(Long patientId);

    Boolean existsByPatientId(Long patientId);
}
