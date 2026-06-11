package com.medilinktunisia.laboratoryservice.repository;

import com.medilinktunisia.laboratoryservice.model.entity.Laboratory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {

    Optional<Laboratory> findByUserId(Long userId);

    Optional<Laboratory> findByLicenseNumber(String licenseNumber);

    List<Laboratory> findByCity(String city);

    List<Laboratory> findByStatus(Laboratory.LaboratoryStatus status);

    Page<Laboratory> findByStatus(Laboratory.LaboratoryStatus status, Pageable pageable);

    Page<Laboratory> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT l FROM Laboratory l WHERE l.city = :city AND l.status = :status")
    List<Laboratory> findByCityAndStatus(@Param("city") String city, 
                                         @Param("status") Laboratory.LaboratoryStatus status);

    @Query("SELECT l FROM Laboratory l WHERE " +
           "l.homeCollection = true AND l.status = 'ACTIVE' AND l.city = :city")
    List<Laboratory> findHomeCollectionLaboratoriesInCity(@Param("city") String city);

    @Query("SELECT l FROM Laboratory l WHERE " +
           "l.urgentAnalysisAvailable = true AND l.status = 'ACTIVE'")
    List<Laboratory> findUrgentAnalysisLaboratories();

    // Recherche géolocalisée
    @Query(value = "SELECT * FROM laboratories l WHERE " +
           "l.latitude IS NOT NULL AND l.longitude IS NOT NULL AND " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * " +
           "cos(radians(l.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(l.latitude)))) <= :radiusKm " +
           "AND l.status = 'ACTIVE'",
           nativeQuery = true)
    List<Laboratory> findLaboratoriesWithinRadius(@Param("latitude") double latitude,
                                                   @Param("longitude") double longitude,
                                                   @Param("radiusKm") double radiusKm);

    @Query("SELECT COUNT(l) FROM Laboratory l WHERE l.status = 'ACTIVE'")
    long countActiveLaboratories();
}
