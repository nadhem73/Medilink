import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';
import { ConsultationsComponent } from './consultations.component';
import { ConsultationService, ConsultationResponse } from '../../../core/services/consultation.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';

describe('ConsultationsComponent', () => {
  let component: ConsultationsComponent;
  let fixture: ComponentFixture<ConsultationsComponent>;
  let mockConsultationService: jasmine.SpyObj<ConsultationService>;
  let mockAppointmentService: jasmine.SpyObj<AppointmentService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockPatientService: jasmine.SpyObj<PatientService>;

  const mockConsultations: ConsultationResponse[] = [
    {
      id: 1,
      patientId: 10,
      doctorId: 1,
      startTime: '2026-06-17T10:00:00',
      status: 'PENDING',
      type: 'PRESENTIEL',
      reason: 'Headache',
      createdAt: '2026-06-17T09:00:00'
    },
    {
      id: 2,
      patientId: 11,
      doctorId: 1,
      startTime: '2026-06-17T11:00:00',
      status: 'IN_PROGRESS',
      type: 'TELECONSULTATION',
      diagnosis: 'Migraine',
      createdAt: '2026-06-17T09:00:00'
    }
  ];

  beforeEach(async () => {
    mockConsultationService = jasmine.createSpyObj('ConsultationService', [
      'getAllConsultations', 'getTodayConsultations', 'getConsultation',
      'startConsultation', 'updateConsultation', 'completeConsultation', 'cancelConsultation'
    ]);
    mockConsultationService.getAllConsultations.and.returnValue(of(mockConsultations));

    mockAppointmentService = jasmine.createSpyObj('AppointmentService', ['getDoctorAppointments']);
    mockAppointmentService.getDoctorAppointments.and.returnValue(of([]));

    mockAuthService = jasmine.createSpyObj('AuthService', ['getAllPatients']);
    mockAuthService.getAllPatients.and.returnValue(of([]));

    mockPatientService = jasmine.createSpyObj('PatientService', ['getPatientMedicalRecord', 'updatePatientMedicalRecord']);
    mockPatientService.getPatientMedicalRecord.and.returnValue(of(null as any));
    mockPatientService.updatePatientMedicalRecord.and.returnValue(of({} as any));

    await TestBed.configureTestingModule({
      declarations: [ConsultationsComponent],
      providers: [
        { provide: ConsultationService, useValue: mockConsultationService },
        { provide: AppointmentService, useValue: mockAppointmentService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: PatientService, useValue: mockPatientService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load consultations on init', () => {
    expect(mockConsultationService.getAllConsultations).toHaveBeenCalledWith(undefined);
    expect(component.consultations.length).toBe(2);
  });

  it('should filter consultations by status', () => {
    mockConsultationService.getAllConsultations.and.returnValue(of([mockConsultations[1]]));
    component.filterByStatus('IN_PROGRESS');
    expect(mockConsultationService.getAllConsultations).toHaveBeenCalledWith('IN_PROGRESS');
  });

  it('should select a consultation and init editing form', () => {
    component.selectConsultation(mockConsultations[0]);
    expect(component.selectedConsultation).toEqual(mockConsultations[0]);
    expect(component.editingConsultation.patientId).toBe(10);
  });

  it('should go back to list', () => {
    component.selectedConsultation = mockConsultations[0];
    component.backToList();
    expect(component.selectedConsultation).toBeNull();
  });

  it('should save medical record', () => {
    const mockRecord = { userId: 10, height: 175, weight: 70 };
    mockPatientService.updatePatientMedicalRecord.and.returnValue(of(mockRecord as any));
    component.selectedConsultation = mockConsultations[0];
    component.editingMedicalRecord = { height: 175, weight: 70 };
    component.saveMedicalRecord();
    expect(mockPatientService.updatePatientMedicalRecord).toHaveBeenCalledWith(10, jasmine.objectContaining({ height: 175, weight: 70 }));
  });

  it('should complete consultation', () => {
    const completed = { ...mockConsultations[1], status: 'COMPLETED' };
    mockConsultationService.completeConsultation.and.returnValue(of(completed));
    component.selectedConsultation = mockConsultations[1];
    component.completeConsultation();
    expect(mockConsultationService.completeConsultation).toHaveBeenCalledWith(2, {});
  });

  it('should cancel consultation', () => {
    mockConsultationService.cancelConsultation.and.returnValue(of(void 0));
    component.cancelConsultation(mockConsultations[0]);
    expect(mockConsultationService.cancelConsultation).toHaveBeenCalledWith(1);
  });

  it('should return correct status class', () => {
    expect(component.getStatusClass('PENDING')).toBe('pending');
    expect(component.getStatusClass('IN_PROGRESS')).toBe('progress');
    expect(component.getStatusClass('COMPLETED')).toBe('completed');
    expect(component.getStatusClass('CANCELLED')).toBe('cancelled');
    expect(component.getStatusClass('UNKNOWN')).toBe('');
  });
});
