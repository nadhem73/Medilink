package com.medilinktunisia.pharmacyservice.service;

import com.medilinktunisia.pharmacyservice.dto.*;
import com.medilinktunisia.pharmacyservice.model.Medicament;
import com.medilinktunisia.pharmacyservice.model.Stock;
import com.medilinktunisia.pharmacyservice.repository.MedicamentRepository;
import com.medilinktunisia.pharmacyservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationStockService {

    private final StockRepository stockRepository;
    private final MedicamentRepository medicamentRepository;

    public Integer getTotalStock(Long medicamentId) {
        return stockRepository.findByMedicamentId(medicamentId).stream()
                .mapToInt(Stock::getQuantiteEnStock)
                .sum();
    }

    @Transactional(readOnly = true)
    public List<MedicationStockDto> getLotsByMedicament(Long medicamentId) {
        List<Stock> lots = stockRepository.findByMedicamentId(medicamentId);
        return lots.stream().map(this::toDto).toList();
    }

    @Transactional
    public MedicationStockDto createStock(StockRequest request) {
        Medicament medicament = medicamentRepository.findById(request.getMedicamentId())
                .orElseThrow(() -> new RuntimeException("Medicament not found: " + request.getMedicamentId()));
        Stock stock = new Stock();
        stock.setMedicament(medicament);
        String lot = request.getNumeroLot();
        if (lot == null || lot.isBlank()) {
            lot = generateNumeroLot(request.getDateFabrication(), request.getDateExpiration());
        }
        stock.setNumeroLot(lot);
        stock.setQuantiteEnStock(request.getQuantiteEnStock() != null ? request.getQuantiteEnStock() : 0);
        stock.setDateFabrication(request.getDateFabrication());
        stock.setDateExpiration(request.getDateExpiration());
        stock.setEmplacement(request.getEmplacement());
        Stock saved = stockRepository.save(stock);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<StockRuptureAlert> getRuptureAlerts(int seuil) {
        Map<Medicament, Integer> totals = stockRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Stock::getMedicament,
                        Collectors.summingInt(Stock::getQuantiteEnStock)
                ));
        return totals.entrySet().stream()
                .filter(e -> e.getValue() < seuil)
                .map(e -> {
                    int stock = e.getValue();
                    String status = stock == 0 ? "Rupture" : stock <= seuil / 2 ? "Critique" : "Faible";
                    return StockRuptureAlert.builder()
                            .medicamentId(e.getKey().getId())
                            .medicamentName(e.getKey().getName())
                            .stockTotal(stock)
                            .status(status)
                            .build();
                })
                .sorted((a, b) -> a.getMedicamentName().compareToIgnoreCase(b.getMedicamentName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicationStockDto> getPerimesAlerts(int jours) {
        LocalDate today = LocalDate.now();
        LocalDate limite = today.plusDays(jours);
        return stockRepository.findByDateExpirationBefore(limite).stream()
                .filter(s -> s.getDateExpiration() != null && !s.getDateExpiration().isBefore(today))
                .map(this::toDto)
                .toList();
    }

    private String generateNumeroLot(LocalDate fab, LocalDate exp) {
        String fabPart = fab != null
                ? String.format("%02d%02d", fab.getYear() % 100, fab.getMonthValue())
                : "0000";
        String expPart = exp != null
                ? String.format("%02d%02d", exp.getYear() % 100, exp.getMonthValue())
                : "0000";
        return "LOT-" + fabPart + "-" + expPart;
    }

    public boolean isInStock(Long medicamentId) {
        return getTotalStock(medicamentId) > 0;
    }

    @Transactional
    public DispensationResult dispenserStock(List<DispensationItem> items) {
        List<LotDeduction> deductions = new ArrayList<>();

        for (DispensationItem item : items) {
            Medicament medicament = medicamentRepository.findById(item.getMedicamentId())
                    .orElseThrow(() -> new RuntimeException("Medicament not found: " + item.getMedicamentId()));

            List<Stock> lots = stockRepository.findAvailableLotsFifo(item.getMedicamentId());
            int remaining = item.getQuantite();

            for (Stock lot : lots) {
                if (remaining <= 0) break;

                int taken = Math.min(remaining, lot.getQuantiteEnStock());
                lot.setQuantiteEnStock(lot.getQuantiteEnStock() - taken);
                stockRepository.save(lot);
                remaining -= taken;

                deductions.add(LotDeduction.builder()
                        .stockId(lot.getId())
                        .numeroLot(lot.getNumeroLot())
                        .medicamentId(medicament.getId())
                        .quantitePrelevee(taken)
                        .stockRestant(lot.getQuantiteEnStock())
                        .build());
            }

            if (remaining > 0) {
                throw new RuntimeException("Stock insuffisant pour : " + medicament.getName()
                        + " (manque " + remaining + " unites)");
            }
        }

        return DispensationResult.builder()
                .success(true)
                .deductions(deductions)
                .build();
    }

    private MedicationStockDto toDto(Stock s) {
        String lot = s.getNumeroLot();
        if (lot == null || lot.isBlank()) {
            lot = generateNumeroLot(s.getDateFabrication(), s.getDateExpiration());
        }
        return MedicationStockDto.builder()
                .id(s.getId())
                .medicamentId(s.getMedicament().getId())
                .medicamentName(s.getMedicament().getName())
                .numeroLot(lot)
                .quantiteEnStock(s.getQuantiteEnStock())
                .dateFabrication(s.getDateFabrication())
                .dateExpiration(s.getDateExpiration())
                .emplacement(s.getEmplacement())
                .build();
    }
}
