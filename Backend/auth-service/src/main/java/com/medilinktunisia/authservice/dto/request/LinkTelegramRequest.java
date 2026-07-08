package com.medilinktunisia.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkTelegramRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String telegramChatId;
}
