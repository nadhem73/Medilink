package com.medilinktunisia.prescriptionservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pickup_codes", indexes = {
        @Index(name = "idx_pickup_code", columnList = "code"),
        @Index(name = "idx_pickup_prescription", columnList = "prescription_id", unique = true)
})
@Getter
@Setter
public class PickupCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_id", nullable = false, unique = true)
    private Long prescriptionId;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean used = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
