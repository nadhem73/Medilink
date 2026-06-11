package com.medilinktunisia.doctorservice.model.entity;

import com.medilinktunisia.doctorservice.model.enums.DoctorStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId; // Référence vers auth-service

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String mobilePhone;

    @Column(nullable = false, unique = true, length = 50)
    private String licenseNumber; // Numéro d'ordre des médecins

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(length = 255)
    private String photoUrl;

    private Integer yearsOfExperience;

    @Column(length = 255)
    private String officeAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country;

    @Column(columnDefinition = "TEXT")
    private String languagesSpoken; // JSON array: ["French", "Arabic", "English"]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DoctorStatus status;

    @Column(nullable = false)
    private Boolean isVerified;

    @Column(nullable = false)
    private Boolean acceptsNewPatients;

    private Double consultationFee;

    @Column(length = 3)
    private String currency; // TND, EUR, USD

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime verifiedAt;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoctorSpecialty> specialties = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoctorEducation> educations = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoctorExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoctorAvailability> availabilities = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoctorDocument> documents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = DoctorStatus.PENDING;
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (acceptsNewPatients == null) {
            acceptsNewPatients = true;
        }
        if (currency == null) {
            currency = "TND";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addSpecialty(DoctorSpecialty specialty) {
        specialties.add(specialty);
        specialty.setDoctor(this);
    }

    public void removeSpecialty(DoctorSpecialty specialty) {
        specialties.remove(specialty);
        specialty.setDoctor(null);
    }

    public void addEducation(DoctorEducation education) {
        educations.add(education);
        education.setDoctor(this);
    }

    public void removeEducation(DoctorEducation education) {
        educations.remove(education);
        education.setDoctor(null);
    }

    public void addExperience(DoctorExperience experience) {
        experiences.add(experience);
        experience.setDoctor(this);
    }

    public void removeExperience(DoctorExperience experience) {
        experiences.remove(experience);
        experience.setDoctor(null);
    }

    public void addAvailability(DoctorAvailability availability) {
        availabilities.add(availability);
        availability.setDoctor(this);
    }

    public void removeAvailability(DoctorAvailability availability) {
        availabilities.remove(availability);
        availability.setDoctor(null);
    }

    public void addDocument(DoctorDocument document) {
        documents.add(document);
        document.setDoctor(this);
    }

    public void removeDocument(DoctorDocument document) {
        documents.remove(document);
        document.setDoctor(null);
    }
}
