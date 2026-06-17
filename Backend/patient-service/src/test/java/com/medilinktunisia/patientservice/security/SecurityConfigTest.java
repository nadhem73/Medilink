package com.medilinktunisia.patientservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void internalEndpoint_shouldBeAccessibleWithoutToken() throws Exception {
        String body = """
                {"userId": 1}
                """;
        mockMvc.perform(post("/api/patients/internal/medical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void myMedicalRecordEndpoint_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/patients/me/medical-record"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientMedicalRecordEndpoint_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/patients/1/medical-record"))
                .andExpect(status().isUnauthorized());
    }
}
