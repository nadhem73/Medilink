import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MedicalRecordsComponent } from './medical-records.component';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';
import { AuthService } from '../../../core/services/auth.service';

describe('MedicalRecordsComponent', () => {
  let component: MedicalRecordsComponent;
  let fixture: ComponentFixture<MedicalRecordsComponent>;
  let patientServiceSpy: jasmine.SpyObj<PatientService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockRecord: MedicalRecord = {
    userId: 1,
    bloodGroup: 'A+',
    height: 175,
    weight: 78,
    allergies: 'Pollen',
    chronicDiseases: 'Hypertension',
    currentTreatments: 'Amlodipine 5 mg',
    emergencyContactName: 'Sami Aloui',
    emergencyContactPhone: '+21620123457',
    insuranceCompany: 'CNAM',
    insuranceNumber: '123456789'
  };

  beforeEach(async () => {
    patientServiceSpy = jasmine.createSpyObj('PatientService', ['getMyMedicalRecord']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    authServiceSpy.getCurrentUser.and.returnValue({
      firstName: 'Ahmed',
      lastName: 'Ben Ali'
    });

    await TestBed.configureTestingModule({
      declarations: [MedicalRecordsComponent],
      providers: [
        { provide: PatientService, useValue: patientServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
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
    expect(component.vitals.length).toBe(4);
    expect(component.vitals[0].value).toBe('A+');
    expect(component.vitals[1].value).toBe('175 cm');
    expect(component.vitals[2].value).toBe('78 kg');
    expect(component.vitals[3].value).toBe('25.5');
    expect(component.vitals[3].sub).toBe('Surpoids');
  });

  it('should load insurance data from medical record', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(component.assurance.length).toBe(2);
    expect(component.assurance[0].value).toBe('CNAM');
    expect(component.assurance[1].value).toBe('123456789');
  });

  it('should parse treatments from medical record', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(component.treatments.length).toBe(1);
    expect(component.treatments[0].name).toBe('Amlodipine 5 mg');
    expect(component.treatments[0].status).toBe('En cours');
  });

  it('should split treatments by comma, newline and semicolon', () => {
    const recordWithMultiple: MedicalRecord = {
      ...mockRecord,
      currentTreatments: 'Amlodipine 5 mg,Metformine 850 mg\nVitamine D;Omeprazole'
    };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(recordWithMultiple));
    fixture.detectChanges();

    expect(component.treatments.length).toBe(4);
    expect(component.treatments[0].name).toBe('Amlodipine 5 mg');
    expect(component.treatments[1].name).toBe('Metformine 850 mg');
    expect(component.treatments[2].name).toBe('Vitamine D');
    expect(component.treatments[3].name).toBe('Omeprazole');
  });

  it('should parse allergies from medical record', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(component.hasAllergies).toBeTrue();
    expect(component.allergies.length).toBe(1);
    expect(component.allergies[0]).toBe('Pollen');
  });

  it('should parse chronic diseases from medical record', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(component.hasChronicDiseases).toBeTrue();
    expect(component.chronicDiseases.length).toBe(1);
    expect(component.chronicDiseases[0]).toBe('Hypertension');
  });

  it('should set error message on failure', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBeTruthy();
  });

  it('should display patient name from auth service', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(component.patientName).toBe('Ahmed Ben Ali');
    expect(component.initials).toBe('AB');
  });

  it('should set emergency contact info', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(component.hasEmergencyContact).toBeTrue();
    expect(component.emergencyName).toBe('Sami Aloui');
    expect(component.emergencyPhone).toBe('+21620123457');
  });

  it('should detect absent emergency contact', () => {
    const noEmerg: MedicalRecord = {
      ...mockRecord,
      emergencyContactName: '',
      emergencyContactPhone: ''
    };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(noEmerg));
    fixture.detectChanges();

    expect(component.hasEmergencyContact).toBeFalse();
    expect(component.emergencyName).toBe('');
    expect(component.emergencyPhone).toBe('');
  });

  it('should detect emergency contact with only name', () => {
    const nameOnly: MedicalRecord = {
      ...mockRecord,
      emergencyContactName: 'Sami',
      emergencyContactPhone: ''
    };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(nameOnly));
    fixture.detectChanges();

    expect(component.hasEmergencyContact).toBeTrue();
  });

  it('should compute BMI correctly', () => {
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    expect(component.bmi).toBe(25.5);
    expect(component.bmiCategory).toBe('Surpoids');
  });

  it('should compute BMI category for underweight', () => {
    const thinRecord: MedicalRecord = { ...mockRecord, height: 180, weight: 50 };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(thinRecord));
    fixture.detectChanges();

    expect(component.bmi).toBe(15.4);
    expect(component.bmiCategory).toBe('Insuffisance ponderale');
  });

  it('should compute BMI category for normal', () => {
    const normalRecord: MedicalRecord = { ...mockRecord, height: 175, weight: 65 };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(normalRecord));
    fixture.detectChanges();

    expect(component.bmi).toBe(21.2);
    expect(component.bmiCategory).toBe('Corpulence normale');
  });

  it('should compute BMI category for obese', () => {
    const obeseRecord: MedicalRecord = { ...mockRecord, height: 170, weight: 100 };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(obeseRecord));
    fixture.detectChanges();

    expect(component.bmi).toBe(34.6);
    expect(component.bmiCategory).toBe('Obesite');
  });

  it('should return null BMI when height or weight missing', () => {
    const noHeight: MedicalRecord = { ...mockRecord, height: undefined, weight: 78 };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(noHeight));
    fixture.detectChanges();

    expect(component.bmi).toBeNull();
  });

  it('should return vital icon name from getVitalIcon', () => {
    expect(component.getVitalIcon('Groupe sanguin')).toBe('blood');
    expect(component.getVitalIcon('Taille')).toBe('height');
    expect(component.getVitalIcon('Poids')).toBe('weight');
    expect(component.getVitalIcon('IMC')).toBe('bmi');
    expect(component.getVitalIcon('unknown')).toBe('blood');
  });

  it('should handle empty treatments gracefully', () => {
    const noTxRecord: MedicalRecord = { ...mockRecord, currentTreatments: '' };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(noTxRecord));
    fixture.detectChanges();

    expect(component.treatments.length).toBe(0);
  });

  it('should handle empty allergies gracefully', () => {
    const noAllergiesRecord: MedicalRecord = { ...mockRecord, allergies: '' };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(noAllergiesRecord));
    fixture.detectChanges();

    expect(component.hasAllergies).toBeFalse();
    expect(component.allergies.length).toBe(0);
  });

  it('should handle default values when blood group is missing', () => {
    const noBloodRecord: MedicalRecord = { ...mockRecord, bloodGroup: undefined };
    patientServiceSpy.getMyMedicalRecord.and.returnValue(of(noBloodRecord));
    fixture.detectChanges();

    expect(component.bloodGroup).toBe('Non renseigne');
    expect(component.vitals[0].value).toBe('Non renseigne');
  });
});
