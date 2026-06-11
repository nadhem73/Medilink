package com.medilinktunisia.patientservice.model.entity;

import com.medilinktunisia.patientservice.model.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_medical_documents", indexes = {
    @Index(name = "idx_document_patient", columnList = "patient_id"),
    @Index(name = "idx_document_type", columnList = "document_type"),
    @Index(name = "idx_document_date", columnList = "document_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMedicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "document_title", nullable = false, length = 200)
    private String documentTitle;

    @Column(name = "document_description", columnDefinition = "TEXT")
    private String documentDescription;

    @Column(name = "document_category", length = 100)
    private String documentCategory; // RADIOLOGY, CARDIOLOGY, GENERAL

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "file_mime_type", length = 100)
    private String fileMimeType;

    @Column(name = "uploaded_by_user_id")
    private Long uploadedByUserId;

    @Column(name = "document_date")
    private LocalDate documentDate;

    @Column(name = "is_shared_with_doctors", nullable = false)
    @Builder.Default
    private Boolean isSharedWithDoctors = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
