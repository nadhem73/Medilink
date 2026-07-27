package com.medilinktunisia.bilanservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultItem {
    private String test;

    private String valeur;

    private String unite;

    @JsonProperty("reference_min")
    private Double referenceMin;

    @JsonProperty("reference_max")
    private Double referenceMax;

    @JsonProperty("reference_text")
    private String referenceText;

    @JsonProperty("valeur_ancienne")
    private String valeurAncienne;

    @JsonProperty("date_ancienne")
    private String dateAncienne;

    private String confiance;
}
