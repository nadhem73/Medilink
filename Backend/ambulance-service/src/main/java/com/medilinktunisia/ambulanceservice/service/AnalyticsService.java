package com.medilinktunisia.ambulanceservice.service;

import com.medilinktunisia.ambulanceservice.model.dto.AnalyticsDto;
import com.medilinktunisia.ambulanceservice.model.enums.AmbulanceStatus;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyPriority;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyStatus;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyType;
import com.medilinktunisia.ambulanceservice.repository.AmbulanceRepository;
import com.medilinktunisia.ambulanceservice.repository.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AmbulanceRepository ambulanceRepository;
    private final EmergencyRepository emergencyRepository;

    public AnalyticsDto getDashboardStatistics() {
        AnalyticsDto stats = new AnalyticsDto();

        // Statistiques ambulances
        stats.setTotalAmbulances(ambulanceRepository.count());
        stats.setAvailableAmbulances(
            (long) ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE).size()
        );
        stats.setOnMissionAmbulances(
            (long) ambulanceRepository.findByStatus(AmbulanceStatus.ON_MISSION).size()
        );

        // Statistiques urgences du jour
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        List<com.medilinktunisia.ambulanceservice.model.entity.Emergency> todayEmergencies = 
            emergencyRepository.findByRequestedAtBetween(startOfDay, endOfDay);
        
        stats.setTotalEmergenciesToday((long) todayEmergencies.size());
        stats.setActiveEmergencies(
            (long) emergencyRepository.findActiveEmergencies().size()
        );
        stats.setCompletedEmergenciesToday(
            todayEmergencies.stream()
                .filter(e -> e.getStatus() == EmergencyStatus.COMPLETED)
                .count()
        );

        // Temps de réponse moyen
        Double avgResponse = emergencyRepository.getAverageResponseTime(startOfDay, endOfDay);
        stats.setAverageResponseTimeMinutes(avgResponse != null ? avgResponse : 0.0);

        // Urgences par priorité
        stats.setCriticalEmergencies(
            emergencyRepository.findActiveEmergencies().stream()
                .filter(e -> e.getPriority() == EmergencyPriority.CRITICAL)
                .count()
        );
        stats.setHighPriorityEmergencies(
            emergencyRepository.findActiveEmergencies().stream()
                .filter(e -> e.getPriority() == EmergencyPriority.HIGH)
                .count()
        );

        return stats;
    }

    public Double getAverageResponseTime(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        Double avg = emergencyRepository.getAverageResponseTime(start, end);
        return avg != null ? avg : 0.0;
    }

    public Map<EmergencyType, Long> getEmergenciesByType(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        List<com.medilinktunisia.ambulanceservice.model.entity.Emergency> emergencies = 
            emergencyRepository.findByRequestedAtBetween(start, end);
        
        Map<EmergencyType, Long> byType = new HashMap<>();
        for (EmergencyType type : EmergencyType.values()) {
            long count = emergencies.stream()
                .filter(e -> e.getEmergencyType() == type)
                .count();
            byType.put(type, count);
        }
        
        return byType;
    }

    public Map<String, Object> getAmbulanceUtilization() {
        Map<String, Object> utilization = new HashMap<>();
        
        long total = ambulanceRepository.count();
        long available = ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE).size();
        long onMission = ambulanceRepository.findByStatus(AmbulanceStatus.ON_MISSION).size();
        long unavailable = ambulanceRepository.findByStatus(AmbulanceStatus.UNAVAILABLE).size();
        
        utilization.put("total", total);
        utilization.put("available", available);
        utilization.put("onMission", onMission);
        utilization.put("unavailable", unavailable);
        utilization.put("utilizationRate", total > 0 ? (onMission * 100.0 / total) : 0.0);
        
        return utilization;
    }
}
