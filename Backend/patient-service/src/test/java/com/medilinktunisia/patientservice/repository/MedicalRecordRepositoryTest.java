package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.MedicalRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MedicalRecordRepositoryTest {

    @Autowired
    private MedicalRecordRepository repository;

    @BeforeEach
    void setUp() {
        MedicalRecord record = new MedicalRecord();
        record.setUserId(10L);
        record.setBloodGroup("O+");
        record.setHeight(170.0);
        record.setWeight(65.0);
        record.setAllergies("None");
        record.setChronicDiseases("None");
        record.setCurrentTreatments("None");
        record.setEmergencyContactName("Contact");
        record.setEmergencyContactPhone("+21600000000");
        record.setInsuranceCompany("Ins");
        record.setInsuranceNumber("000");
        repository.saveAndFlush(record);
    }

    @Test
    void findByUserId_whenExists_shouldReturnRecord() {
        Optional<MedicalRecord> result = repository.findByUserId(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(10L);
        assertThat(result.get().getBloodGroup()).isEqualTo("O+");
    }

    @Test
    void findByUserId_whenNotExists_shouldReturnEmpty() {
        Optional<MedicalRecord> result = repository.findByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUserId_whenExists_shouldReturnTrue() {
        boolean exists = repository.existsByUserId(10L);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserId_whenNotExists_shouldReturnFalse() {
        boolean exists = repository.existsByUserId(999L);

        assertThat(exists).isFalse();
    }
}
