package com.medilinktunisia.patientservice.model.dto;

import com.medilinktunisia.patientservice.model.entity.PatientAllergy;
import com.medilinktunisia.patientservice.model.enums.AllergenType;
import com.medilinktunisia.patientservice.model.enums.SeverityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyDto {
    private Long id;
    private String allergenName;
    private AllergenType allergenType;
    private String reactionDescription;
    private SeverityLevel severityLevel;
    private LocalDate firstReactionDate;
    private LocalDate diagnosedDate;
    private Long diagnosedByDoctorId;
    private String treatmentGiven;
    private String additionalNotes;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static AllergyDto fromEntity(PatientAllergy allergy) {
        return AllergyDto.builder()
                .id(allergy.getId())
                .allergenName(allergy.getAllergenName())
                .allergenType(allergy.getAllergenType())
                .reactionDescription(allergy.getReactionDescription())
                .severityLevel(allergy.getSeverityLevel())
                .firstReactionDate(allergy.getFirstReactionDate())
                .diagnosedDate(allergy.getDiagnosedDate())
                .diagnosedByDoctorId(allergy.getDiagnosedByDoctorId())
                .treatmentGiven(allergy.getTreatmentGiven())
                .additionalNotes(allergy.getAdditionalNotes())
                .isActive(allergy.getIsActive())
                .createdAt(allergy.getCreatedAt())
                .build();
    }
}
