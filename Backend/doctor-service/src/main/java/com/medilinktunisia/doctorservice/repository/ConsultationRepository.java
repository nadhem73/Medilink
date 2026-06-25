package com.medilinktunisia.doctorservice.repository;

import com.medilinktunisia.doctorservice.model.Consultation;
import com.medilinktunisia.doctorservice.model.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByDoctorIdOrderByStartTimeDesc(Long doctorId);

    List<Consultation> findByDoctorIdAndStatusOrderByStartTimeDesc(Long doctorId, ConsultationStatus status);

    List<Consultation> findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(
            Long doctorId, LocalDateTime start, LocalDateTime end);

    List<Consultation> findByPatientIdOrderByStartTimeDesc(Long patientId);

    List<Consultation> findByDoctorIdAndPatientIdOrderByStartTimeDesc(Long doctorId, Long patientId);

    List<Consultation> findByAppointmentId(Long appointmentId);
}
