package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.Laboratory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaboratoryDto {
    private Long id;
    private Long userId;
    private String name;
    private String licenseNumber;
    private String directorName;
    private String address;
    private String city;
    private String zipCode;
    private String region;
    private String phone;
    private String alternativePhone;
    private String email;
    private Double latitude;
    private Double longitude;
    private String openingHours;
    private String accreditations;
    private String specialties;
    private Boolean homeCollection;
    private Boolean urgentAnalysisAvailable;
    private Laboratory.LaboratoryStatus status;
    private String description;
    private String website;
    private Integer totalAnalyses;
    private Integer totalPatients;
    private Double rating;
    private Integer reviewsCount;
    private Integer averageTurnaroundTimeHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
