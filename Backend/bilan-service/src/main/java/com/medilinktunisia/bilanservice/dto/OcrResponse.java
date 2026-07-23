package com.medilinktunisia.bilanservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResponse {
    private boolean success;
    private String error;
    private OcrResultData data;
}
