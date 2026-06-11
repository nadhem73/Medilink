package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.entity.PatientVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientVaccinationRepository extends JpaRepository<PatientVaccination, Long> {

    List<PatientVaccination> findByPatientId(Long patientId);

    @Query("SELECT v FROM PatientVaccination v WHERE v.patient.id = :patientId ORDER BY v.vaccinationDate DESC")
    List<PatientVaccination> findByPatientIdOrderByVaccinationDateDesc(Long patientId);

    @Query("SELECT v FROM PatientVaccination v WHERE v.patient.id = :patientId AND v.nextDoseDate IS NOT NULL AND v.nextDoseDate >= :today")
    List<PatientVaccination> findUpcomingVaccinations(Long patientId, LocalDate today);
}
