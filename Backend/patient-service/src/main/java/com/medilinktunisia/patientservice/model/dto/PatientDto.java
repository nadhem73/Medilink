package com.medilinktunisia.patientservice.model.dto;

import com.medilinktunisia.patientservice.model.entity.Patient;
import com.medilinktunisia.patientservice.model.enums.Gender;
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
public class PatientDto {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Integer age;
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String postalCode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    private String insuranceNumber;
    private String insuranceProvider;
    private String profilePictureUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PatientDto fromEntity(Patient patient) {
        return PatientDto.builder()
                .id(patient.getId())
                .userId(patient.getUserId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .age(patient.getAge())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .city(patient.getCity())
                .postalCode(patient.getPostalCode())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .emergencyContactRelationship(patient.getEmergencyContactRelationship())
                .insuranceNumber(patient.getInsuranceNumber())
                .insuranceProvider(patient.getInsuranceProvider())
                .profilePictureUrl(patient.getProfilePictureUrl())
                .isActive(patient.getIsActive())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
