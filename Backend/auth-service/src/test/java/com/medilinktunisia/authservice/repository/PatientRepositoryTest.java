package com.medilinktunisia.authservice.repository;

import com.medilinktunisia.authservice.model.entity.Patient;
import com.medilinktunisia.authservice.model.enums.Gender;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void saveAndFindByCin() {
        Patient patient = new Patient();
        patient.setCin("12345678");
        patient.setEmail("test@example.com");
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setPassword("encoded");
        patient.setRole(Role.PATIENT);
        patient.setStatus(UserStatus.ACTIVE);
        patient.setGender(Gender.MALE);

        patientRepository.save(patient);

        Optional<Patient> found = patientRepository.findByCin("12345678");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void existsByCin_returnsTrue_whenExists() {
        Patient patient = new Patient();
        patient.setCin("87654321");
        patient.setEmail("exists@example.com");
        patient.setFirstName("Jane");
        patient.setLastName("Doe");
        patient.setPassword("encoded");
        patient.setRole(Role.PATIENT);
        patient.setStatus(UserStatus.ACTIVE);

        patientRepository.save(patient);

        assertThat(patientRepository.existsByCin("87654321")).isTrue();
    }

    @Test
    void existsByCin_returnsFalse_whenNotExists() {
        assertThat(patientRepository.existsByCin("00000000")).isFalse();
    }
}
