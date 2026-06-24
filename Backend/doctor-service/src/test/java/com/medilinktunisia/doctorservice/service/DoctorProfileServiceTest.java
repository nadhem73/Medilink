package com.medilinktunisia.doctorservice.service;

import com.medilinktunisia.doctorservice.dto.DoctorProfileDto;
import com.medilinktunisia.doctorservice.dto.DoctorProfileRequest;
import com.medilinktunisia.doctorservice.model.DoctorProfile;
import com.medilinktunisia.doctorservice.repository.DoctorProfileRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private DoctorProfileService service;

    @Captor
    private ArgumentCaptor<DoctorProfile> profileCaptor;

    private final Long userId = 1L;

    @Test
    void createDoctorProfile_newProfile_savesWithDefaults() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(userId);
        request.setBiography("Test bio");
        request.setFee(new BigDecimal("50.00"));

        when(repository.existsByUserId(userId)).thenReturn(false);

        service.createDoctorProfile(request);

        verify(repository).save(profileCaptor.capture());
        DoctorProfile saved = profileCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getAvailable()).isTrue();
        assertThat(saved.getBiography()).isEqualTo("Test bio");
        assertThat(saved.getFee()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(saved.getDebutMatin()).isEqualTo("08:00");
        assertThat(saved.getFinMatin()).isEqualTo("13:00");
        assertThat(saved.getDebutApresMidi()).isEqualTo("15:00");
        assertThat(saved.getFinApresMidi()).isEqualTo("19:00");
    }

    @Test
    void createDoctorProfile_existingUserId_doesNothing() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(userId);

        when(repository.existsByUserId(userId)).thenReturn(true);

        service.createDoctorProfile(request);

        verify(repository, never()).save(any());
    }

    @Test
    void createDoctorProfile_nullUserId_doesNothing() {
        DoctorProfileRequest request = new DoctorProfileRequest();

        service.createDoctorProfile(request);

        verify(repository, never()).save(any());
    }

    @Test
    void getByUserId_found_returnsDto() {
        DoctorProfile profile = new DoctorProfile();
        profile.setUserId(userId);
        profile.setAvailable(true);
        profile.setBiography("Bio");
        profile.setFee(new BigDecimal("50.00"));

        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));

        DoctorProfileDto result = service.getByUserId(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getBiography()).isEqualTo("Bio");
        assertThat(result.getFee()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void getByUserId_notFound_returnsEmptyDtoWithDefaultAvailable() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        DoctorProfileDto result = service.getByUserId(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getBiography()).isNull();
        assertThat(result.getFee()).isNull();
    }

    @Test
    void getAllProfiles_returnsAllProfiles() {
        DoctorProfile p1 = new DoctorProfile();
        p1.setUserId(1L);
        p1.setAvailable(true);

        DoctorProfile p2 = new DoctorProfile();
        p2.setUserId(2L);
        p2.setAvailable(false);

        when(repository.findAll()).thenReturn(List.of(p1, p2));

        List<DoctorProfileDto> result = service.getAllProfiles();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DoctorProfileDto::getUserId).containsExactly(1L, 2L);
    }

    @Test
    void getAllProfiles_emptyList_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<DoctorProfileDto> result = service.getAllProfiles();

        assertThat(result).isEmpty();
    }

    @Test
    void updateByUserId_existingProfile_updatesFields() {
        DoctorProfile existing = new DoctorProfile();
        existing.setUserId(userId);
        existing.setAvailable(true);

        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(false);
        request.setBiography("Updated bio");
        request.setFee(new BigDecimal("75.00"));

        when(repository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileDto result = service.updateByUserId(userId, request);

        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getBiography()).isEqualTo("Updated bio");
        assertThat(result.getFee()).isEqualByComparingTo(new BigDecimal("75.00"));
        verify(repository).save(existing);
    }

    @Test
    void updateByUserId_nonExisting_createsThenUpdates() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(false);
        request.setBiography("New bio");

        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileDto result = service.updateByUserId(userId, request);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getBiography()).isEqualTo("New bio");
        verify(repository).save(any(DoctorProfile.class));
    }

    @Test
    void createDoctorProfile_customTimes_savesWithCustomTimes() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setUserId(userId);
        request.setAvailable(false);
        request.setDebutMatin("09:00");
        request.setFinMatin("12:00");
        request.setDebutApresMidi("14:00");
        request.setFinApresMidi("18:00");

        when(repository.existsByUserId(userId)).thenReturn(false);

        service.createDoctorProfile(request);

        verify(repository).save(profileCaptor.capture());
        DoctorProfile saved = profileCaptor.getValue();
        assertThat(saved.getAvailable()).isFalse();
        assertThat(saved.getDebutMatin()).isEqualTo("09:00");
        assertThat(saved.getFinMatin()).isEqualTo("12:00");
        assertThat(saved.getDebutApresMidi()).isEqualTo("14:00");
        assertThat(saved.getFinApresMidi()).isEqualTo("18:00");
    }

    @Test
    void updateByUserId_nullAvailable_keepsExistingValue() {
        DoctorProfile existing = new DoctorProfile();
        existing.setUserId(userId);
        existing.setAvailable(true);
        existing.setBiography("Original bio");
        existing.setFee(new BigDecimal("50.00"));

        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setBiography("Updated bio");

        when(repository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileDto result = service.updateByUserId(userId, request);

        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getBiography()).isEqualTo("Updated bio");
    }

    @Test
    void updateByUserId_nullTimeFields_keepsExistingTimes() {
        DoctorProfile existing = new DoctorProfile();
        existing.setUserId(userId);
        existing.setAvailable(true);
        existing.setDebutMatin("08:00");
        existing.setFinMatin("13:00");
        existing.setDebutApresMidi("15:00");
        existing.setFinApresMidi("19:00");

        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(false);

        when(repository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileDto result = service.updateByUserId(userId, request);

        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getDebutMatin()).isEqualTo("08:00");
        assertThat(result.getFinMatin()).isEqualTo("13:00");
        assertThat(result.getDebutApresMidi()).isEqualTo("15:00");
        assertThat(result.getFinApresMidi()).isEqualTo("19:00");
    }

    @Test
    void updateByUserId_nullBiographyAndFee_setsThemToNull() {
        DoctorProfile existing = new DoctorProfile();
        existing.setUserId(userId);
        existing.setAvailable(true);
        existing.setBiography("Old bio");
        existing.setFee(new BigDecimal("50.00"));

        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setAvailable(false);

        when(repository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileDto result = service.updateByUserId(userId, request);

        assertThat(result.getBiography()).isNull();
        assertThat(result.getFee()).isNull();
    }
}
