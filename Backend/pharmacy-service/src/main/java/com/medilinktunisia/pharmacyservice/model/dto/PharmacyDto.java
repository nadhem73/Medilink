package com.medilinktunisia.pharmacyservice.model.dto;

import com.medilinktunisia.pharmacyservice.model.entity.Pharmacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyDto {
    private Long id;
    private Long userId;
    private String name;
    private String licenseNumber;
    private String ownerName;
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
    private Boolean homeDelivery;
    private Boolean electronicPrescriptionEnabled;
    private Boolean nightService;
    private Pharmacy.PharmacyStatus status;
    private String description;
    private String website;
    private Integer totalOrders;
    private Integer totalMedicationsSold;
    private Double rating;
    private Integer reviewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
