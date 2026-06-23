package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.dto.MedicalRecordDto;
import com.medilinktunisia.patientservice.dto.MedicalRecordRequest;
import com.medilinktunisia.patientservice.security.JwtService;
import com.medilinktunisia.patientservice.service.MedicalRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicalRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicalRecordService service;

    @MockBean
    private JwtService jwtService;

    @Test
    void createMedicalRecord_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/patients/internal/medical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1}"))
                .andExpect(status().isCreated());

        verify(service).createMedicalRecord(any(MedicalRecordRequest.class));
    }

    @Test
    void getMyMedicalRecord_shouldReturn200() throws Exception {
        MedicalRecordDto dto = MedicalRecordDto.builder()
                .userId(1L).bloodGroup("A+")
                .height(175.0).weight(70.0)
                .allergies("None").build();

        when(service.getByUserId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/patients/me/medical-record")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.bloodGroup", is("A+")));

        verify(service).getByUserId(1L);
    }

    @Test
    void getPatientMedicalRecord_shouldReturn200() throws Exception {
        when(service.getByUserId(2L))
                .thenReturn(MedicalRecordDto.builder().userId(2L).bloodGroup("B+").build());

        mockMvc.perform(get("/api/patients/{userId}/medical-record", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(2)))
                .andExpect(jsonPath("$.bloodGroup", is("B+")));

        verify(service).getByUserId(2L);
    }
}
