import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DoctorSectionComponent } from './doctor-section.component';
import { AuthService, PatientListDto } from '../../../core/services/auth.service';
import { AppointmentService, AppointmentDto } from '../../../core/services/appointment.service';
import { ConsultationService, ConsultationResponse } from '../../../core/services/consultation.service';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';
import { BilanService, BilanSummary, ScanBilanResponse } from '../../../core/services/bilan.service';

describe('DoctorSectionComponent', () => {
  let component: DoctorSectionComponent;
  let fixture: ComponentFixture<DoctorSectionComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let appointmentServiceSpy: jasmine.SpyObj<AppointmentService>;
  let consultationServiceSpy: jasmine.SpyObj<ConsultationService>;
  let patientServiceSpy: jasmine.SpyObj<PatientService>;
  let bilanServiceSpy: jasmine.SpyObj<BilanService>;
  let activatedRouteStub: Partial<ActivatedRoute>;

  const mockPatients: PatientListDto[] = [
    { id: 1, firstName: 'Mohamed', lastName: 'Aloui', email: 'm@test.com', phone: '20123456', address: 'Tunis', birthDate: '1985-03-15', cin: '12345678' },
    { id: 2, firstName: 'Fatma', lastName: 'Khelifi', email: 'f@test.com', phone: '20987654' }
  ];

  const mockAppointments: AppointmentDto[] = [
    { id: 1, patientId: 1, doctorId: 1, dateTime: '2026-06-18T09:30:00', status: 'PENDING', mode: 'PRESENTIEL', notes: 'Consultation', createdAt: '2026-06-10T10:00:00' },
    { id: 2, patientId: 2, doctorId: 1, dateTime: '2026-06-18T11:00:00', status: 'CONFIRMED', mode: 'TELECONSULTATION', notes: 'Suivi', createdAt: '2026-06-10T10:00:00' },
    { id: 3, patientId: 1, doctorId: 1, dateTime: '2026-07-01T14:00:00', status: 'CANCELLED', mode: 'PRESENTIEL', notes: '', createdAt: '2026-06-10T10:00:00' }
  ];

  const mockUser = { id: 1, firstName: 'Yasmine', lastName: 'Ben Salem', email: 'yasmine@test.com', phone: '20111111', specialty: 'Cardiologie', licenseNumber: '12345', facility: 'Clinique El Manar' };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getAllPatients', 'getCurrentUser']);
    appointmentServiceSpy = jasmine.createSpyObj('AppointmentService', ['getDoctorAppointments', 'confirmAppointment']);
    consultationServiceSpy = jasmine.createSpyObj('ConsultationService', ['getPatientConsultations']);
    patientServiceSpy = jasmine.createSpyObj('PatientService', ['getPatientMedicalRecord']);
    bilanServiceSpy = jasmine.createSpyObj('BilanService', ['getDoctorBilans', 'getBilanForDoctor', 'updateReviewStatus']);
    activatedRouteStub = {
      data: of({ section: 'patients', title: 'Mes Patients' })
    };

    authServiceSpy.getCurrentUser.and.returnValue(mockUser);
    bilanServiceSpy.getDoctorBilans.and.returnValue(of({ content: [] }));

    await TestBed.configureTestingModule({
      declarations: [DoctorSectionComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: AppointmentService, useValue: appointmentServiceSpy },
        { provide: ConsultationService, useValue: consultationServiceSpy },
        { provide: PatientService, useValue: patientServiceSpy },
        { provide: BilanService, useValue: bilanServiceSpy },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorSectionComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load patients when section is patients', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    expect(authServiceSpy.getAllPatients).toHaveBeenCalled();
    expect(component.section).toBe('patients');
    expect(component.registeredPatients.length).toBe(2);
    expect(component.patients.length).toBe(2);
  });

  it('should load appointments when section is appointments', () => {
    activatedRouteStub.data = of({ section: 'appointments', title: 'Agenda' });
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    expect(appointmentServiceSpy.getDoctorAppointments).toHaveBeenCalled();
    expect(component.allAppointments.length).toBe(3);
    expect(component.loadingAppointments).toBeFalse();
  });

  it('should handle appointments load error', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    expect(component.loadingAppointments).toBeFalse();
    expect(component.allAppointments.length).toBe(0);
  });

  it('should filter appointments by status', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    component.selectedDate = null;

    component.statusFilter = 'PENDING';
    expect(component.filteredAppointments.length).toBe(1);

    component.statusFilter = 'CONFIRMED';
    expect(component.filteredAppointments.length).toBe(1);

    component.statusFilter = 'CANCELLED';
    expect(component.filteredAppointments.length).toBe(1);

    component.statusFilter = 'ALL';
    expect(component.filteredAppointments.length).toBe(3);
  });

  it('should navigate calendar months forward', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    component.calendarMonth = 5;
    component.calendarYear = 2026;
    fixture.detectChanges();

    component.nextMonth();
    expect(component.calendarMonth).toBe(6);
    expect(component.calendarYear).toBe(2026);
  });

  it('should navigate calendar months backward', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    component.calendarMonth = 5;
    component.calendarYear = 2026;
    fixture.detectChanges();

    component.prevMonth();
    expect(component.calendarMonth).toBe(4);
    expect(component.calendarYear).toBe(2026);
  });

  it('should wrap year when navigating past December', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    component.calendarMonth = 11;
    component.calendarYear = 2026;
    fixture.detectChanges();

    component.nextMonth();
    expect(component.calendarMonth).toBe(0);
    expect(component.calendarYear).toBe(2027);
  });

  it('should wrap year when navigating past January', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    component.calendarMonth = 0;
    component.calendarYear = 2026;
    fixture.detectChanges();

    component.prevMonth();
    expect(component.calendarMonth).toBe(11);
    expect(component.calendarYear).toBe(2025);
  });

  it('should confirm appointment', () => {
    const updatedDto: AppointmentDto = { ...mockAppointments[0], status: 'CONFIRMED' };
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.confirmAppointment.and.returnValue(of(updatedDto));
    fixture.detectChanges();

    const targetApp = component.allAppointments[0];
    component.confirmAppointment(targetApp);

    expect(appointmentServiceSpy.confirmAppointment).toHaveBeenCalledWith(1);
    const updated = component.allAppointments.find(a => a.id === 1);
    expect(updated?.status).toBe('Confirmé');
    expect(updated?.statusClass).toBe('status-confirmed');
  });

  it('should handle confirm appointment error', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.confirmAppointment.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    const targetApp = component.allAppointments[0];
    component.confirmAppointment(targetApp);

    expect(appointmentServiceSpy.confirmAppointment).toHaveBeenCalledWith(1);
    expect(component.actionLoading[1]).toBeFalse();
  });

  it('should filter patients on load error and still load appointments', () => {
    authServiceSpy.getAllPatients.and.returnValue(throwError(() => new Error('error')));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    expect(appointmentServiceSpy.getDoctorAppointments).toHaveBeenCalled();
  });

  it('should provide a readable calendar month label', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    component.calendarMonth = 5;
    component.calendarYear = 2026;
    fixture.detectChanges();

    expect(component.calendarMonthLabel).toBe('Juin 2026');
  });

  it('should clear date filter', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();
    component.selectedDate = new Date(2026, 5, 18);

    component.clearDateFilter();
    expect(component.selectedDate).toBeNull();
  });

  // ── Fiche patient tests ────────────────────────────────────────────────

  it('should select and deselect a patient', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    consultationServiceSpy.getPatientConsultations.and.returnValue(of([]));
    patientServiceSpy.getPatientMedicalRecord.and.returnValue(of({} as MedicalRecord));
    fixture.detectChanges();

    component.selectPatient(component.patients[0]);
    expect(component.selectedPatient).toBe(component.patients[0]);
    expect(consultationServiceSpy.getPatientConsultations).toHaveBeenCalledWith(1);
    expect(patientServiceSpy.getPatientMedicalRecord).toHaveBeenCalledWith(1);

    // Re-clicking the same patient deselects
    component.selectPatient(component.patients[0]);
    expect(component.selectedPatient).toBeNull();
  });

  it('should return to patient list', () => {
    component.selectedPatient = component.patients[0];
    component.patientConsultations = [{ id: 1 } as ConsultationResponse];
    component.patientMedicalRecord = {} as MedicalRecord;

    component.backToPatientList();

    expect(component.selectedPatient).toBeNull();
    expect(component.patientConsultations).toEqual([]);
    expect(component.patientMedicalRecord).toBeNull();
  });

  it('should filter patients by search query', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    component.patientSearch = 'Aloui';
    expect(component.filteredPatients.length).toBe(1);
    expect(component.filteredPatients[0].name).toContain('Aloui');

    component.patientSearch = 'nonexistent';
    expect(component.filteredPatients.length).toBe(0);

    component.patientSearch = '';
    expect(component.filteredPatients.length).toBe(2);
  });

  it('should return patient details from registeredPatients', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    component.selectedPatient = { id: 1 };
    const details = component.patientDetails;
    expect(details?.firstName).toBe('Mohamed');
    expect(details?.address).toBe('Tunis');
    expect(details?.birthDate).toBe('1985-03-15');
    expect(details?.cin).toBe('12345678');
  });

  it('should return medical record getters', () => {
    component.patientMedicalRecord = {
      height: 175,
      weight: 70,
      insuranceCompany: 'CNAM',
      insuranceNumber: '12345'
    } as MedicalRecord;

    expect(component.medicalHeight).toBe('175 cm');
    expect(component.medicalWeight).toBe('70 kg');
    expect(component.insuranceLabel).toBe('CNAM — 12345');
  });

  it('should return fallback values when medical record is null', () => {
    component.patientMedicalRecord = null;
    expect(component.medicalHeight).toBe('—');
    expect(component.medicalWeight).toBe('—');
    expect(component.insuranceLabel).toBe('Non renseignée');
  });

  it('should return insurance label from company only', () => {
    component.patientMedicalRecord = { insuranceCompany: 'CNAM' } as MedicalRecord;
    expect(component.insuranceLabel).toBe('CNAM');
  });

  it('should return correct consultation status labels', () => {
    expect(component.getConsultationStatusLabel('PENDING')).toBe('En attente');
    expect(component.getConsultationStatusLabel('IN_PROGRESS')).toBe('En cours');
    expect(component.getConsultationStatusLabel('COMPLETED')).toBe('Terminée');
    expect(component.getConsultationStatusLabel('CANCELLED')).toBe('Annulée');
    expect(component.getConsultationStatusLabel('UNKNOWN')).toBe('UNKNOWN');
  });

  it('should return correct consultation status classes', () => {
    expect(component.getConsultationStatusClass('PENDING')).toBe('status-pending');
    expect(component.getConsultationStatusClass('IN_PROGRESS')).toBe('status-progress');
    expect(component.getConsultationStatusClass('COMPLETED')).toBe('status-completed');
    expect(component.getConsultationStatusClass('CANCELLED')).toBe('status-cancelled');
    expect(component.getConsultationStatusClass('UNKNOWN')).toBe('');
  });

  it('should handle patient consultations load error', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    consultationServiceSpy.getPatientConsultations.and.returnValue(throwError(() => new Error('error')));
    patientServiceSpy.getPatientMedicalRecord.and.returnValue(of({} as MedicalRecord));
    fixture.detectChanges();

    component.selectPatient(component.patients[0]);
    expect(component.loadingPatientConsultations).toBeFalse();
  });

  it('should handle medical record load error', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    consultationServiceSpy.getPatientConsultations.and.returnValue(of([]));
    patientServiceSpy.getPatientMedicalRecord.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    component.selectPatient(component.patients[0]);
    expect(component.medicalLoading).toBeFalse();
  });

  // ── Additional fiche patient tests ──────────────────────────────────────

  it('should get patient name from registeredPatients', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    expect(component.getPatientName(1)).toBe('Mohamed Aloui');
    expect(component.getPatientName(2)).toBe('Fatma Khelifi');
    expect(component.getPatientName(999)).toBe('Patient #999');
  });

  it('should format date in French locale', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    const result = component.formatDate('2026-06-18T09:30:00');
    expect(result).toContain('juin');
    expect(result).toContain('18');
    expect(component.formatDate('')).toBe('');
  });

  it('should format hour as HH:mm', () => {
    expect(component.formatHour('2026-06-18T09:30:00')).toBe('09:30');
    expect(component.formatHour('2026-06-18T14:05:00')).toBe('14:05');
    expect(component.formatHour('')).toBe('');
  });

  it('should detect if appointment is today', () => {
    const today = new Date();
    const todayStr = today.toISOString();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    expect(component.isAppointmentToday({ rawDateTime: todayStr } as any)).toBeTrue();
    expect(component.isAppointmentToday({ rawDateTime: tomorrow.toISOString() } as any)).toBeFalse();
  });

  it('should return insurance label from number only', () => {
    component.patientMedicalRecord = { insuranceNumber: '12345' } as MedicalRecord;
    expect(component.insuranceLabel).toBe('12345');
  });

  it('should return fallback insurance label when both empty', () => {
    component.patientMedicalRecord = {} as MedicalRecord;
    expect(component.insuranceLabel).toBe('Non renseignée');
  });

  it('should handle falsy height and weight in medical getters', () => {
    component.patientMedicalRecord = { height: undefined, weight: undefined } as MedicalRecord;
    expect(component.medicalHeight).toBe('—');
    expect(component.medicalWeight).toBe('—');
  });

  it('should filter appointments by search query', () => {
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    component.selectedDate = null;
    component.searchQuery = 'Aloui';
    expect(component.filteredAppointments.length).toBe(2);

    component.searchQuery = 'Suivi';
    expect(component.filteredAppointments.length).toBe(1);

    component.searchQuery = 'nonexistent';
    expect(component.filteredAppointments.length).toBe(0);

    component.searchQuery = '';
    expect(component.filteredAppointments.length).toBe(3);
  });
});

