package com.medilinktunisia.patientservice.service;

import com.medilinktunisia.patientservice.dto.MedicalRecordDto;
import com.medilinktunisia.patientservice.dto.MedicalRecordRequest;
import com.medilinktunisia.patientservice.model.MedicalRecord;
import com.medilinktunisia.patientservice.repository.MedicalRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository repository;

    @InjectMocks
    private MedicalRecordService service;

    @Test
    void createMedicalRecord_withNewUser_shouldSave() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(1L);
        request.setBloodGroup("A+");
        request.setHeight(175.0);
        request.setWeight(70.0);
        request.setAllergies("Pollen");
        request.setChronicDiseases("None");
        request.setCurrentTreatments("None");
        request.setEmergencyContactName("John Doe");
        request.setEmergencyContactPhone("+21612345678");
        request.setInsuranceCompany("XYZ");
        request.setInsuranceNumber("12345");

        when(repository.existsByUserId(1L)).thenReturn(false);
        when(repository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createMedicalRecord(request);

        ArgumentCaptor<MedicalRecord> captor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(repository).save(captor.capture());

        MedicalRecord saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getBloodGroup()).isEqualTo("A+");
        assertThat(saved.getHeight()).isEqualTo(175.0);
        assertThat(saved.getAllergies()).isEqualTo("Pollen");
    }

    @Test
    void createMedicalRecord_withNullUserId_shouldDoNothing() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(null);

        service.createMedicalRecord(request);

        verify(repository, never()).save(any());
        verify(repository, never()).existsByUserId(any());
    }

    @Test
    void createMedicalRecord_withExistingUserId_shouldDoNothing() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(1L);

        when(repository.existsByUserId(1L)).thenReturn(true);

        service.createMedicalRecord(request);

        verify(repository, never()).save(any());
    }

    @Test
    void getByUserId_whenFound_shouldReturnDto() {
        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setBloodGroup("B-");
        record.setHeight(180.0);
        record.setWeight(80.0);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(record));

        MedicalRecordDto result = service.getByUserId(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBloodGroup()).isEqualTo("B-");
        assertThat(result.getHeight()).isEqualTo(180.0);
        assertThat(result.getWeight()).isEqualTo(80.0);
    }

    @Test
    void getByUserId_whenNotFound_shouldReturnEmptyDto() {
        when(repository.findByUserId(99L)).thenReturn(Optional.empty());

        MedicalRecordDto result = service.getByUserId(99L);

        assertThat(result.getUserId()).isEqualTo(99L);
        assertThat(result.getBloodGroup()).isNull();
        assertThat(result.getHeight()).isNull();
        assertThat(result.getWeight()).isNull();
    }
}
