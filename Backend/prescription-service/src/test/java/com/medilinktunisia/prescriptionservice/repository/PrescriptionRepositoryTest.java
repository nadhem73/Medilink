package com.medilinktunisia.prescriptionservice.repository;

import com.medilinktunisia.prescriptionservice.model.entity.Prescription;
import com.medilinktunisia.prescriptionservice.model.entity.PrescriptionItem;
import com.medilinktunisia.prescriptionservice.model.enums.PrescriptionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PrescriptionRepositoryTest {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionItemRepository prescriptionItemRepository;

    private Prescription createPrescription(Long consultationId, Long patientId, Long doctorId, PrescriptionStatus status) {
        Prescription p = new Prescription();
        p.setConsultationId(consultationId);
        p.setPatientId(patientId);
        p.setDoctorId(doctorId);
        p.setStatus(status);

        PrescriptionItem item = new PrescriptionItem();
        item.setPrescription(p);
        item.setMedicamentId(1L);
        item.setMedicamentName("DOLIPRANE");
        item.setDosage("500mg");
        item.setPosologie("1 comprimé 3 fois par jour");
        p.setItems(List.of(item));

        return prescriptionRepository.save(p);
    }

    @Test
    void saveAndFindById() {
        Prescription saved = createPrescription(100L, 10L, 1L, PrescriptionStatus.SOUMISE);

        Optional<Prescription> found = prescriptionRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getConsultationId()).isEqualTo(100L);
        assertThat(found.get().getPatientId()).isEqualTo(10L);
        assertThat(found.get().getStatus()).isEqualTo(PrescriptionStatus.SOUMISE);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_notFound() {
        Optional<Prescription> found = prescriptionRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findByConsultationId() {
        createPrescription(100L, 10L, 1L, PrescriptionStatus.SOUMISE);

        Optional<Prescription> found = prescriptionRepository.findByConsultationId(100L);

        assertThat(found).isPresent();
        assertThat(found.get().getConsultationId()).isEqualTo(100L);
    }

    @Test
    void findByConsultationId_notFound() {
        Optional<Prescription> found = prescriptionRepository.findByConsultationId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findByPatientIdOrderByCreatedAtDesc() {
        createPrescription(100L, 10L, 1L, PrescriptionStatus.SOUMISE);
        createPrescription(101L, 10L, 1L, PrescriptionStatus.DISPENSEE);

        List<Prescription> results = prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(10L);

        assertThat(results).hasSize(2);
    }

    @Test
    void findByPatientIdOrderByCreatedAtDesc_empty() {
        List<Prescription> results = prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(99L);

        assertThat(results).isEmpty();
    }

    @Test
    void findByDoctorIdOrderByCreatedAtDesc() {
        createPrescription(100L, 10L, 1L, PrescriptionStatus.SOUMISE);
        createPrescription(101L, 11L, 2L, PrescriptionStatus.BROUILLON);

        List<Prescription> results = prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(1L);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getDoctorId()).isEqualTo(1L);
    }

    @Test
    void deletePrescription_cascadesToItems() {
        Prescription saved = createPrescription(100L, 10L, 1L, PrescriptionStatus.SOUMISE);
        Long savedId = saved.getId();

        prescriptionRepository.deleteById(savedId);

        Optional<Prescription> deleted = prescriptionRepository.findById(savedId);
        assertThat(deleted).isEmpty();

        List<PrescriptionItem> items = prescriptionItemRepository.findByPrescriptionId(savedId);
        assertThat(items).isEmpty();
    }

    @Test
    void defaultStatusIsBrouillon() {
        Prescription p = new Prescription();
        p.setConsultationId(200L);
        p.setPatientId(20L);
        p.setDoctorId(1L);

        Prescription saved = prescriptionRepository.save(p);

        assertThat(saved.getStatus()).isEqualTo(PrescriptionStatus.BROUILLON);
    }
}
