package com.medilinktunisia.teleconsultationservice.repository;

import com.medilinktunisia.teleconsultationservice.model.entity.ConsultationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationParticipantRepository extends JpaRepository<ConsultationParticipant, Long> {
    List<ConsultationParticipant> findByUserId(Long userId);
}
