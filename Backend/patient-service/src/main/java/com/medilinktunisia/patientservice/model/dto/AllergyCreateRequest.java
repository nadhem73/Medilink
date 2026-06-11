package com.medilinktunisia.patientservice.model.dto;

import com.medilinktunisia.patientservice.model.enums.AllergenType;
import com.medilinktunisia.patientservice.model.enums.SeverityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyCreateRequest {

    @NotBlank(message = "Le nom de l'allergène est obligatoire")
    private String allergenName;

    @NotNull(message = "Le type d'allergène est obligatoire")
    private AllergenType allergenType;

    @NotBlank(message = "La description de la réaction est obligatoire")
    private String reactionDescription;

    @NotNull(message = "Le niveau de gravité est obligatoire")
    private SeverityLevel severityLevel;

    private LocalDate firstReactionDate;
    private LocalDate diagnosedDate;
    private Long diagnosedByDoctorId;
    private String treatmentGiven;
    private String additionalNotes;
}
