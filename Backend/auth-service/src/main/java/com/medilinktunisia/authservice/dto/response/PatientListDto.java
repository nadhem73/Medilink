package com.medilinktunisia.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientListDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
}
