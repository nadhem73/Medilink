package com.medilinktunisia.ambulanceservice.model.dto;

import com.medilinktunisia.ambulanceservice.model.enums.EmergencyPriority;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyStatus;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyDto {
    private Long id;
    private String emergencyCode;
    private String callerName;
    private String callerPhone;
    private String callerRelationToPatient;
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private Long patientId;
    private EmergencyType emergencyType;
    private EmergencyPriority priority;
    private String description;
    private String symptoms;
    private String address;
    private Double latitude;
    private Double longitude;
    private String locationDetails;
    private AmbulanceDto assignedAmbulance;
    private EmergencyStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime completedAt;
    private Integer estimatedArrivalMinutes;
    private String destinationHospital;
    private String paramedicNotes;
}
