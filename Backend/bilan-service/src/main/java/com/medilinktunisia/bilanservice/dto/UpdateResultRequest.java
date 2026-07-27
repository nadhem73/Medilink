package com.medilinktunisia.bilanservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResultRequest {
    private String valeur;
    private String valeurAncienne;
    private Double referenceMin;
    private Double referenceMax;
    private String referenceText;
}
