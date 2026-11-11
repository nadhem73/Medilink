package com.medilinktunisia.bilanservice.service;

import com.medilinktunisia.bilanservice.dto.OcrRequest;
import com.medilinktunisia.bilanservice.dto.OcrResponse;
import com.medilinktunisia.bilanservice.dto.OcrResultData;
import com.medilinktunisia.bilanservice.dto.OcrResultItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OcrClientService ocrClientService;

    @Captor
    private ArgumentCaptor<OcrRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ocrClientService, "aiServiceUrl", "http://localhost:8093");
    }

    @Test
    void processOcr_shouldReturnSuccessResponse() {
        OcrResultData data = new OcrResultData();
        data.setTypeBilan("Bilan sanguin");
        data.setFormat("nouveau");
        data.setDateBilan("2026-06-15");
        data.setLaboratoire("Labo Tunis");
        OcrResultItem item = new OcrResultItem();
        item.setTest("Glycemie");
        item.setValeur("5.2");
        item.setUnite("mmol/L");
        data.setResultats(List.of(item));

        OcrResponse mockResponse = new OcrResponse(true, null, data);

        when(restTemplate.postForObject(anyString(), requestCaptor.capture(), eq(OcrResponse.class)))
                .thenReturn(mockResponse);

        OcrResponse result = ocrClientService.processOcr("base64image");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getTypeBilan()).isEqualTo("Bilan sanguin");
        assertThat(result.getData().getResultats()).hasSize(1);
        assertThat(result.getData().getResultats().get(0).getTest()).isEqualTo("Glycemie");
        assertThat(requestCaptor.getValue().getImage()).isEqualTo("base64image");
    }

    @Test
    void processOcr_shouldHandleNullResponse() {
        when(restTemplate.postForObject(anyString(), any(), eq(OcrResponse.class)))
                .thenReturn(null);

        OcrResponse result = ocrClientService.processOcr("base64image");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).contains("Empty response");
    }

    @Test
    void processOcr_shouldHandleRestClientException() {
        when(restTemplate.postForObject(anyString(), any(), eq(OcrResponse.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        OcrResponse result = ocrClientService.processOcr("base64image");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).contains("unavailable");
    }

    @Test
    void processOcr_shouldPostToCorrectUrl() {
        when(restTemplate.postForObject(eq("http://localhost:8093/api/ai/ocr"), any(), eq(OcrResponse.class)))
                .thenReturn(new OcrResponse(true, null, new OcrResultData()));

        OcrResponse result = ocrClientService.processOcr("test");

        assertThat(result.isSuccess()).isTrue();
    }
}
