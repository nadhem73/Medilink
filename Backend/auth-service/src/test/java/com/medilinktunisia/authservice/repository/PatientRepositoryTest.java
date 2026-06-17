package com.medilinktunisia.authservice.repository;

import com.medilinktunisia.authservice.model.entity.Patient;
import com.medilinktunisia.authservice.model.enums.Gender;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PatientRepositoryTest {

    @Autowired private PatientRepository patientRepository;

    private Patient savedPatient;

    @BeforeEach
    void setUp() {
        Patient patient = new Patient();
        patient.setEmail("patient@test.com");
        patient.setPassword("encodedPassword");
        patient.setFirstName("Test");
        patient.setLastName("User");
        patient.setPhone("+21650123456");
        patient.setCin("98765432");
        patient.setBirthDate(LocalDate.of(1995, 5, 15));
        patient.setGender(Gender.FEMALE);
        patient.setAddress("Sfax");
        patient.setRole(Role.PATIENT);
        patient.setStatus(UserStatus.ACTIVE);
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());

        savedPatient = patientRepository.save(patient);
    }

    @Test
    void findByCin_whenExists_shouldReturnPatient() {
        Optional<Patient> found = patientRepository.findByCin("98765432");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedPatient.getId());
        assertThat(found.get().getEmail()).isEqualTo("patient@test.com");
        assertThat(found.get().getFirstName()).isEqualTo("Test");
        assertThat(found.get().getCin()).isEqualTo("98765432");
    }

    @Test
    void findByCin_whenNotExists_shouldReturnEmpty() {
        Optional<Patient> found = patientRepository.findByCin("00000000");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByCin_whenExists_shouldReturnTrue() {
        boolean exists = patientRepository.existsByCin("98765432");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCin_whenNotExists_shouldReturnFalse() {
        boolean exists = patientRepository.existsByCin("00000000");

        assertThat(exists).isFalse();
    }
}
