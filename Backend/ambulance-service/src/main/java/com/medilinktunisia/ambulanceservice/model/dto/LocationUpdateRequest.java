package com.medilinktunisia.ambulanceservice.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {

    @NotNull(message = "L'ID de l'ambulance est obligatoire")
    private Long ambulanceId;

    @NotNull(message = "La latitude est obligatoire")
    @Min(value = -90, message = "La latitude doit être entre -90 et 90")
    @Max(value = 90, message = "La latitude doit être entre -90 et 90")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    @Min(value = -180, message = "La longitude doit être entre -180 et 180")
    @Max(value = 180, message = "La longitude doit être entre -180 et 180")
    private Double longitude;

    private Double speed;
    private Double heading;
    private Double accuracy;
    private Long emergencyId;
}
