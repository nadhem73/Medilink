package com.medilinktunisia.patientservice.controller;

import com.medilinktunisia.patientservice.dto.MedicalRecordDto;
import com.medilinktunisia.patientservice.dto.MedicalRecordRequest;
import com.medilinktunisia.patientservice.security.JwtAuthenticationFilter;
import com.medilinktunisia.patientservice.security.JwtService;
import com.medilinktunisia.patientservice.security.SecurityConfig;
import com.medilinktunisia.patientservice.service.MedicalRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MedicalRecordController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
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
        String body = """
                {
                    "userId": 1,
                    "bloodGroup": "A+",
                    "height": 175.0,
                    "weight": 70.0,
                    "allergies": "None",
                    "chronicDiseases": "None",
                    "currentTreatments": "None",
                    "emergencyContactName": "John",
                    "emergencyContactPhone": "+21612345678",
                    "insuranceCompany": "Comp",
                    "insuranceNumber": "123"
                }
                """;

        mockMvc.perform(post("/api/patients/internal/medical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
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
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.bloodGroup", is("A+")))
                .andExpect(jsonPath("$.height", is(175.0)))
                .andExpect(jsonPath("$.weight", is(70.0)))
                .andExpect(jsonPath("$.allergies", is("None")));

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

    @Test
    void getMyMedicalRecord_whenRecordNotFound_shouldReturnEmptyDto() throws Exception {
        when(service.getByUserId(1L))
                .thenReturn(MedicalRecordDto.builder().userId(1L).build());

        mockMvc.perform(get("/api/patients/me/medical-record")
                        .with(request -> {
                            request.setAttribute("userId", 1L);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.bloodGroup").isEmpty());
    }

    @Test
    void getPatientMedicalRecord_whenNotFound_shouldReturnEmptyDto() throws Exception {
        when(service.getByUserId(999L))
                .thenReturn(MedicalRecordDto.builder().userId(999L).build());

        mockMvc.perform(get("/api/patients/{userId}/medical-record", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(999L))
                .andExpect(jsonPath("$.bloodGroup").isEmpty());
    }
}
