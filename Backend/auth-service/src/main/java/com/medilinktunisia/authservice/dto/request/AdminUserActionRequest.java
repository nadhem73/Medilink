package com.medilinktunisia.authservice.dto.request;

import com.medilinktunisia.authservice.model.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserActionRequest {

    @NotNull
    private UserStatus status;

    private LocalDateTime suspendUntil;
}