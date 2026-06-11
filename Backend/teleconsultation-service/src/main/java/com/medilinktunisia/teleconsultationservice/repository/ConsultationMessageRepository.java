package com.medilinktunisia.teleconsultationservice.repository;

import com.medilinktunisia.teleconsultationservice.model.entity.ConsultationMessage;
import com.medilinktunisia.teleconsultationservice.model.entity.Teleconsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {

    List<ConsultationMessage> findByTeleconsultationIdOrderBySentAtAsc(Long teleconsultationId);

    List<ConsultationMessage> findByTeleconsultationOrderBySentAtAsc(Teleconsultation teleconsultation);

    List<ConsultationMessage> findByTeleconsultationOrderBySentAtDesc(Teleconsultation teleconsultation);

    @Query("SELECT m FROM ConsultationMessage m WHERE m.teleconsultation.id = :consultationId AND m.isRead = false")
    List<ConsultationMessage> findUnreadMessages(@Param("consultationId") Long consultationId);

    @Query("SELECT COUNT(m) FROM ConsultationMessage m WHERE m.teleconsultation.id = :consultationId AND m.isRead = false")
    Long countUnreadMessages(@Param("consultationId") Long consultationId);
}
