package com.medilinktunisia.ambulanceservice.model.dto;

import com.medilinktunisia.ambulanceservice.model.enums.AmbulanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmbulanceDto {
    private Long id;
    private String registrationNumber;
    private String vehicleModel;
    private AmbulanceStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private String baseStation;
    private String equipment;
    private Boolean hasDefibrillator;
    private Boolean hasOxygenSupply;
    private Boolean hasAdvancedLifeSupport;
    private String driverName;
    private String driverPhone;
    private String paramedicName;
    private String paramedicPhone;
    private Integer capacity;
    private Boolean active;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private String notes;
}
