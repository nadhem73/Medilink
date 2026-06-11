package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResultDto {
    private Long id;
    private Long requestId;
    private String requestNumber;
    private Long analysisTypeId;
    private String analysisTypeName;
    private String analysisTypeCode;
    private String result;
    private String unit;
    private String referenceRange;
    private AnalysisResult.ResultStatus resultStatus;
    private String pdfFileName;
    private String pdfFilePath;
    private Long pdfFileSize;
    private String interpretation;
    private String comments;
    private Boolean validated;
    private String validatedBy;
    private LocalDateTime validatedAt;
    private String performedBy;
    private LocalDateTime performedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
