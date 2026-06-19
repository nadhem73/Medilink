package com.medilinktunisia.patientservice.repository;

import com.medilinktunisia.patientservice.model.MedicalRecord;
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

    @Test
    void saveAndFindByUserId_shouldWork() {
        MedicalRecord record = new MedicalRecord();
        record.setUserId(1L);
        record.setBloodGroup("A+");
        record.setHeight(175.0);
        record.setWeight(70.0);
        record.setAllergies("Pollen");
        record.setChronicDiseases("None");
        record.setEmergencyContactName("John");
        record.setEmergencyContactPhone("+21612345678");

        repository.save(record);

        Optional<MedicalRecord> found = repository.findByUserId(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getBloodGroup()).isEqualTo("A+");
        assertThat(found.get().getHeight()).isEqualTo(175.0);
        assertThat(found.get().getWeight()).isEqualTo(70.0);
        assertThat(found.get().getAllergies()).isEqualTo("Pollen");
    }

    @Test
    void existsByUserId_shouldReturnCorrectResult() {
        MedicalRecord record = new MedicalRecord();
        record.setUserId(10L);
        repository.save(record);

        assertThat(repository.existsByUserId(10L)).isTrue();
        assertThat(repository.existsByUserId(999L)).isFalse();
    }
}
