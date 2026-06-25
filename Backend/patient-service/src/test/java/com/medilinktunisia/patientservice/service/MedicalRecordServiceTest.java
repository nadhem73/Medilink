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
    void createMedicalRecord_whenNullUserId_shouldDoNothing() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(null);

        service.createMedicalRecord(request);

        verify(repository, never()).existsByUserId(any());
        verify(repository, never()).save(any());
    }

    @Test
    void createMedicalRecord_whenExistingRecord_shouldDoNothing() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(1L);

        when(repository.existsByUserId(1L)).thenReturn(true);

        service.createMedicalRecord(request);

        verify(repository, never()).save(any());
    }

    @Test
    void createMedicalRecord_whenNewRecord_shouldSaveSuccessfully() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(1L);
        request.setBloodGroup("A+");
        request.setHeight(175.0);
        request.setWeight(70.5);
        request.setAllergies("Pollen");
        request.setChronicDiseases("Asthma");
        request.setCurrentTreatments("Inhaler");
        request.setEmergencyContactName("Jane Doe");
        request.setEmergencyContactPhone("+21698765432");
        request.setInsuranceCompany("Assurance Tunis");
        request.setInsuranceNumber("AT-123456");

        when(repository.existsByUserId(1L)).thenReturn(false);
        when(repository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createMedicalRecord(request);

        ArgumentCaptor<MedicalRecord> captor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(repository).save(captor.capture());

        MedicalRecord saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getBloodGroup()).isEqualTo("A+");
        assertThat(saved.getHeight()).isEqualTo(175.0);
        assertThat(saved.getWeight()).isEqualTo(70.5);
        assertThat(saved.getAllergies()).isEqualTo("Pollen");
        assertThat(saved.getChronicDiseases()).isEqualTo("Asthma");
        assertThat(saved.getCurrentTreatments()).isEqualTo("Inhaler");
        assertThat(saved.getEmergencyContactName()).isEqualTo("Jane Doe");
        assertThat(saved.getEmergencyContactPhone()).isEqualTo("+21698765432");
        assertThat(saved.getInsuranceCompany()).isEqualTo("Assurance Tunis");
        assertThat(saved.getInsuranceNumber()).isEqualTo("AT-123456");
    }

    @Test
    void getByUserId_whenFound_shouldReturnMappedDto() {
        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setBloodGroup("B+");
        record.setHeight(180.0);
        record.setWeight(80.0);
        record.setAllergies("None");
        record.setChronicDiseases("None");
        record.setCurrentTreatments("None");
        record.setEmergencyContactName("John Doe");
        record.setEmergencyContactPhone("+21612345678");
        record.setInsuranceCompany("Company");
        record.setInsuranceNumber("12345");

        when(repository.findByUserId(1L)).thenReturn(Optional.of(record));

        MedicalRecordDto result = service.getByUserId(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBloodGroup()).isEqualTo("B+");
        assertThat(result.getHeight()).isEqualTo(180.0);
        assertThat(result.getWeight()).isEqualTo(80.0);
        assertThat(result.getAllergies()).isEqualTo("None");
        assertThat(result.getChronicDiseases()).isEqualTo("None");
        assertThat(result.getCurrentTreatments()).isEqualTo("None");
        assertThat(result.getEmergencyContactName()).isEqualTo("John Doe");
        assertThat(result.getEmergencyContactPhone()).isEqualTo("+21612345678");
        assertThat(result.getInsuranceCompany()).isEqualTo("Company");
        assertThat(result.getInsuranceNumber()).isEqualTo("12345");
    }

    @Test
    void getByUserId_whenNotFound_shouldReturnEmptyDtoWithUserId() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        MedicalRecordDto result = service.getByUserId(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBloodGroup()).isNull();
        assertThat(result.getAllergies()).isNull();
    }

    // ── updateMedicalRecord ──────────────────────────────────────────────────

    @Test
    void updateMedicalRecord_whenExistingRecord_shouldUpdateAllFields() {
        MedicalRecord existing = new MedicalRecord();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setBloodGroup("A+");
        existing.setHeight(175.0);
        existing.setWeight(70.0);
        existing.setAllergies("None");
        existing.setChronicDiseases("None");
        existing.setCurrentTreatments("None");
        existing.setEmergencyContactName("Old Contact");
        existing.setEmergencyContactPhone("+21611111111");
        existing.setInsuranceCompany("Old Ins");
        existing.setInsuranceNumber("OLD");

        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(1L);
        request.setBloodGroup("B+");
        request.setHeight(180.0);
        request.setWeight(80.0);
        request.setAllergies("Pollen");
        request.setChronicDiseases("Asthma");
        request.setCurrentTreatments("Inhaler");
        request.setEmergencyContactName("New Contact");
        request.setEmergencyContactPhone("+21622222222");
        request.setInsuranceCompany("New Ins");
        request.setInsuranceNumber("NEW");

        when(repository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MedicalRecordDto result = service.updateMedicalRecord(1L, request);

        assertThat(result.getBloodGroup()).isEqualTo("B+");
        assertThat(result.getHeight()).isEqualTo(180.0);
        assertThat(result.getWeight()).isEqualTo(80.0);
        assertThat(result.getAllergies()).isEqualTo("Pollen");
        assertThat(result.getChronicDiseases()).isEqualTo("Asthma");
        assertThat(result.getCurrentTreatments()).isEqualTo("Inhaler");
        assertThat(result.getEmergencyContactName()).isEqualTo("New Contact");
        assertThat(result.getEmergencyContactPhone()).isEqualTo("+21622222222");
        assertThat(result.getInsuranceCompany()).isEqualTo("New Ins");
        assertThat(result.getInsuranceNumber()).isEqualTo("NEW");

        verify(repository).save(any(MedicalRecord.class));
    }

    @Test
    void updateMedicalRecord_whenPartialUpdate_shouldOnlyUpdateNonNullFields() {
        MedicalRecord existing = new MedicalRecord();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setBloodGroup("A+");
        existing.setHeight(175.0);
        existing.setWeight(70.0);
        existing.setAllergies("None");

        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(1L);
        request.setHeight(180.0);
        request.setInsuranceCompany("CNAM");

        when(repository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MedicalRecordDto result = service.updateMedicalRecord(1L, request);

        assertThat(result.getBloodGroup()).isEqualTo("A+");
        assertThat(result.getHeight()).isEqualTo(180.0);
        assertThat(result.getWeight()).isEqualTo(70.0);
        assertThat(result.getAllergies()).isEqualTo("None");
        assertThat(result.getInsuranceCompany()).isEqualTo("CNAM");
        assertThat(result.getChronicDiseases()).isNull();

        verify(repository).save(any(MedicalRecord.class));
    }

    @Test
    void updateMedicalRecord_whenNotFound_shouldCreateNewRecord() {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setUserId(1L);
        request.setBloodGroup("O+");
        request.setHeight(170.0);
        request.setWeight(65.0);

        when(repository.findByUserId(1L)).thenReturn(Optional.empty());
        when(repository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MedicalRecordDto result = service.updateMedicalRecord(1L, request);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBloodGroup()).isEqualTo("O+");
        assertThat(result.getHeight()).isEqualTo(170.0);
        assertThat(result.getWeight()).isEqualTo(65.0);

        verify(repository).findByUserId(1L);
        verify(repository).save(any(MedicalRecord.class));
    }
}
