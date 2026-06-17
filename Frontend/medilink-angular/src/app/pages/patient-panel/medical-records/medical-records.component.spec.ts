import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MedicalRecordsComponent } from './medical-records.component';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';

describe('MedicalRecordsComponent', () => {
  let component: MedicalRecordsComponent;
  let fixture: ComponentFixture<MedicalRecordsComponent>;
  let patientServiceSpy: jasmine.SpyObj<PatientService>;

  const mockRecord: MedicalRecord = {
    userId: 1,
    bloodGroup: 'A+',
    height: 175,
    weight: 78,
    allergies: 'Pollen',
    chronicDiseases: 'Hypertension',
    currentTreatments: 'Amlodipine 5 mg',
    emergencyContactName: 'Sami Aloui',
    emergencyContactPhone: '20123457'
  };

  beforeEach(async () => {
    patientServiceSpy = jasmine.createSpyObj('PatientService', ['getMyMedicalRecord']);

    await TestBed.configureTestingModule({
      declarations: [MedicalRecordsComponent],
      providers: [
        { provide: PatientService, useValue: patientServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(MedicalRecordsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load medical record on init', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(patientServiceSpy.getMyMedicalRecord).toHaveBeenCalled();
    expect(component.loading).toBeFalse();
    expect(component.vitals.length).toBe(6);
    expect(component.vitals[0].value).toBe('A+');
    expect(component.vitals[1].value).toBe('175 cm');
    expect(component.vitals[2].value).toBe('78 kg');
    expect(component.vitals[3].value).toBe('Pollen');
    expect(component.vitals[4].value).toBe('Hypertension');
    expect(component.vitals[5].value).toBe('Sami Aloui 20123457');
  });

  it('should parse treatments from medical record', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(component.treatments.length).toBe(1);
    expect(component.treatments[0].name).toBe('Amlodipine 5 mg');
  });

  it('should split treatments by comma, newline and semicolon', () => {
    const recordWithMultiple: MedicalRecord = {
      ...mockRecord,
      currentTreatments: 'Amlodipine 5 mg,Metformine 850 mg\nVitamine D;Omeprazole'
    };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(recordWithMultiple));
    fixture.detectChanges();

    expect(component.treatments.length).toBe(4);
  });

  it('should fallback to vitals with defaults on error', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    expect(component.loading).toBeFalse();
    expect(component.vitals.length).toBe(6);
    expect(component.vitals.every(v => v.value === 'Non disponible')).toBeTrue();
  });

  it('should set emergency contact for name only', () => {
    const recordWithNameOnly: MedicalRecord = {
      ...mockRecord,
      emergencyContactName: 'Sami Aloui',
      emergencyContactPhone: ''
    };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(recordWithNameOnly));
    fixture.detectChanges();

    expect(component.vitals[5].value).toBe('Sami Aloui');
  });

  it('should set emergency contact for phone only', () => {
    const recordWithPhoneOnly: MedicalRecord = {
      ...mockRecord,
      emergencyContactName: '',
      emergencyContactPhone: '20123457'
    };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(recordWithPhoneOnly));
    fixture.detectChanges();

    expect(component.vitals[5].value).toBe('20123457');
  });

  it('should set Non renseigne when both emergency fields are missing', () => {
    const recordNoEmerg: MedicalRecord = {
      ...mockRecord,
      emergencyContactName: '',
      emergencyContactPhone: ''
    };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(recordNoEmerg));
    fixture.detectChanges();

    expect(component.vitals[5].value).toBe('Non renseigne');
  });
});
