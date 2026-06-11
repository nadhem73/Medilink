package com.medilinktunisia.ambulanceservice.repository;

import com.medilinktunisia.ambulanceservice.model.entity.AmbulanceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AmbulanceLocationRepository extends JpaRepository<AmbulanceLocation, Long> {

    List<AmbulanceLocation> findByAmbulanceIdOrderByTimestampDesc(Long ambulanceId);

    @Query("SELECT al FROM AmbulanceLocation al WHERE al.ambulance.id = :ambulanceId AND al.timestamp BETWEEN :start AND :end ORDER BY al.timestamp")
    List<AmbulanceLocation> findLocationHistoryBetween(Long ambulanceId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT al FROM AmbulanceLocation al WHERE al.ambulance.id = :ambulanceId ORDER BY al.timestamp DESC LIMIT 1")
    AmbulanceLocation findLatestLocationByAmbulanceId(Long ambulanceId);

    List<AmbulanceLocation> findByEmergencyIdOrderByTimestampAsc(Long emergencyId);

    void deleteByTimestampBefore(LocalDateTime timestamp);
}
