package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Optional<Medication> findByMedicationCode(String medicationCode);

    Page<Medication> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Medication> findByCategory(Medication.MedicationCategory category, Pageable pageable);

    Page<Medication> findByManufacturer(String manufacturer, Pageable pageable);

    List<Medication> findByActiveTrue();

    List<Medication> findByStatus(Medication.MedicationStatus status);

    @Query("SELECT m FROM Medication m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.scientificName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.medicationCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Medication> searchMedications(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT m FROM Medication m WHERE " +
           "m.category = :category AND m.active = true AND m.status = 'AVAILABLE'")
    List<Medication> findAvailableMedicationsByCategory(@Param("category") Medication.MedicationCategory category);

    @Query("SELECT m FROM Medication m WHERE m.requiresPrescription = false AND m.active = true")
    List<Medication> findOverTheCounterMedications();

    @Query("SELECT COUNT(m) FROM Medication m WHERE m.status = 'AVAILABLE'")
    long countAvailableMedications();

    @Query("SELECT m.category, COUNT(m) FROM Medication m WHERE m.active = true GROUP BY m.category")
    List<Object[]> countMedicationsByCategory();
}
