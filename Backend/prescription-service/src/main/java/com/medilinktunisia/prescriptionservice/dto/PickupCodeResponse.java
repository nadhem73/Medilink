package com.medilinktunisia.prescriptionservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PickupCodeResponse {
    private Long id;
    private Long prescriptionId;
    private String code;
    private boolean used;
}
