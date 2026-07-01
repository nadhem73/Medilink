package com.medilinktunisia.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrescriptionEmailRequest {

    @NotBlank
    private String patientEmail;

    @NotBlank
    private String patientName;

    private String pdfMedicationsBase64;

    private String pdfAnalysesBase64;

    private String medicationsFileName;

    private String analysesFileName;
}
