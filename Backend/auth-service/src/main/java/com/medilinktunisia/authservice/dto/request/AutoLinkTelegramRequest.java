package com.medilinktunisia.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AutoLinkTelegramRequest {

    @NotBlank
    @Email
    private String email;
}
