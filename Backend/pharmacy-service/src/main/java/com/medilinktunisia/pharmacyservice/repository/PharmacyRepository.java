package com.medilinktunisia.pharmacyservice.repository;

import com.medilinktunisia.pharmacyservice.model.entity.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    Optional<Pharmacy> findByUserId(Long userId);

    Optional<Pharmacy> findByLicenseNumber(String licenseNumber);

    List<Pharmacy> findByCity(String city);

    List<Pharmacy> findByStatus(Pharmacy.PharmacyStatus status);

    Page<Pharmacy> findByStatus(Pharmacy.PharmacyStatus status, Pageable pageable);

    Page<Pharmacy> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Pharmacy p WHERE p.city = :city AND p.status = :status")
    List<Pharmacy> findByCityAndStatus(@Param("city") String city, 
                                       @Param("status") Pharmacy.PharmacyStatus status);

    @Query("SELECT p FROM Pharmacy p WHERE " +
           "p.nightService = true AND p.status = 'ACTIVE'")
    List<Pharmacy> findNightServicePharmacies();

    @Query("SELECT p FROM Pharmacy p WHERE " +
           "p.homeDelivery = true AND p.status = 'ACTIVE' AND p.city = :city")
    List<Pharmacy> findHomeDeliveryPharmaciesInCity(@Param("city") String city);

    // Recherche géolocalisée (pharmacies dans un rayon)
    @Query(value = "SELECT * FROM pharmacies p WHERE " +
           "p.latitude IS NOT NULL AND p.longitude IS NOT NULL AND " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(p.latitude)))) <= :radiusKm " +
           "AND p.status = 'ACTIVE'",
           nativeQuery = true)
    List<Pharmacy> findPharmaciesWithinRadius(@Param("latitude") double latitude,
                                               @Param("longitude") double longitude,
                                               @Param("radiusKm") double radiusKm);

    @Query("SELECT COUNT(p) FROM Pharmacy p WHERE p.status = 'ACTIVE'")
    long countActivePharmacies();

    @Query("SELECT p.city, COUNT(p) FROM Pharmacy p WHERE p.status = 'ACTIVE' GROUP BY p.city")
    List<Object[]> countPharmaciesByCity();
}
