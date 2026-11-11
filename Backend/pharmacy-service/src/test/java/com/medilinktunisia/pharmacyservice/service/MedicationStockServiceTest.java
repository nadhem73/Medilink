package com.medilinktunisia.pharmacyservice.service;

import com.medilinktunisia.pharmacyservice.dto.DispensationItem;
import com.medilinktunisia.pharmacyservice.dto.DispensationResult;
import com.medilinktunisia.pharmacyservice.dto.MedicationStockDto;
import com.medilinktunisia.pharmacyservice.dto.StockRuptureAlert;
import com.medilinktunisia.pharmacyservice.model.Medicament;
import com.medilinktunisia.pharmacyservice.model.Stock;
import com.medilinktunisia.pharmacyservice.repository.MedicamentRepository;
import com.medilinktunisia.pharmacyservice.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationStockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private MedicamentRepository medicamentRepository;

    @InjectMocks
    private MedicationStockService medicationStockService;

    private Medicament createMedicament(Long id, String name) {
        Medicament m = new Medicament();
        m.setId(id);
        m.setName(name);
        return m;
    }

    private Stock createStock(Long id, Medicament medicament, String lot, int qte,
                              LocalDate fab, LocalDate exp, String emplacement) {
        Stock s = new Stock();
        s.setId(id);
        s.setMedicament(medicament);
        s.setNumeroLot(lot);
        s.setQuantiteEnStock(qte);
        s.setDateFabrication(fab);
        s.setDateExpiration(exp);
        s.setEmplacement(emplacement);
        return s;
    }

    // ─── dispenserStock ────────────────────────────────────────

    @Test
    void dispenserStock_success_singleLot() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        Stock lot = createStock(10L, med, "LOT-2401-2701", 100,
                LocalDate.of(2024, 1, 15), LocalDate.of(2027, 1, 15), "A12");

        when(medicamentRepository.findById(1L)).thenReturn(Optional.of(med));
        when(stockRepository.findAvailableLotsFifo(1L)).thenReturn(List.of(lot));

        DispensationResult result = medicationStockService.dispenserStock(
                List.of(new DispensationItem(1L, 30)));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDeductions()).hasSize(1);
        assertThat(result.getDeductions().get(0).getQuantitePrelevee()).isEqualTo(30);
        assertThat(result.getDeductions().get(0).getStockRestant()).isEqualTo(70);
        assertThat(lot.getQuantiteEnStock()).isEqualTo(70);
        verify(stockRepository).save(lot);
    }

    @Test
    void dispenserStock_success_multipleLots_fifoOrder() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        Stock ancien = createStock(10L, med, "LOT-2306-2606", 50,
                LocalDate.of(2023, 6, 1), LocalDate.of(2026, 6, 1), "A12");
        Stock recent = createStock(11L, med, "LOT-2406-2706", 50,
                LocalDate.of(2024, 6, 1), LocalDate.of(2027, 6, 1), "B12");

        when(medicamentRepository.findById(1L)).thenReturn(Optional.of(med));
        when(stockRepository.findAvailableLotsFifo(1L)).thenReturn(List.of(ancien, recent));

        DispensationResult result = medicationStockService.dispenserStock(
                List.of(new DispensationItem(1L, 80)));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDeductions()).hasSize(2);
        // First deduction: 50 from ancien lot (entire lot)
        assertThat(result.getDeductions().get(0).getStockId()).isEqualTo(10L);
        assertThat(result.getDeductions().get(0).getQuantitePrelevee()).isEqualTo(50);
        assertThat(result.getDeductions().get(0).getStockRestant()).isEqualTo(0);
        // Second deduction: 30 from recent lot
        assertThat(result.getDeductions().get(1).getStockId()).isEqualTo(11L);
        assertThat(result.getDeductions().get(1).getQuantitePrelevee()).isEqualTo(30);
        assertThat(result.getDeductions().get(1).getStockRestant()).isEqualTo(20);
        verify(stockRepository, times(2)).save(any(Stock.class));
    }

    @Test
    void dispenserStock_insufficientStock_throwsException() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        Stock lot = createStock(10L, med, "LOT-2401-2701", 10,
                LocalDate.of(2024, 1, 15), LocalDate.of(2027, 1, 15), "A12");

        when(medicamentRepository.findById(1L)).thenReturn(Optional.of(med));
        when(stockRepository.findAvailableLotsFifo(1L)).thenReturn(List.of(lot));

        assertThatThrownBy(() -> medicationStockService.dispenserStock(
                List.of(new DispensationItem(1L, 50))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuffisant")
                .hasMessageContaining("DOLIPRANE");
    }

    @Test
    void dispenserStock_medicamentNotFound_throwsException() {
        when(medicamentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationStockService.dispenserStock(
                List.of(new DispensationItem(99L, 1))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Medicament not found");
    }

    @Test
    void dispenserStock_multipleMedicaments_success() {
        Medicament medA = createMedicament(1L, "DOLIPRANE");
        Medicament medB = createMedicament(2L, "AMOXICILLINE");
        Stock lotA = createStock(10L, medA, "LOT-2401-2701", 100,
                LocalDate.of(2024, 1, 15), LocalDate.of(2027, 1, 15), "A12");
        Stock lotB = createStock(11L, medB, "LOT-2402-2702", 50,
                LocalDate.of(2024, 2, 1), LocalDate.of(2027, 2, 1), "B12");

        when(medicamentRepository.findById(1L)).thenReturn(Optional.of(medA));
        when(medicamentRepository.findById(2L)).thenReturn(Optional.of(medB));
        when(stockRepository.findAvailableLotsFifo(1L)).thenReturn(List.of(lotA));
        when(stockRepository.findAvailableLotsFifo(2L)).thenReturn(List.of(lotB));

        DispensationResult result = medicationStockService.dispenserStock(List.of(
                new DispensationItem(1L, 20),
                new DispensationItem(2L, 30)));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDeductions()).hasSize(2);
        assertThat(lotA.getQuantiteEnStock()).isEqualTo(80);
        assertThat(lotB.getQuantiteEnStock()).isEqualTo(20);
    }

    // ─── getRuptureAlerts ──────────────────────────────────────

    @Test
    void getRuptureAlerts_belowThreshold_returnsAlerts() {
        Medicament medA = createMedicament(1L, "DOLIPRANE");
        Medicament medB = createMedicament(2L, "AMOXICILLINE");
        Stock lotA = createStock(10L, medA, "LOT-2401-2701", 5, null, null, null);
        Stock lotB = createStock(11L, medB, "LOT-2402-2702", 100, null, null, null);

        when(stockRepository.findAll()).thenReturn(List.of(lotA, lotB));

        List<StockRuptureAlert> alerts = medicationStockService.getRuptureAlerts(20);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMedicamentName()).isEqualTo("DOLIPRANE");
        assertThat(alerts.get(0).getStockTotal()).isEqualTo(5);
    }

    @Test
    void getRuptureAlerts_allAboveThreshold_returnsEmpty() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        Stock lot = createStock(10L, med, "LOT-2401-2701", 100, null, null, null);

        when(stockRepository.findAll()).thenReturn(List.of(lot));

        List<StockRuptureAlert> alerts = medicationStockService.getRuptureAlerts(20);

        assertThat(alerts).isEmpty();
    }

    @Test
    void getRuptureAlerts_zeroStock_returnsRuptureStatus() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        Stock lot = createStock(10L, med, "LOT-2401-2701", 0, null, null, null);

        when(stockRepository.findAll()).thenReturn(List.of(lot));

        List<StockRuptureAlert> alerts = medicationStockService.getRuptureAlerts(20);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo("Rupture");
    }

    @Test
    void getRuptureAlerts_criticalStock_returnsCritiqueStatus() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        Stock lot = createStock(10L, med, "LOT-2401-2701", 5, null, null, null);

        when(stockRepository.findAll()).thenReturn(List.of(lot));

        List<StockRuptureAlert> alerts = medicationStockService.getRuptureAlerts(20);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo("Critique");
    }

    // ─── getPerimesAlerts ──────────────────────────────────────

    @Test
    void getPerimesAlerts_expiringSoon_returnsLots() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        LocalDate today = LocalDate.now();
        Stock nearExpiry = createStock(10L, med, "LOT-2401-2701", 100,
                LocalDate.of(2024, 1, 15), today.plusDays(15), "A12");

        when(stockRepository.findByDateExpirationBefore(any(LocalDate.class)))
                .thenReturn(List.of(nearExpiry));

        List<MedicationStockDto> alerts = medicationStockService.getPerimesAlerts(30);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMedicamentName()).isEqualTo("DOLIPRANE");
    }

    @Test
    void getPerimesAlerts_noneExpiring_returnsEmpty() {
        when(stockRepository.findByDateExpirationBefore(any(LocalDate.class)))
                .thenReturn(List.of());

        List<MedicationStockDto> alerts = medicationStockService.getPerimesAlerts(30);

        assertThat(alerts).isEmpty();
    }

    @Test
    void getPerimesAlerts_filtersPastExpiration() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        Stock expired = createStock(10L, med, "LOT-2001-2101", 5,
                LocalDate.of(2020, 1, 15), LocalDate.now().minusDays(5), "A12");

        when(stockRepository.findByDateExpirationBefore(any(LocalDate.class)))
                .thenReturn(List.of(expired));

        List<MedicationStockDto> alerts = medicationStockService.getPerimesAlerts(30);

        assertThat(alerts).isEmpty();
    }

    // ─── getTotalStock ─────────────────────────────────────────

    @Test
    void getTotalStock_sumsAllLots() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        when(stockRepository.findByMedicamentId(1L)).thenReturn(List.of(
                createStock(10L, med, "LOT-2401-2701", 50, null, null, null),
                createStock(11L, med, "LOT-2402-2702", 30, null, null, null)
        ));

        Integer total = medicationStockService.getTotalStock(1L);

        assertThat(total).isEqualTo(80);
    }

    @Test
    void getTotalStock_noLots_returnsZero() {
        when(stockRepository.findByMedicamentId(1L)).thenReturn(List.of());

        Integer total = medicationStockService.getTotalStock(1L);

        assertThat(total).isEqualTo(0);
    }

    // ─── isInStock ─────────────────────────────────────────────

    @Test
    void isInStock_positiveStock_returnsTrue() {
        Medicament med = createMedicament(1L, "DOLIPRANE");
        when(stockRepository.findByMedicamentId(1L)).thenReturn(List.of(
                createStock(10L, med, "LOT-2401-2701", 10, null, null, null)
        ));

        assertThat(medicationStockService.isInStock(1L)).isTrue();
    }

    @Test
    void isInStock_zeroStock_returnsFalse() {
        when(stockRepository.findByMedicamentId(1L)).thenReturn(List.of());

        assertThat(medicationStockService.isInStock(1L)).isFalse();
    }
}
