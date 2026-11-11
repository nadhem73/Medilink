package com.medilinktunisia.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRuptureAlert {
    private Long medicamentId;
    private String medicamentName;
    private Integer stockTotal;
    private String status;
}
