package com.medilinktunisia.authservice.dto.request;

import com.medilinktunisia.authservice.model.enums.Gender;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String firstName;

    @Size(min = 2, max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    private LocalDate birthDate;

    private Gender gender;
}
