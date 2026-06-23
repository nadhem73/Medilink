import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MedicalRecordsComponent } from './medical-records.component';
import { AuthService, PatientListDto } from '../../../core/services/auth.service';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';

describe('MedicalRecordsComponent', () => {
  let component: MedicalRecordsComponent;
  let fixture: ComponentFixture<MedicalRecordsComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let patientServiceSpy: jasmine.SpyObj<PatientService>;

  const mockPatients: PatientListDto[] = [
    { id: 1, firstName: 'Mohamed', lastName: 'Aloui', email: 'mohamed@test.com', phone: '20123456' },
    { id: 2, firstName: 'Fatma', lastName: 'Khelifi', email: 'fatma@test.com', phone: '20987654' },
    { id: 3, firstName: 'Ahmed', lastName: 'Ben Ali', email: 'ahmed@test.com', phone: '20555555' }
  ];

  const mockRecord: MedicalRecord = {
    userId: 1,
    bloodGroup: 'A+',
    height: 175,
    weight: 78,
    allergies: 'Pollen',
    chronicDiseases: 'Hypertension',
    currentTreatments: 'Amlodipine 5 mg'
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getAllPatients']);
    patientServiceSpy = jasmine.createSpyObj('PatientService', ['getPatientMedicalRecord']);

    await TestBed.configureTestingModule({
      declarations: [MedicalRecordsComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: PatientService, useValue: patientServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(MedicalRecordsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load patients on init', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    fixture.detectChanges();

    expect(authServiceSpy.getAllPatients).toHaveBeenCalled();
    expect(component.patients.length).toBe(3);
    expect(component.loadingList).toBeFalse();
  });

  it('should handle load patients error', () => {
    authServiceSpy.getAllPatients.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    expect(component.error).toBe('Erreur lors du chargement de la liste des patients.');
    expect(component.loadingList).toBeFalse();
  });

  it('should filter patients by search query', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    fixture.detectChanges();

    component.searchQuery = 'mohamed';
    expect(component.filteredPatients.length).toBe(1);
    expect(component.filteredPatients[0].firstName).toBe('Mohamed');

    component.searchQuery = 'test.com';
    expect(component.filteredPatients.length).toBe(3);

    component.searchQuery = '';
    expect(component.filteredPatients.length).toBe(3);
  });

  it('should select a patient and load medical record', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    patientServiceSpy.getPatientMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    component.selectPatient(mockPatients[0]);

    expect(component.selectedPatient).toEqual(mockPatients[0]);
    expect(patientServiceSpy.getPatientMedicalRecord).toHaveBeenCalledWith(1);
    expect(component.medicalRecord).toEqual(mockRecord);
    expect(component.loadingRecord).toBeFalse();
  });

  it('should not reload medical record if same patient selected', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    patientServiceSpy.getPatientMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();

    component.selectPatient(mockPatients[0]);
    patientServiceSpy.getPatientMedicalRecord.calls.reset();

    component.selectPatient(mockPatients[0]);
    expect(patientServiceSpy.getPatientMedicalRecord).not.toHaveBeenCalled();
  });

  it('should handle medical record load error', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    patientServiceSpy.getPatientMedicalRecord.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    component.selectPatient(mockPatients[0]);

    expect(component.error).toBe('Erreur lors du chargement du dossier médical.');
    expect(component.loadingRecord).toBeFalse();
  });

  it('should close detail view', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    patientServiceSpy.getPatientMedicalRecord.and.returnValue(of(mockRecord));
    fixture.detectChanges();
    component.selectPatient(mockPatients[0]);

    component.closeDetail();

    expect(component.selectedPatient).toBeNull();
    expect(component.medicalRecord).toBeNull();
  });
});
