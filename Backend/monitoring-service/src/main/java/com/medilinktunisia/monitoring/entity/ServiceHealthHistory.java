package com.medilinktunisia.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_health_history", indexes = {
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_service_timestamp", columnList = "serviceName, timestamp"),
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 100)
    private String serviceName;

    @Column(length = 20)
    private String status;

    private Double cpuPercent;

    private Double ramPercent;

    private Double diskPercent;

    private Double responseTimeMs;

    private Long requestsCount;

    private Long errorsCount;

    private Double uptimeHours;

    private Integer threadCount;

    private Integer connectedUsers;

    @Column(length = 20)
    private String version;
}
