package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.entity.PatientMedicalDocument;
import com.medilinktunisia.patientservice.model.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientMedicalDocumentRepository extends JpaRepository<PatientMedicalDocument, Long> {

    List<PatientMedicalDocument> findByPatientId(Long patientId);

    @Query("SELECT d FROM PatientMedicalDocument d WHERE d.patient.id = :patientId ORDER BY d.createdAt DESC")
    List<PatientMedicalDocument> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<PatientMedicalDocument> findByPatientIdAndDocumentType(Long patientId, DocumentType documentType);

    @Query("SELECT d FROM PatientMedicalDocument d WHERE d.patient.id = :patientId AND d.isSharedWithDoctors = true")
    List<PatientMedicalDocument> findSharedDocuments(Long patientId);
}
