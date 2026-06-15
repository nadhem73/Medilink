package com.medilinktunisia.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerificationRequest {

    @NotBlank(message = "Le code de validation est obligatoire")
    @Size(min = 6, max = 6, message = "Le code OTP doit être composé de 6 chiffres")
    private String code;
}
