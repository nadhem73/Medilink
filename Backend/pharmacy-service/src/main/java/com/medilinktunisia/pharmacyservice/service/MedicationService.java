package com.medilinktunisia.pharmacyservice.service;

import com.medilinktunisia.pharmacyservice.dto.MedicationDto;
import com.medilinktunisia.pharmacyservice.model.Medicament;
import com.medilinktunisia.pharmacyservice.model.Stock;
import com.medilinktunisia.pharmacyservice.repository.MedicamentRepository;
import com.medilinktunisia.pharmacyservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicamentRepository medicamentRepository;
    private final StockRepository stockRepository;

    public Page<MedicationDto> searchByName(String name, Pageable pageable) {
        Page<Medicament> page = medicamentRepository.findByNameContainingIgnoreCase(name, pageable);
        Map<Long, Integer> stockMap = buildStockMap();
        return page.map(m -> toDto(m, stockMap.getOrDefault(m.getId(), 0)));
    }

    public MedicationDto getById(Long id) {
        Medicament medicament = medicamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicament not found: " + id));
        int stockTotal = stockRepository.findByMedicamentId(id).stream()
                .mapToInt(Stock::getQuantiteEnStock)
                .sum();
        return toDto(medicament, stockTotal);
    }

    public Map<Long, Integer> getStockForMedicaments(List<Long> medicamentIds) {
        return medicamentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> stockRepository.findByMedicamentId(id).stream()
                                .mapToInt(Stock::getQuantiteEnStock)
                                .sum()
                ));
    }

    private Map<Long, Integer> buildStockMap() {
        return stockRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getMedicament().getId(),
                        Collectors.summingInt(Stock::getQuantiteEnStock)
                ));
    }

    private MedicationDto toDto(Medicament m, int stockTotal) {
        return MedicationDto.builder()
                .id(m.getId())
                .name(m.getName())
                .dosage(m.getDosage())
                .forme(m.getForme())
                .presentation(m.getPresentation())
                .price(m.getPrice())
                .remboursement(m.getRemboursement())
                .dci(m.getDci())
                .type(m.getType())
                .prescriptionRequired(m.getPrescriptionRequired())
                .stockTotal(stockTotal)
                .voieAdministration(mapFormeToVoies(m.getForme()))
                .build();
    }

    private Set<String> mapFormeToVoies(String forme) {
        if (forme == null) return Set.of("Orale");
        String f = forme.toLowerCase();

        if (f.contains("injectable") || f.contains("injection") || f.contains("perfusion"))
            return Set.of("Injectable");

        if (f.contains("collyre") || f.contains("goutte") && (f.contains("ophtalmi") || f.contains("oculaire")))
            return Set.of("Oculaire");

        if (f.contains("crème") || f.contains("pommade") || f.contains("gel dermique")
                || f.contains("lotion") || f.contains("solution externe")
                || f.contains("solution gynécologique") || f.contains("poudre à usage externe"))
            return Set.of("Cutanée");

        if (f.contains("aérosol") || f.contains("aerosol") || (f.contains("inhalation") && !f.contains("nasale")))
            return Set.of("Inhalée");

        if (f.contains("spray nasal") || f.contains("suspension nasale"))
            return Set.of("Nasale");

        if (f.contains("suppositoire"))
            return Set.of("Rectale");

        if (f.contains("orodispersible") || f.contains("à sucer") || f.contains("sublingual"))
            return Set.of("Orale", "Sublinguale");

        return Set.of("Orale");
    }
}
