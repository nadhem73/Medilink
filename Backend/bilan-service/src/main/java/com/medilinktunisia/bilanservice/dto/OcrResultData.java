package com.medilinktunisia.bilanservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultData {
    @JsonProperty("type_bilan")
    private String typeBilan;

    private String format;

    @JsonProperty("date_bilan")
    private String dateBilan;

    private String laboratoire;

    private List<OcrResultItem> resultats;
}
