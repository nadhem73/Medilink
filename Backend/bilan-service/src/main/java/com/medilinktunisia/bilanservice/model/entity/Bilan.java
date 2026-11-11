package com.medilinktunisia.bilanservice.model.entity;

import com.medilinktunisia.bilanservice.model.enums.BilanStatus;
import com.medilinktunisia.bilanservice.model.enums.FormatBilan;
import com.medilinktunisia.bilanservice.model.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bilans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bilan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ReviewStatus reviewStatus;

    @Column(name = "type_bilan")
    private String typeBilan;

    @Enumerated(EnumType.STRING)
    @Column(name = "format")
    private FormatBilan format;

    @Column(name = "date_bilan")
    private LocalDate dateBilan;

    @Column(name = "laboratoire")
    private String laboratoire;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "raw_ocr_text", columnDefinition = "TEXT")
    private String rawOcrText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BilanStatus status;

    @OneToMany(mappedBy = "bilan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<BilanResult> resultats = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = BilanStatus.PENDING;
        }
        if (reviewStatus == null) {
            reviewStatus = ReviewStatus.EN_ATTENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
