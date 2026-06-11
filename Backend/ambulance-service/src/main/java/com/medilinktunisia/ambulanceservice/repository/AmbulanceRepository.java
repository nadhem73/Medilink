package com.medilinktunisia.ambulanceservice.repository;

import com.medilinktunisia.ambulanceservice.model.entity.Ambulance;
import com.medilinktunisia.ambulanceservice.model.enums.AmbulanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {

    Optional<Ambulance> findByRegistrationNumber(String registrationNumber);

    List<Ambulance> findByStatus(AmbulanceStatus status);

    List<Ambulance> findByActiveTrue();

    List<Ambulance> findByActiveTrueAndStatus(AmbulanceStatus status);

    /**
     * Trouve les ambulances disponibles près d'une localisation
     * Utilise la formule de Haversine pour calculer la distance
     */
    @Query(value = """
        SELECT * FROM ambulances a
        WHERE a.active = true
        AND a.status = 'AVAILABLE'
        AND (6371 * acos(cos(radians(:lat)) * cos(radians(a.current_latitude))
        * cos(radians(a.current_longitude) - radians(:lon))
        + sin(radians(:lat)) * sin(radians(a.current_latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(a.current_latitude))
        * cos(radians(a.current_longitude) - radians(:lon))
        + sin(radians(:lat)) * sin(radians(a.current_latitude))))
        """, nativeQuery = true)
    List<Ambulance> findAvailableNearby(
        @Param("lat") Double latitude,
        @Param("lon") Double longitude,
        @Param("radiusKm") Double radiusKm
    );

    @Query(value = """
        SELECT a.*, 
        (6371 * acos(cos(radians(:lat)) * cos(radians(a.current_latitude))
        * cos(radians(a.current_longitude) - radians(:lon))
        + sin(radians(:lat)) * sin(radians(a.current_latitude)))) as distance
        FROM ambulances a
        WHERE a.active = true AND a.status = 'AVAILABLE'
        ORDER BY distance
        LIMIT 1
        """, nativeQuery = true)
    Optional<Ambulance> findNearestAvailable(
        @Param("lat") Double latitude,
        @Param("lon") Double longitude
    );
}