describe('DoctorSectionComponent - Bilan', () => {
  let component: DoctorSectionComponent;
  let fixture: ComponentFixture<DoctorSectionComponent>;
  let bilanServiceSpy: jasmine.SpyObj<BilanService>;

  const mockBilans: BilanSummary[] = [
    { id: 'uuid-1', typeBilan: 'Bilan sanguin', dateBilan: '2026-06-15', status: 'PENDING', resultCount: 3, abnormalCount: 1, reviewStatus: 'EN_ATTENTE', patientId: 1, doctorId: 2 },
    { id: 'uuid-2', typeBilan: 'Bilan urinaire', dateBilan: '2026-05-10', status: 'CONFIRMED', resultCount: 2, abnormalCount: 0, reviewStatus: 'LU', patientId: 2, doctorId: 2 }
  ];

  beforeEach(async () => {
    bilanServiceSpy = jasmine.createSpyObj('BilanService', ['getDoctorBilans', 'getBilanForDoctor', 'updateReviewStatus']);

    const authSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser', 'getAllPatients']);
    authSpy.getCurrentUser.and.returnValue({ id: 2, firstName: 'Dr', lastName: 'Test' });
    authSpy.getAllPatients.and.returnValue(of([]));

    bilanServiceSpy.getDoctorBilans.and.returnValue(of({ content: mockBilans }));

    await TestBed.configureTestingModule({
      declarations: [DoctorSectionComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: ActivatedRoute, useValue: { data: of({ section: 'labs', title: 'Resultats de laboratoire' }) } },
        { provide: AuthService, useValue: authSpy },
        { provide: AppointmentService, useValue: jasmine.createSpyObj('AppointmentService', ['getDoctorAppointments', 'confirmAppointment']) },
        { provide: ConsultationService, useValue: jasmine.createSpyObj('ConsultationService', ['getPatientConsultations']) },
        { provide: PatientService, useValue: jasmine.createSpyObj('PatientService', ['getPatientMedicalRecord']) },
        { provide: BilanService, useValue: bilanServiceSpy },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load doctor bilans on init when section is labs', () => {
    expect(component.section).toBe('labs');
    expect(component.doctorBilans.length).toBe(2);
    expect(bilanServiceSpy.getDoctorBilans).toHaveBeenCalled();
  });

  it('should set filteredDoctorBilans after loading', () => {
    expect(component.filteredDoctorBilans.length).toBe(2);
  });

  it('should select and deselect a doctor bilan', () => {
    const mockDetail = { id: 'uuid-1', typeBilan: 'Bilan sanguin', format: 'NOUVEAU_PATIENT', dateBilan: '2026-06-15', laboratoire: 'Labo', status: 'PENDING', reviewStatus: 'EN_ATTENTE', patientId: 1, doctorId: 2, resultats: [] };
    bilanServiceSpy.getBilanForDoctor.and.returnValue(of(mockDetail));

    component.selectDoctorBilan('uuid-1');

    expect(bilanServiceSpy.getBilanForDoctor).toHaveBeenCalledWith('uuid-1');
    expect(component.selectedDoctorBilan).toEqual(mockDetail);
  });

  it('should deselect doctor bilan', () => {
    component.deselectDoctorBilan();
    expect(component.selectedDoctorBilan).toBeNull();
  });

  it('should handle doctor bilans load error', () => {
    bilanServiceSpy.getDoctorBilans.and.returnValue(throwError(() => new Error('error')));
    component.loadDoctorBilans();
    expect(component.loadingDoctorBilans).toBeFalse();
  });

  it('should handle select doctor bilan error', () => {
    bilanServiceSpy.getBilanForDoctor.and.returnValue(throwError(() => new Error('error')));
    component.selectDoctorBilan('invalid');
    expect(component.loadingDoctorBilanDetail).toBeFalse();
  });

  it('should update review status', () => {
    component.doctorBilans = mockBilans;
    const updatedDetail: ScanBilanResponse = { ...mockBilans[0], reviewStatus: 'LU', format: '', laboratoire: '', resultats: [] };
    bilanServiceSpy.updateReviewStatus.and.returnValue(of(updatedDetail));

    component.selectedDoctorBilan = { id: 'uuid-1' } as any;
    component.updateReviewStatus('uuid-1', 'LU');

    expect(bilanServiceSpy.updateReviewStatus).toHaveBeenCalledWith('uuid-1', 'LU');
  });

  it('should filter bilans by search query', () => {
    component.doctorBilanSearchQuery = 'urinaire';
    component.applyDoctorBilanFilters();
    expect(component.filteredDoctorBilans.length).toBe(1);
    expect(component.filteredDoctorBilans[0].typeBilan).toBe('Bilan urinaire');
  });

  it('should sort bilans by date (recent first)', () => {
    component.doctorBilanSortOrder = 'recent';
    component.applyDoctorBilanFilters();
    expect(component.filteredDoctorBilans[0].dateBilan).toBe('2026-06-15');
  });

  it('should sort bilans by date (ancien first)', () => {
    component.doctorBilanSortOrder = 'ancien';
    component.applyDoctorBilanFilters();
    expect(component.filteredDoctorBilans[0].dateBilan).toBe('2026-05-10');
  });

  it('should clear selection when filtered list no longer contains it', () => {
    component.selectedDoctorBilan = { id: 'uuid-999' } as any;
    component.doctorBilanSearchQuery = 'nonexistent';
    component.applyDoctorBilanFilters();
    expect(component.selectedDoctorBilan).toBeNull();
  });

  it('should return correct review status label', () => {
    expect(component.getDoctorReviewStatusLabel('LU')).toBe('Consulté');
    expect(component.getDoctorReviewStatusLabel('TRAITE')).toBe('Traité');
    expect(component.getDoctorReviewStatusLabel('EN_ATTENTE')).toBe('En attente');
    expect(component.getDoctorReviewStatusLabel('UNKNOWN')).toBe('En attente');
  });

  it('should return correct review status class', () => {
    expect(component.getDoctorReviewStatusClass('LU')).toBe('review-lu');
    expect(component.getDoctorReviewStatusClass('TRAITE')).toBe('review-traite');
    expect(component.getDoctorReviewStatusClass('EN_ATTENTE')).toBe('review-en-attente');
  });

  it('should return correct bilan status label', () => {
    expect(component.getBilanStatusLabel('PENDING')).toBe('En attente');
    expect(component.getBilanStatusLabel('CONFIRMED')).toBe('Confirmé');
  });

  it('should return correct bilan status class', () => {
    expect(component.getBilanStatusClass('CONFIRMED')).toBe('status-confirmed');
    expect(component.getBilanStatusClass('PENDING')).toBe('status-pending');
  });

  it('should return correct result status label', () => {
    expect(component.getResultStatusLabel('NORMAL')).toBe('Normal');
    expect(component.getResultStatusLabel('ANORMAL')).toBe('Anormal');
    expect(component.getResultStatusLabel('CRITIQUE')).toBe('Critique');
    expect(component.getResultStatusLabel('UNKNOWN')).toBe('N/A');
  });

  it('should return correct result status class', () => {
    expect(component.getResultStatusClass('NORMAL')).toBe('result-normal');
    expect(component.getResultStatusClass('ANORMAL')).toBe('result-anormal');
    expect(component.getResultStatusClass('CRITIQUE')).toBe('result-critique');
    expect(component.getResultStatusClass('UNKNOWN')).toBe('result-na');
  });
});
