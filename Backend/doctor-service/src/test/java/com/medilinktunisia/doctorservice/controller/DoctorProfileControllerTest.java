package com.medilinktunisia.doctorservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilinktunisia.doctorservice.dto.DoctorProfileDto;
import com.medilinktunisia.doctorservice.dto.DoctorProfileRequest;
import com.medilinktunisia.doctorservice.security.JwtService;
import com.medilinktunisia.doctorservice.service.DoctorProfileService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorProfileService service;

    @MockBean
    private JwtService jwtService;

    @Test
    void createDoctorProfile_shouldReturn201() throws Exception {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(1L);
        request.setAvailable(true);

        mockMvc.perform(post("/api/doctors/internal/doctor-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(service).createDoctorProfile(any(DoctorProfileRequest.class));
    }

    @Test
    void getMyDoctorProfile_shouldReturn200() throws Exception {
        DoctorProfileDto dto = DoctorProfileDto.builder()
                .userId(1L)
                .available(true)
                .fee(BigDecimal.valueOf(100))
                .build();

        when(service.getByUserId(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/api/doctors/me/doctor-profile")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.fee").value(100));
    }

    @Test
    void getAllDoctorProfiles_shouldReturn200() throws Exception {
        DoctorProfileDto dto1 = DoctorProfileDto.builder().userId(1L).available(true).build();
        DoctorProfileDto dto2 = DoctorProfileDto.builder().userId(2L).available(false).build();

        when(service.getAllProfiles()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/doctors/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[1].userId").value(2L));
    }

    @Test
    void updateMyDoctorProfile_shouldReturn200() throws Exception {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(false);
        request.setFee(BigDecimal.valueOf(150));

        DoctorProfileDto updated = DoctorProfileDto.builder()
                .userId(1L)
                .available(false)
                .fee(BigDecimal.valueOf(150))
                .build();

        when(service.updateByUserId(eq(1L), any(DoctorProfileRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/doctors/me/doctor-profile")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.fee").value(150));
    }
}
