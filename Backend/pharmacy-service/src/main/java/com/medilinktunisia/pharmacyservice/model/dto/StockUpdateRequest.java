package com.medilinktunisia.pharmacyservice.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateRequest {

    @Min(value = 0, message = "La quantité doit être supérieure ou égale à 0")
    private Integer quantity;

    @Min(value = 0, message = "Le niveau minimum de stock doit être supérieur ou égal à 0")
    private Integer minimumStockLevel;

    @Min(value = 0, message = "Le niveau de réapprovisionnement doit être supérieur ou égal à 0")
    private Integer reorderLevel;

    @Min(value = 0, message = "Le niveau maximum de stock doit être supérieur ou égal à 0")
    private Integer maximumStockLevel;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix de vente doit être supérieur à 0")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix d'achat doit être supérieur à 0")
    private BigDecimal purchasePrice;

    private String batchNumber;

    private LocalDate expiryDate;

    private String shelfLocation;

    private String notes;
}
