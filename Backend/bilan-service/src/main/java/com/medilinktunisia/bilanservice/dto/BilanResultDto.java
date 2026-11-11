package com.medilinktunisia.bilanservice.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BilanResultDto {
    private Long id;
    private String testName;
    private String valeur;
    private String unite;
    private Double referenceMin;
    private Double referenceMax;
    private String referenceText;
    private String valeurAncienne;
    private LocalDate dateAncienne;
    private String status;

    private String confiance;
}
