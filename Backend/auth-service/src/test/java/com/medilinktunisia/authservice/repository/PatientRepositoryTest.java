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

    private Patient createPatient(String cin) {
        Patient patient = new Patient();
        patient.setEmail("patient_" + cin + "@test.com");
        patient.setPassword("encodedPassword");
        patient.setFirstName("Test");
        patient.setLastName("User");
        patient.setPhone("+21650123456");
        patient.setCin(cin);
        patient.setRole(Role.PATIENT);
        patient.setStatus(UserStatus.ACTIVE);
        patient.setGender(Gender.MALE);
        return patientRepository.save(patient);
    }

    @Test
    void findByCin_whenExists_shouldReturnPatient() {
        Patient saved = createPatient("98765432");

        Optional<Patient> found = patientRepository.findByCin("98765432");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail()).isEqualTo(saved.getEmail());
        assertThat(found.get().getCin()).isEqualTo("98765432");
    }

    @Test
    void findByCin_whenNotExists_shouldReturnEmpty() {
        Optional<Patient> found = patientRepository.findByCin("00000000");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByCin_whenExists_shouldReturnTrue() {
        createPatient("98765432");

        assertThat(patientRepository.existsByCin("98765432")).isTrue();
    }

    @Test
    void existsByCin_whenNotExists_shouldReturnFalse() {
        assertThat(patientRepository.existsByCin("00000000")).isFalse();
    }
}
