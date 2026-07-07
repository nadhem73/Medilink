package com.medilinktunisia.prescriptionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResult {
    private Long medicamentId;
    private String medicamentName;
    private boolean inStock;
    private Integer totalStock;
}
