package com.medilinktunisia.bilanservice.service;

import com.medilinktunisia.bilanservice.dto.OcrRequest;
import com.medilinktunisia.bilanservice.dto.OcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrClientService {

    private final RestTemplate restTemplate;

    @Value("${app.ai-service.url}")
    private String aiServiceUrl;

    public OcrResponse processOcr(String base64Image) {
        String url = aiServiceUrl + "/api/ai/ocr";
        OcrRequest request = new OcrRequest(base64Image);

        try {
            log.info("Calling AI OCR service at {}", url);
            OcrResponse response = restTemplate.postForObject(url, request, OcrResponse.class);
            if (response == null) {
                return new OcrResponse(false, "Empty response from AI service", null);
            }
            log.info("OCR response: success={}, type={}", response.isSuccess(),
                    response.getData() != null ? response.getData().getTypeBilan() : "null");
            return response;
        } catch (RestClientException e) {
            log.error("AI OCR service call failed: {}", e.getMessage());
            return new OcrResponse(false, "AI OCR service unavailable: " + e.getMessage(), null);
        }
    }
}
