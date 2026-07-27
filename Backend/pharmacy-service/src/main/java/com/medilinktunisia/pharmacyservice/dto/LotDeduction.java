package com.medilinktunisia.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotDeduction {
    private Long stockId;
    private String numeroLot;
    private Long medicamentId;
    private Integer quantitePrelevee;
    private Integer stockRestant;
}
