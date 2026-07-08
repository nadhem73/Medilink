package com.medilinktunisia.pharmacyservice.service;

import com.medilinktunisia.pharmacyservice.dto.MedicationStockDto;
import com.medilinktunisia.pharmacyservice.model.Stock;
import com.medilinktunisia.pharmacyservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationStockService {

    private final StockRepository stockRepository;

    public Integer getTotalStock(Long medicamentId) {
        return stockRepository.findByMedicamentId(medicamentId).stream()
                .mapToInt(Stock::getQuantiteEnStock)
                .sum();
    }

    public List<MedicationStockDto> getLotsByMedicament(Long medicamentId) {
        List<Stock> lots = stockRepository.findByMedicamentId(medicamentId);
        return lots.stream().map(this::toDto).toList();
    }

    public boolean isInStock(Long medicamentId) {
        return getTotalStock(medicamentId) > 0;
    }

    private MedicationStockDto toDto(Stock s) {
        return MedicationStockDto.builder()
                .id(s.getId())
                .medicamentId(s.getMedicament().getId())
                .medicamentName(s.getMedicament().getName())
                .quantiteEnStock(s.getQuantiteEnStock())
                .dateFabrication(s.getDateFabrication())
                .dateExpiration(s.getDateExpiration())
                .emplacement(s.getEmplacement())
                .build();
    }
}
