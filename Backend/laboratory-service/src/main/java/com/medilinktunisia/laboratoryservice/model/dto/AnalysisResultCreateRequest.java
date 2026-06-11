package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResultCreateRequest {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotNull(message = "Analysis type ID is required")
    private Long analysisTypeId;

    @Size(max = 2000)
    private String result;

    @Size(max = 50)
    private String unit;

    @Size(max = 500)
    private String referenceRange;

    private AnalysisResult.ResultStatus resultStatus;

    @Size(max = 2000)
    private String interpretation;

    @Size(max = 1000)
    private String comments;

    private String performedBy;

    private LocalDateTime performedAt;
}
