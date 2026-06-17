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
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    void createDoctorProfile_returns201() throws Exception {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(1L);

        mockMvc.perform(post("/api/doctors/internal/doctor-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getMyDoctorProfile_returns200() throws Exception {
        DoctorProfileDto dto = DoctorProfileDto.builder().userId(1L).available(true).build();
        when(service.getByUserId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/doctors/me/doctor-profile")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void getAllDoctorProfiles_returns200() throws Exception {
        DoctorProfileDto dto = DoctorProfileDto.builder().userId(1L).available(true).build();
        when(service.getAllProfiles()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/doctors/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateMyDoctorProfile_returns200() throws Exception {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(false);

        DoctorProfileDto dto = DoctorProfileDto.builder().userId(1L).available(false).build();
        when(service.updateByUserId(any(Long.class), any(DoctorProfileRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/doctors/me/doctor-profile")
                .requestAttr("userId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void createDoctorProfile_withAllFields_returns201() throws Exception {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(1L);
        request.setAvailable(true);
        request.setBiography("General practitioner");
        request.setFee(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/doctors/internal/doctor-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllDoctorProfiles_emptyList_returns200() throws Exception {
        when(service.getAllProfiles()).thenReturn(List.of());

        mockMvc.perform(get("/api/doctors/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createDoctorProfile_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/doctors/internal/doctor-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMyDoctorProfile_partialFields_returns200() throws Exception {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(true);

        DoctorProfileDto dto = DoctorProfileDto.builder().userId(1L).available(true).build();
        when(service.updateByUserId(any(Long.class), any(DoctorProfileRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/doctors/me/doctor-profile")
                .requestAttr("userId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }
}
