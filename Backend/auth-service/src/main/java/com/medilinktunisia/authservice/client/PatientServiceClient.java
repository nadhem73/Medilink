package com.medilinktunisia.authservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Client Feign vers le patient-service.
 * Utilisé à l'inscription pour créer le dossier médical du nouveau patient
 * via l'endpoint interne (non protégé par JWT, appel service-à-service).
 */
@FeignClient(name = "patient-service")
public interface PatientServiceClient {

    @PostMapping("/api/patients/internal/medical-record")
    void createMedicalRecord(@RequestBody MedicalRecordRequest request);
}
