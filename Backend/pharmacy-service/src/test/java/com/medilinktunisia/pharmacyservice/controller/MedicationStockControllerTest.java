package com.medilinktunisia.pharmacyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilinktunisia.pharmacyservice.dto.*;
import com.medilinktunisia.pharmacyservice.exception.GlobalExceptionHandler;
import com.medilinktunisia.pharmacyservice.security.JwtService;
import com.medilinktunisia.pharmacyservice.service.MedicationStockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicationStockController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MedicationStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicationStockService medicationStockService;

    @MockBean
    private JwtService jwtService;

    // ─── POST /stock/dispenser ─────────────────────────────────

    @Test
    void dispenserStock_returns200() throws Exception {
        DispensationRequest request = DispensationRequest.builder()
                .items(List.of(new DispensationItem(1L, 30)))
                .build();
        DispensationResult result = DispensationResult.builder()
                .success(true)
                .deductions(List.of(LotDeduction.builder()
                        .stockId(10L)
                        .numeroLot("LOT-2401-2701")
                        .medicamentId(1L)
                        .quantitePrelevee(30)
                        .stockRestant(70)
                        .build()))
                .build();

        when(medicationStockService.dispenserStock(anyList())).thenReturn(result);

        mockMvc.perform(post("/stock/dispenser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.deductions[0].quantitePrelevee").value(30));
    }

    @Test
    void dispenserStock_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/stock/dispenser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dispenserStock_insufficientStock_returns409() throws Exception {
        DispensationRequest request = DispensationRequest.builder()
                .items(List.of(new DispensationItem(1L, 999)))
                .build();

        when(medicationStockService.dispenserStock(anyList()))
                .thenThrow(new RuntimeException("Stock insuffisant pour : DOLIPRANE"));

        mockMvc.perform(post("/stock/dispenser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Stock insuffisant pour : DOLIPRANE"));
    }

    // ─── GET /stock/alerts/rupture ─────────────────────────────

    @Test
    void getRuptureAlerts_returns200() throws Exception {
        List<StockRuptureAlert> alerts = List.of(
                StockRuptureAlert.builder()
                        .medicamentId(1L)
                        .medicamentName("DOLIPRANE")
                        .stockTotal(5)
                        .status("Critique")
                        .build()
        );

        when(medicationStockService.getRuptureAlerts(20)).thenReturn(alerts);

        mockMvc.perform(get("/stock/alerts/rupture")
                        .param("seuil", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medicamentName").value("DOLIPRANE"))
                .andExpect(jsonPath("$[0].status").value("Critique"));
    }

    @Test
    void getRuptureAlerts_defaultSeuil_returns200() throws Exception {
        when(medicationStockService.getRuptureAlerts(20)).thenReturn(List.of());

        mockMvc.perform(get("/stock/alerts/rupture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getRuptureAlerts_customSeuil_returns200() throws Exception {
        when(medicationStockService.getRuptureAlerts(50)).thenReturn(List.of());

        mockMvc.perform(get("/stock/alerts/rupture")
                        .param("seuil", "50"))
                .andExpect(status().isOk());
    }

    // ─── GET /stock/alerts/perimes ─────────────────────────────

    @Test
    void getPerimesAlerts_returns200() throws Exception {
        List<MedicationStockDto> alerts = List.of(
                MedicationStockDto.builder()
                        .id(10L)
                        .medicamentId(1L)
                        .medicamentName("DOLIPRANE")
                        .numeroLot("LOT-2401-2701")
                        .quantiteEnStock(100)
                        .dateExpiration(LocalDate.now().plusDays(15))
                        .emplacement("A12")
                        .build()
        );

        when(medicationStockService.getPerimesAlerts(30)).thenReturn(alerts);

        mockMvc.perform(get("/stock/alerts/perimes")
                        .param("jours", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medicamentName").value("DOLIPRANE"))
                .andExpect(jsonPath("$[0].numeroLot").value("LOT-2401-2701"));
    }

    @Test
    void getPerimesAlerts_defaultJours_returns200() throws Exception {
        when(medicationStockService.getPerimesAlerts(30)).thenReturn(List.of());

        mockMvc.perform(get("/stock/alerts/perimes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── GET /stock/medicament/{id}/total ──────────────────────

    @Test
    void getTotalStock_returns200() throws Exception {
        when(medicationStockService.getTotalStock(1L)).thenReturn(150);

        mockMvc.perform(get("/stock/medicament/1/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicamentId").value(1))
                .andExpect(jsonPath("$.totalStock").value(150))
                .andExpect(jsonPath("$.inStock").value(true));
    }

    @Test
    void getTotalStock_zero_returns200() throws Exception {
        when(medicationStockService.getTotalStock(1L)).thenReturn(0);

        mockMvc.perform(get("/stock/medicament/1/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(0))
                .andExpect(jsonPath("$.inStock").value(false));
    }

    // ─── GET /stock/medicament/{id}/lots ───────────────────────

    @Test
    void getLots_returns200() throws Exception {
        List<MedicationStockDto> lots = List.of(
                MedicationStockDto.builder()
                        .id(10L)
                        .medicamentId(1L)
                        .medicamentName("DOLIPRANE")
                        .numeroLot("LOT-2401-2701")
                        .quantiteEnStock(100)
                        .emplacement("A12")
                        .build()
        );

        when(medicationStockService.getLotsByMedicament(1L)).thenReturn(lots);

        mockMvc.perform(get("/stock/medicament/1/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroLot").value("LOT-2401-2701"))
                .andExpect(jsonPath("$[0].quantiteEnStock").value(100));
    }

    @Test
    void getLots_empty_returns200() throws Exception {
        when(medicationStockService.getLotsByMedicament(1L)).thenReturn(List.of());

        mockMvc.perform(get("/stock/medicament/1/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── POST /stock ───────────────────────────────────────────

    @Test
    void createStock_returns201() throws Exception {
        StockRequest request = new StockRequest();
        request.setMedicamentId(1L);
        request.setNumeroLot("LOT-2401-2701");
        request.setQuantiteEnStock(100);
        request.setEmplacement("A12");

        MedicationStockDto dto = MedicationStockDto.builder()
                .id(10L)
                .medicamentId(1L)
                .medicamentName("DOLIPRANE")
                .numeroLot("LOT-2401-2701")
                .quantiteEnStock(100)
                .emplacement("A12")
                .build();

        when(medicationStockService.createStock(any(StockRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroLot").value("LOT-2401-2701"))
                .andExpect(jsonPath("$.quantiteEnStock").value(100));
    }

    @Test
    void createStock_medicamentNotFound_returns404() throws Exception {
        StockRequest request = new StockRequest();
        request.setMedicamentId(99L);

        when(medicationStockService.createStock(any(StockRequest.class)))
                .thenThrow(new RuntimeException("Medicament not found: 99"));

        mockMvc.perform(post("/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
