package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.entity.PatientAppointment;
import com.medilinktunisia.patientservice.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientAppointmentRepository extends JpaRepository<PatientAppointment, Long> {

    List<PatientAppointment> findByPatientId(Long patientId);

    @Query("SELECT a FROM PatientAppointment a WHERE a.patient.id = :patientId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<PatientAppointment> findByPatientIdOrderByDateDesc(Long patientId);

    @Query("SELECT a FROM PatientAppointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :today AND a.appointmentStatus NOT IN ('CANCELLED', 'COMPLETED') ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<PatientAppointment> findUpcomingAppointments(Long patientId, LocalDate today);

    @Query("SELECT a FROM PatientAppointment a WHERE a.patient.id = :patientId AND a.appointmentDate < :today ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<PatientAppointment> findPastAppointments(Long patientId, LocalDate today);

    List<PatientAppointment> findByDoctorId(Long doctorId);

    @Query("SELECT a FROM PatientAppointment a WHERE a.doctorId = :doctorId AND a.appointmentDate = :date ORDER BY a.appointmentTime ASC")
    List<PatientAppointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    @Query("SELECT a FROM PatientAppointment a WHERE a.appointmentStatus = :status AND a.appointmentDate = :today")
    List<PatientAppointment> findByStatusAndDate(AppointmentStatus status, LocalDate today);
}
