package com.medilinktunisia.doctorservice.service;

import com.medilinktunisia.doctorservice.dto.DoctorProfileDto;
import com.medilinktunisia.doctorservice.dto.DoctorProfileRequest;
import com.medilinktunisia.doctorservice.model.DoctorProfile;
import com.medilinktunisia.doctorservice.repository.DoctorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorProfileServiceTest {

    @Mock
    private DoctorProfileRepository repository;

    private DoctorProfileService service;

    @Captor
    private ArgumentCaptor<DoctorProfile> profileCaptor;

    @BeforeEach
    void setUp() {
        service = new DoctorProfileService(repository);
    }

    @Test
    void createDoctorProfile_withValidRequest_shouldSave() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(1L);
        request.setAvailable(true);
        request.setBiography("Experienced doctor");
        request.setFee(BigDecimal.valueOf(100));

        when(repository.existsByUserId(1L)).thenReturn(false);

        service.createDoctorProfile(request);

        verify(repository).save(profileCaptor.capture());
        DoctorProfile saved = profileCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getAvailable()).isTrue();
        assertThat(saved.getBiography()).isEqualTo("Experienced doctor");
        assertThat(saved.getFee()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(saved.getDebutMatin()).isEqualTo("08:00");
        assertThat(saved.getFinMatin()).isEqualTo("13:00");
        assertThat(saved.getDebutApresMidi()).isEqualTo("15:00");
        assertThat(saved.getFinApresMidi()).isEqualTo("19:00");
    }

    @Test
    void createDoctorProfile_withExistingUserId_shouldSkip() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(1L);

        when(repository.existsByUserId(1L)).thenReturn(true);

        service.createDoctorProfile(request);

        verify(repository, never()).save(any());
    }

    @Test
    void createDoctorProfile_withNullUserId_shouldSkip() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(null);

        service.createDoctorProfile(request);

        verify(repository, never()).save(any());
    }

    @Test
    void getByUserId_whenFound_shouldReturnDto() {
        DoctorProfile profile = new DoctorProfile();
        profile.setId(1L);
        profile.setUserId(1L);
        profile.setAvailable(true);
        profile.setBiography("Expert");
        profile.setFee(BigDecimal.valueOf(200));
        profile.setDebutMatin("09:00");
        profile.setFinMatin("14:00");
        profile.setDebutApresMidi("16:00");
        profile.setFinApresMidi("20:00");

        when(repository.findByUserId(1L)).thenReturn(Optional.of(profile));

        DoctorProfileDto result = service.getByUserId(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getBiography()).isEqualTo("Expert");
        assertThat(result.getFee()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(result.getDebutMatin()).isEqualTo("09:00");
        assertThat(result.getFinMatin()).isEqualTo("14:00");
        assertThat(result.getDebutApresMidi()).isEqualTo("16:00");
        assertThat(result.getFinApresMidi()).isEqualTo("20:00");
    }

    @Test
    void getByUserId_whenNotFound_shouldReturnEmptyDto() {
        when(repository.findByUserId(99L)).thenReturn(Optional.empty());

        DoctorProfileDto result = service.getByUserId(99L);

        assertThat(result.getUserId()).isEqualTo(99L);
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getFee()).isNull();
    }

    @Test
    void getAllProfiles_shouldReturnAllDtos() {
        DoctorProfile profile1 = new DoctorProfile();
        profile1.setId(1L);
        profile1.setUserId(1L);
        profile1.setAvailable(true);

        DoctorProfile profile2 = new DoctorProfile();
        profile2.setId(2L);
        profile2.setUserId(2L);
        profile2.setAvailable(false);

        when(repository.findAll()).thenReturn(List.of(profile1, profile2));

        List<DoctorProfileDto> result = service.getAllProfiles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(1).getUserId()).isEqualTo(2L);
    }

    @Test
    void updateByUserId_whenExists_shouldUpdate() {
        DoctorProfile existing = new DoctorProfile();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setAvailable(true);
        existing.setBiography("Old bio");
        existing.setFee(BigDecimal.valueOf(100));
        existing.setDebutMatin("08:00");
        existing.setFinMatin("13:00");
        existing.setDebutApresMidi("15:00");
        existing.setFinApresMidi("19:00");

        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(false);
        request.setBiography("Updated bio");
        request.setFee(BigDecimal.valueOf(150));
        request.setDebutMatin("09:00");

        when(repository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(DoctorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileDto result = service.updateByUserId(1L, request);

        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getBiography()).isEqualTo("Updated bio");
        assertThat(result.getFee()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(result.getDebutMatin()).isEqualTo("09:00");
        assertThat(result.getFinMatin()).isEqualTo("13:00");
    }

    @Test
    void updateByUserId_whenNotExists_shouldCreate() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(1L);
        request.setAvailable(true);
        request.setBiography("New profile");
        request.setFee(BigDecimal.valueOf(200));

        when(repository.findByUserId(1L)).thenReturn(Optional.empty());
        when(repository.save(any(DoctorProfile.class))).thenAnswer(invocation -> {
            DoctorProfile p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        DoctorProfileDto result = service.updateByUserId(1L, request);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getBiography()).isEqualTo("New profile");
    }

    @Test
    void updateByUserId_withNullOptionalFields_shouldKeepDefaults() {
        DoctorProfile existing = new DoctorProfile();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setAvailable(true);

        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(null);
        request.setBiography(null);
        request.setFee(null);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(DoctorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileDto result = service.updateByUserId(1L, request);

        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getBiography()).isNull();
        assertThat(result.getFee()).isNull();
    }
}
