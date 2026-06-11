package com.medilinktunisia.pharmacyservice.service;

import com.medilinktunisia.pharmacyservice.exception.MedicationNotFoundException;
import com.medilinktunisia.pharmacyservice.exception.PharmacyNotFoundException;
import com.medilinktunisia.pharmacyservice.exception.StockNotFoundException;
import com.medilinktunisia.pharmacyservice.exception.UnauthorizedAccessException;
import com.medilinktunisia.pharmacyservice.model.dto.StockCreateRequest;
import com.medilinktunisia.pharmacyservice.model.dto.StockDto;
import com.medilinktunisia.pharmacyservice.model.dto.StockUpdateRequest;
import com.medilinktunisia.pharmacyservice.model.entity.Medication;
import com.medilinktunisia.pharmacyservice.model.entity.MedicationStock;
import com.medilinktunisia.pharmacyservice.model.entity.Pharmacy;
import com.medilinktunisia.pharmacyservice.repository.MedicationRepository;
import com.medilinktunisia.pharmacyservice.repository.MedicationStockRepository;
import com.medilinktunisia.pharmacyservice.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des stocks de médicaments
 * Selon cahier des charges Section 6.4 - Gestion des stocks avec alertes automatiques
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicationStockService {

    private final MedicationStockRepository stockRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicationRepository medicationRepository;

    /**
     * Ajouter un médicament au stock
     */
    public StockDto addMedicationToStock(StockCreateRequest request, Long currentUserId) {
        log.info("Adding medication to stock for pharmacy: {}", request.getPharmacyId());

        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException("Pharmacie non trouvée"));

        // Vérifier l'autorisation
        if (!pharmacy.getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à gérer le stock de cette pharmacie");
        }

        Medication medication = medicationRepository.findById(request.getMedicationId())
                .orElseThrow(() -> new MedicationNotFoundException("Médicament non trouvé"));

        // Vérifier si le stock existe déjà
        if (stockRepository.findByPharmacyIdAndMedicationId(
                request.getPharmacyId(), request.getMedicationId()).isPresent()) {
            throw new IllegalArgumentException("Ce médicament existe déjà dans le stock");
        }

        MedicationStock stock = MedicationStock.builder()
                .pharmacy(pharmacy)
                .medication(medication)
                .quantity(request.getQuantity())
                .minimumStockLevel(request.getMinimumStockLevel() != null ? request.getMinimumStockLevel() : 10)
                .reorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 50)
                .maximumStockLevel(request.getMaximumStockLevel() != null ? request.getMaximumStockLevel() : 500)
                .sellingPrice(request.getSellingPrice())
                .purchasePrice(request.getPurchasePrice())
                .batchNumber(request.getBatchNumber())
                .expiryDate(request.getExpiryDate())
                .shelfLocation(request.getShelfLocation())
                .build();

        stock.updateStatus();
        stock = stockRepository.save(stock);

        log.info("Medication added to stock successfully with ID: {}", stock.getId());
        return mapToDto(stock);
    }

    /**
     * Mettre à jour le stock
     */
    public StockDto updateStock(Long stockId, StockUpdateRequest request, Long currentUserId) {
        log.info("Updating stock ID: {}", stockId);

        MedicationStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException("Stock non trouvé"));

        // Vérifier l'autorisation
        if (!stock.getPharmacy().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier ce stock");
        }

        // Mettre à jour les champs
        if (request.getQuantity() != null) {
            stock.setQuantity(request.getQuantity());
        }
        if (request.getMinimumStockLevel() != null) {
            stock.setMinimumStockLevel(request.getMinimumStockLevel());
        }
        if (request.getReorderLevel() != null) {
            stock.setReorderLevel(request.getReorderLevel());
        }
        if (request.getMaximumStockLevel() != null) {
            stock.setMaximumStockLevel(request.getMaximumStockLevel());
        }
        if (request.getSellingPrice() != null) {
            stock.setSellingPrice(request.getSellingPrice());
        }
        if (request.getPurchasePrice() != null) {
            stock.setPurchasePrice(request.getPurchasePrice());
        }
        if (request.getBatchNumber() != null) {
            stock.setBatchNumber(request.getBatchNumber());
        }
        if (request.getExpiryDate() != null) {
            stock.setExpiryDate(request.getExpiryDate());
        }
        if (request.getShelfLocation() != null) {
            stock.setShelfLocation(request.getShelfLocation());
        }
        if (request.getNotes() != null) {
            stock.setNotes(request.getNotes());
        }

        stock.updateStatus();
        stock = stockRepository.save(stock);

        log.info("Stock updated successfully");
        return mapToDto(stock);
    }

    /**
     * Augmenter la quantité en stock (réapprovisionnement)
     */
    public StockDto increaseStock(Long stockId, Integer quantity, Long currentUserId) {
        log.info("Increasing stock ID: {} by {}", stockId, quantity);

        MedicationStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException("Stock non trouvé"));

        // Vérifier l'autorisation
        if (!stock.getPharmacy().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier ce stock");
        }

        stock.increaseQuantity(quantity);
        stock = stockRepository.save(stock);

        log.info("Stock increased successfully");
        return mapToDto(stock);
    }

    /**
     * Diminuer la quantité en stock (vente/dispensation)
     */
    public StockDto decreaseStock(Long stockId, Integer quantity, Long currentUserId) {
        log.info("Decreasing stock ID: {} by {}", stockId, quantity);

        MedicationStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException("Stock non trouvé"));

        // Vérifier l'autorisation
        if (!stock.getPharmacy().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à modifier ce stock");
        }

        stock.decreaseQuantity(quantity);
        stock = stockRepository.save(stock);

        log.info("Stock decreased successfully");
        return mapToDto(stock);
    }

    /**
     * Obtenir le stock par ID
     */
    @Transactional(readOnly = true)
    public StockDto getStockById(Long id) {
        MedicationStock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock non trouvé"));
        return mapToDto(stock);
    }

    /**
     * Obtenir tous les stocks d'une pharmacie
     */
    @Transactional(readOnly = true)
    public Page<StockDto> getPharmacyStock(Long pharmacyId, Pageable pageable) {
        return stockRepository.findByPharmacyId(pharmacyId, pageable).map(this::mapToDto);
    }

    /**
     * Obtenir les stocks faibles (nécessitant réapprovisionnement)
     */
    @Transactional(readOnly = true)
    public List<StockDto> getLowStock(Long pharmacyId) {
        return stockRepository.findLowStockByPharmacy(pharmacyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les stocks critiques
     */
    @Transactional(readOnly = true)
    public List<StockDto> getCriticalStock(Long pharmacyId) {
        return stockRepository.findCriticalStockByPharmacy(pharmacyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les stocks en rupture
     */
    @Transactional(readOnly = true)
    public List<StockDto> getOutOfStock(Long pharmacyId) {
        return stockRepository.findOutOfStockByPharmacy(pharmacyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les médicaments arrivant à expiration (3 mois)
     */
    @Transactional(readOnly = true)
    public List<StockDto> getExpiringStock(Long pharmacyId) {
        LocalDate now = LocalDate.now();
        LocalDate threeMonthsLater = now.plusMonths(3);
        
        return stockRepository.findExpiringStockByPharmacy(pharmacyId, now, threeMonthsLater).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les médicaments expirés
     */
    @Transactional(readOnly = true)
    public List<StockDto> getExpiredStock(Long pharmacyId) {
        return stockRepository.findExpiredStockByPharmacy(pharmacyId, LocalDate.now()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher dans le stock disponible
     */
    @Transactional(readOnly = true)
    public Page<StockDto> searchAvailableStock(Long pharmacyId, String searchTerm, Pageable pageable) {
        return stockRepository.searchAvailableStock(pharmacyId, searchTerm, pageable)
                .map(this::mapToDto);
    }

    /**
     * Vérifier la disponibilité d'un médicament
     */
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(Long pharmacyId, Long medicationId, Integer quantity) {
        return stockRepository.isStockAvailable(pharmacyId, medicationId, quantity);
    }

    /**
     * Obtenir la valeur totale du stock
     */
    @Transactional(readOnly = true)
    public Double getTotalStockValue(Long pharmacyId) {
        Double value = stockRepository.calculateTotalStockValue(pharmacyId);
        return value != null ? value : 0.0;
    }

    /**
     * Supprimer un médicament du stock
     */
    public void deleteStock(Long stockId, Long currentUserId) {
        log.info("Deleting stock ID: {}", stockId);

        MedicationStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException("Stock non trouvé"));

        // Vérifier l'autorisation
        if (!stock.getPharmacy().getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Vous n'êtes pas autorisé à supprimer ce stock");
        }

        stockRepository.delete(stock);
        log.info("Stock deleted successfully");
    }

    /**
     * Mapper MedicationStock vers StockDto
     */
    private StockDto mapToDto(MedicationStock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .pharmacyId(stock.getPharmacy().getId())
                .pharmacyName(stock.getPharmacy().getName())
                .medicationId(stock.getMedication().getId())
                .medicationName(stock.getMedication().getName())
                .medicationCode(stock.getMedication().getMedicationCode())
                .quantity(stock.getQuantity())
                .minimumStockLevel(stock.getMinimumStockLevel())
                .reorderLevel(stock.getReorderLevel())
                .maximumStockLevel(stock.getMaximumStockLevel())
                .sellingPrice(stock.getSellingPrice())
                .purchasePrice(stock.getPurchasePrice())
                .batchNumber(stock.getBatchNumber())
                .expiryDate(stock.getExpiryDate())
                .shelfLocation(stock.getShelfLocation())
                .status(stock.getStatus())
                .totalSold(stock.getTotalSold())
                .lastSaleDate(stock.getLastSaleDate())
                .notes(stock.getNotes())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
