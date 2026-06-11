package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.entity.MedicationStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationStockRepository extends JpaRepository<MedicationStock, Long> {

    Optional<MedicationStock> findByPharmacyIdAndMedicationId(Long pharmacyId, Long medicationId);

    List<MedicationStock> findByPharmacyId(Long pharmacyId);

    Page<MedicationStock> findByPharmacyId(Long pharmacyId, Pageable pageable);

    List<MedicationStock> findByMedicationId(Long medicationId);

    @Query("SELECT s FROM MedicationStock s WHERE s.pharmacy.id = :pharmacyId AND s.status = :status")
    List<MedicationStock> findByPharmacyIdAndStatus(@Param("pharmacyId") Long pharmacyId,
                                                     @Param("status") MedicationStock.StockStatus status);

    // Stocks faibles nécessitant réapprovisionnement
    @Query("SELECT s FROM MedicationStock s WHERE " +
           "s.pharmacy.id = :pharmacyId AND s.quantity <= s.reorderLevel")
    List<MedicationStock> findLowStockByPharmacy(@Param("pharmacyId") Long pharmacyId);

    // Stocks critiques
    @Query("SELECT s FROM MedicationStock s WHERE " +
           "s.pharmacy.id = :pharmacyId AND s.quantity <= s.minimumStockLevel")
    List<MedicationStock> findCriticalStockByPharmacy(@Param("pharmacyId") Long pharmacyId);

    // Stocks en rupture
    @Query("SELECT s FROM MedicationStock s WHERE " +
           "s.pharmacy.id = :pharmacyId AND s.quantity = 0")
    List<MedicationStock> findOutOfStockByPharmacy(@Param("pharmacyId") Long pharmacyId);

    // Médicaments arrivant à expiration
    @Query("SELECT s FROM MedicationStock s WHERE " +
           "s.pharmacy.id = :pharmacyId AND s.expiryDate IS NOT NULL AND " +
           "s.expiryDate BETWEEN :startDate AND :endDate")
    List<MedicationStock> findExpiringStockByPharmacy(@Param("pharmacyId") Long pharmacyId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    // Médicaments expirés
    @Query("SELECT s FROM MedicationStock s WHERE " +
           "s.pharmacy.id = :pharmacyId AND s.expiryDate IS NOT NULL AND " +
           "s.expiryDate < :currentDate")
    List<MedicationStock> findExpiredStockByPharmacy(@Param("pharmacyId") Long pharmacyId,
                                                      @Param("currentDate") LocalDate currentDate);

    // Recherche de médicaments disponibles dans une pharmacie
    @Query("SELECT s FROM MedicationStock s " +
           "JOIN s.medication m WHERE " +
           "s.pharmacy.id = :pharmacyId AND " +
           "s.quantity > 0 AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.scientificName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<MedicationStock> searchAvailableStock(@Param("pharmacyId") Long pharmacyId,
                                                @Param("searchTerm") String searchTerm,
                                                Pageable pageable);

    // Statistiques
    @Query("SELECT COUNT(s) FROM MedicationStock s WHERE " +
           "s.pharmacy.id = :pharmacyId AND s.quantity > 0")
    long countAvailableStock(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT SUM(s.quantity * s.sellingPrice) FROM MedicationStock s WHERE " +
           "s.pharmacy.id = :pharmacyId")
    Double calculateTotalStockValue(@Param("pharmacyId") Long pharmacyId);

    // Vérifier la disponibilité d'un médicament
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM MedicationStock s WHERE " +
           "s.pharmacy.id = :pharmacyId AND s.medication.id = :medicationId AND s.quantity >= :quantity")
    boolean isStockAvailable(@Param("pharmacyId") Long pharmacyId,
                            @Param("medicationId") Long medicationId,
                            @Param("quantity") Integer quantity);

    // Top médicaments vendus
    @Query("SELECT s FROM MedicationStock s WHERE s.pharmacy.id = :pharmacyId " +
           "ORDER BY s.totalSold DESC")
    Page<MedicationStock> findTopSellingMedications(@Param("pharmacyId") Long pharmacyId, 
                                                     Pageable pageable);
}
