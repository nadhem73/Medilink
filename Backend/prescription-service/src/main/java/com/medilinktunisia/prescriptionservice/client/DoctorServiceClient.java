package com.medilinktunisia.prescriptionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service", path = "/api/doctors")
public interface DoctorServiceClient {

    @PatchMapping("/consultations/{consultationId}/prescription/{prescriptionId}")
    void linkPrescriptionToConsultation(
            @PathVariable("consultationId") Long consultationId,
            @PathVariable("prescriptionId") Long prescriptionId);
}
