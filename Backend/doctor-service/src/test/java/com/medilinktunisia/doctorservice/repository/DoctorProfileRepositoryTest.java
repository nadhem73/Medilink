package com.medilinktunisia.doctorservice.repository;

import com.medilinktunisia.doctorservice.model.DoctorProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DoctorProfileRepositoryTest {

    @Autowired
    private DoctorProfileRepository repository;

    @Test
    void saveAndFindByUserId() {
        DoctorProfile profile = new DoctorProfile();
        profile.setUserId(1L);
        profile.setAvailable(true);
        profile.setBiography("Test doctor");
        profile.setFee(BigDecimal.valueOf(150));
        profile.setDebutMatin("08:00");
        profile.setFinMatin("13:00");
        profile.setDebutApresMidi("15:00");
        profile.setFinApresMidi("19:00");

        repository.save(profile);

        Optional<DoctorProfile> found = repository.findByUserId(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getAvailable()).isTrue();
        assertThat(found.get().getBiography()).isEqualTo("Test doctor");
        assertThat(found.get().getFee()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void existsByUserId_shouldReturnTrue_whenExists() {
        DoctorProfile profile = new DoctorProfile();
        profile.setUserId(2L);
        repository.save(profile);

        assertThat(repository.existsByUserId(2L)).isTrue();
    }

    @Test
    void existsByUserId_shouldReturnFalse_whenNotExists() {
        assertThat(repository.existsByUserId(999L)).isFalse();
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenNotExists() {
        Optional<DoctorProfile> found = repository.findByUserId(999L);
        assertThat(found).isEmpty();
    }
}
