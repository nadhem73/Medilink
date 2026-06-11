package com.medilinktunisia.pharmacyservice.model.dto;

import com.medilinktunisia.pharmacyservice.model.entity.MedicationStock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDto {
    private Long id;
    private Long pharmacyId;
    private String pharmacyName;
    private Long medicationId;
    private String medicationName;
    private String medicationCode;
    private Integer quantity;
    private Integer minimumStockLevel;
    private Integer reorderLevel;
    private Integer maximumStockLevel;
    private BigDecimal sellingPrice;
    private BigDecimal purchasePrice;
    private String batchNumber;
    private LocalDate expiryDate;
    private String shelfLocation;
    private MedicationStock.StockStatus status;
    private Integer totalSold;
    private LocalDateTime lastSaleDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
