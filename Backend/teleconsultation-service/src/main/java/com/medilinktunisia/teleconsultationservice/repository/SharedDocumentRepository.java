package com.medilinktunisia.teleconsultationservice.repository;

import com.medilinktunisia.teleconsultationservice.model.entity.SharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedDocumentRepository extends JpaRepository<SharedDocument, Long> {

    List<SharedDocument> findByTeleconsultationIdOrderByUploadedAtDesc(Long teleconsultationId);
}
