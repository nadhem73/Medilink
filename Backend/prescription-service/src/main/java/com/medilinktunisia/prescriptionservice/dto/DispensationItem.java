package com.medilinktunisia.prescriptionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispensationItem {
    private Long medicamentId;
    private Integer quantite;
}
