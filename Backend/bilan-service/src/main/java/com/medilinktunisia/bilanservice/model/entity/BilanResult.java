package com.medilinktunisia.bilanservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medilinktunisia.bilanservice.model.enums.ResultStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bilan_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BilanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bilan_id", nullable = false)
    @JsonIgnore
    private Bilan bilan;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "valeur")
    private String valeur;

    @Column(name = "unite")
    private String unite;

    @Column(name = "reference_min")
    private Double referenceMin;

    @Column(name = "reference_max")
    private Double referenceMax;

    @Column(name = "reference_text")
    private String referenceText;

    @Column(name = "valeur_ancienne")
    private String valeurAncienne;

    @Column(name = "date_ancienne")
    private LocalDate dateAncienne;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ResultStatus status;

    @Column(name = "confiance")
    private String confiance;
}
