package com.medilinktunisia.teleconsultationservice.model.dto;

import com.medilinktunisia.teleconsultationservice.model.enums.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantDto {
    private Long id;
    private Long userId;
    private String userName;
    private ParticipantRole role;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
}
