package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisItemDto {
    private Long id;
    private Long analysisTypeId;
    private String analysisTypeName;
    private String analysisTypeCode;
    private BigDecimal price;
    private String notes;
    private AnalysisItem.ItemStatus status;
}
