package com.medilinktunisia.prescriptionservice.repository;

import com.medilinktunisia.prescriptionservice.model.entity.PrescriptionMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionMedicationRepository extends JpaRepository<PrescriptionMedication, Long> {

    List<PrescriptionMedication> findByPrescriptionId(Long prescriptionId);

    @Query("SELECT pm FROM PrescriptionMedication pm WHERE pm.prescription.patientId = :patientId")
    List<PrescriptionMedication> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT pm FROM PrescriptionMedication pm WHERE pm.medicationName LIKE %:name%")
    List<PrescriptionMedication> findByMedicationNameContaining(@Param("name") String name);
}
