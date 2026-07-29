import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';
import { ConsultationsComponent } from './consultations.component';
import { ConsultationService, ConsultationResponse, ConsultationRequest } from '../../../core/services/consultation.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { PrescriptionService, PrescriptionEmailRequest } from '../../../core/services/prescription.service';

describe('ConsultationsComponent', () => {
  let component: ConsultationsComponent;
  let fixture: ComponentFixture<ConsultationsComponent>;
  let mockConsultationService: jasmine.SpyObj<ConsultationService>;
  let mockAppointmentService: jasmine.SpyObj<AppointmentService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockPatientService: jasmine.SpyObj<PatientService>;
  let mockDoctorService: jasmine.SpyObj<DoctorService>;
  let mockPrescriptionService: jasmine.SpyObj<PrescriptionService>;

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

  const mockPatients = [
    { id: 10, firstName: 'John', lastName: 'Doe', email: 'john@test.com', gender: 'Homme', phone: '', role: 'PATIENT' },
    { id: 11, firstName: 'Jane', lastName: 'Smith', email: '', gender: 'Femme', phone: '', role: 'PATIENT' }
  ];

  const mockDoctorProfile = {
    id: 1, userId: 1, specialty: 'Cardiologie', licenseNumber: 'TUN-12345',
    phone: '+216 99 999 999', email: 'doctor@test.com', hospital: 'Hôpital Charles Nicolle',
    debutMatin: '08:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00',
    available: true, fee: 50
  };

  const mockCurrentUser = {
    id: 1, firstName: 'Ahmed', lastName: 'BenAli', email: 'doctor@test.com',
    phone: '+216 99 999 999', role: 'DOCTOR'
  };

  beforeEach(async () => {
    mockConsultationService = jasmine.createSpyObj('ConsultationService', [
      'getAllConsultations', 'getTodayConsultations', 'getConsultation',
      'startConsultation', 'updateConsultation', 'completeConsultation', 'cancelConsultation'
    ]);
    mockConsultationService.getAllConsultations.and.returnValue(of(mockConsultations));

    mockAppointmentService = jasmine.createSpyObj('AppointmentService', [
      'getDoctorAppointments', 'getAvailableSlots', 'createFollowUpAppointment', 'markAppointmentAsCompleted'
    ]);
    mockAppointmentService.getDoctorAppointments.and.returnValue(of([]));
    mockAppointmentService.getAvailableSlots.and.returnValue(of([]));

    mockAuthService = jasmine.createSpyObj('AuthService', ['getCurrentUser', 'getAllPatients']);
    mockAuthService.getCurrentUser.and.returnValue(mockCurrentUser as any);
    mockAuthService.getAllPatients.and.returnValue(of(mockPatients as any));

    mockPatientService = jasmine.createSpyObj('PatientService', ['getPatientMedicalRecord', 'updatePatientMedicalRecord']);
    mockPatientService.getPatientMedicalRecord.and.returnValue(of(null as any));
    mockPatientService.updatePatientMedicalRecord.and.returnValue(of({} as any));

    mockDoctorService = jasmine.createSpyObj('DoctorService', ['getDoctorProfileById']);
    mockDoctorService.getDoctorProfileById.and.returnValue(of(mockDoctorProfile as any));

    mockPrescriptionService = jasmine.createSpyObj('PrescriptionService', [
      'createPrescription', 'getPrescription', 'getPrescriptionByConsultation',
      'getPrescriptionsByPatient', 'cancelPrescription', 'sendPrescriptionEmail', 'searchMedicaments', 'checkStock'
    ]);
    mockPrescriptionService.sendPrescriptionEmail.and.returnValue(of({}));
    mockPrescriptionService.getPrescription.and.returnValue(of({ id: 1, items: [] } as any));

    await TestBed.configureTestingModule({
      declarations: [ConsultationsComponent],
      providers: [
        { provide: ConsultationService, useValue: mockConsultationService },
        { provide: AppointmentService, useValue: mockAppointmentService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: PatientService, useValue: mockPatientService },
        { provide: DoctorService, useValue: mockDoctorService },
        { provide: PrescriptionService, useValue: mockPrescriptionService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── Base Tests ──

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load consultations on init', () => {
    expect(mockConsultationService.getAllConsultations).toHaveBeenCalledWith(undefined);
    expect(component.consultations.length).toBe(2);
  });

  it('should load doctor profile on init', () => {
    expect(mockDoctorService.getDoctorProfileById).toHaveBeenCalledWith(1);
  });

  it('should load patients on init', () => {
    expect(mockAuthService.getAllPatients).toHaveBeenCalled();
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

  // ── Patient Info Tests ──

  it('should get patient name from patientMap', () => {
    expect(component.getPatientName(10)).toBe('John Doe');
    expect(component.getPatientName(99)).toBe('Patient #99');
  });

  it('should get patient email from patientMap', () => {
    expect(component.getPatientEmail(10)).toBe('john@test.com');
    expect(component.getPatientEmail(99)).toBe('');
  });

  it('should get patient initials', () => {
    expect(component.getPatientInitials(10)).toBe('JD');
    expect(component.getPatientInitials(99)).toBe('#99');
  });

  it('should get patient gender', () => {
    expect(component.getPatientGender(10)).toBe('Homme');
    expect(component.getPatientGender(99)).toBe('');
  });

  // ── Prescription Modal Tests ──

  it('should open prescription modal', () => {
    component.openPrescriptionModal();
    expect(component.showPrescriptionModal).toBeTrue();
    expect(component.prescriptionError).toBe('');
  });

  it('should close prescription modal', () => {
    component.showPrescriptionModal = true;
    component.closePrescriptionModal();
    expect(component.showPrescriptionModal).toBeFalse();
  });

  it('should onPrescriptionSaved fetch updated consultation and load items', () => {
    const updatedConsultation = { ...mockConsultations[0], prescriptionId: 5, requestedExams: 'Hématologie' };
    mockConsultationService.getConsultation.and.returnValue(of(updatedConsultation));

    const prescriptionResponse = { id: 5, items: [{ medicamentId: 1, medicamentName: 'DOLIPRANE' }] };
    mockPrescriptionService.getPrescription.and.returnValue(of(prescriptionResponse as any));

    component.selectedConsultation = mockConsultations[0];
    component.editingConsultation.requestedExams = 'Hématologie';
    component.showPrescriptionModal = true;

    component.onPrescriptionSaved();

    expect(component.showPrescriptionModal).toBeFalse();
    expect(mockConsultationService.getConsultation).toHaveBeenCalledWith(1);
    expect(mockPrescriptionService.getPrescription).toHaveBeenCalledWith(5);
    expect(component.existingPrescriptionId).toBe(5);
  });

  it('should onPrescriptionSaved handle prescription without items', () => {
    const updatedConsultation = { ...mockConsultations[0], prescriptionId: undefined };
    mockConsultationService.getConsultation.and.returnValue(of(updatedConsultation));

    component.selectedConsultation = mockConsultations[0];
    component.showPrescriptionModal = true;

    component.onPrescriptionSaved();

    expect(component.showPrescriptionModal).toBeFalse();
    expect(component.existingPrescriptionId).toBeNull();
    expect(mockPrescriptionService.getPrescription).not.toHaveBeenCalled();
  });

  // ── Complete Consultation Tests ──

  it('should complete consultation and send email', () => {
    const completedConsultation = { ...mockConsultations[0], status: 'COMPLETED', prescriptionId: 5, requestedExams: 'Hématologie' };
    mockConsultationService.completeConsultation.and.returnValue(of(completedConsultation));
    mockPatientService.updatePatientMedicalRecord.and.returnValue(of({} as any));

    component.selectedConsultation = mockConsultations[0];
    component.editingConsultation.requestedExams = 'Hématologie';
    (component as any).savedPrescriptionItems = [{ medicamentId: 1, medicamentName: 'DOLIPRANE', posologie: '1x3/j' }];

    component.completeConsultation();

    expect(mockConsultationService.completeConsultation).toHaveBeenCalledWith(1, jasmine.any(Object));
    expect(mockPrescriptionService.sendPrescriptionEmail).toHaveBeenCalled();
    const emailArg = mockPrescriptionService.sendPrescriptionEmail.calls.mostRecent().args[0] as PrescriptionEmailRequest;
    expect(emailArg.patientEmail).toBe('john@test.com');
    expect(emailArg.patientName).toBe('John Doe');
  });

  it('should complete consultation without email when no patient email', () => {
    const completedConsultation = { ...mockConsultations[1], status: 'COMPLETED' };
    mockConsultationService.completeConsultation.and.returnValue(of(completedConsultation));
    mockPatientService.updatePatientMedicalRecord.and.returnValue(of({} as any));
    spyOn(console, 'warn');

    component.selectedConsultation = mockConsultations[1];
    (component as any).savedPrescriptionItems = [{ medicamentId: 1, medicamentName: 'DOLIPRANE' }];

    component.completeConsultation();

    expect(mockConsultationService.completeConsultation).toHaveBeenCalledWith(2, jasmine.any(Object));
    expect(mockPrescriptionService.sendPrescriptionEmail).not.toHaveBeenCalled();
    expect(console.warn).toHaveBeenCalledWith('Aucun email trouvé pour le patient, email non envoyé.');
  });

  // ── Send Prescription Email Tests ──

  it('should send email with saved prescription items', () => {
    component.selectedConsultation = mockConsultations[0];
    (component as any).savedPrescriptionItems = [{ medicamentId: 1, medicamentName: 'DOLIPRANE' }];
    component.editingConsultation.requestedExams = 'Hématologie,Biochimie';

    spyOn<any>(component, 'generateMedicationPdfBase64').and.returnValue('pdf-base64-data');
    spyOn<any>(component, 'generateAnalysesPdfBase64').and.returnValue('analyses-pdf-base64');

    (component as any).sendPrescriptionEmailAfterCompletion();

    expect(mockPrescriptionService.sendPrescriptionEmail).toHaveBeenCalled();
    const emailArg = mockPrescriptionService.sendPrescriptionEmail.calls.mostRecent().args[0] as PrescriptionEmailRequest;
    expect(emailArg.patientEmail).toBe('john@test.com');
    expect(emailArg.patientName).toBe('John Doe');
    expect(emailArg.pdfMedicationsBase64).toBeDefined();
    expect(emailArg.pdfAnalysesBase64).toBeDefined();
  });

  it('should send email by fetching prescription when savedItems empty', () => {
    component.selectedConsultation = mockConsultations[0];
    component.existingPrescriptionId = 5;
    (component as any).savedPrescriptionItems = [];
    component.editingConsultation.requestedExams = '';

    const prescriptionResp = { id: 5, items: [{ medicamentId: 1, medicamentName: 'DOLIPRANE', posologie: '1x3/j' }] };
    mockPrescriptionService.getPrescription.and.returnValue(of(prescriptionResp as any));

    spyOn<any>(component, 'generateMedicationPdfBase64').and.returnValue('pdf-base64');

    (component as any).sendPrescriptionEmailAfterCompletion();

    expect(mockPrescriptionService.getPrescription).toHaveBeenCalledWith(5);
    expect(mockPrescriptionService.sendPrescriptionEmail).toHaveBeenCalled();
  });

  it('should not send email when no prescription and no analyses', () => {
    component.selectedConsultation = mockConsultations[0];
    (component as any).savedPrescriptionItems = [];
    component.existingPrescriptionId = null;
    component.editingConsultation.requestedExams = '';

    (component as any).sendPrescriptionEmailAfterCompletion();

    expect(mockPrescriptionService.sendPrescriptionEmail).not.toHaveBeenCalled();
  });

  it('should not send email when no patient email', () => {
    component.selectedConsultation = mockConsultations[1];
    (component as any).savedPrescriptionItems = [{ medicamentId: 1 }];

    spyOn(console, 'warn');

    (component as any).sendPrescriptionEmailAfterCompletion();

    expect(mockPrescriptionService.sendPrescriptionEmail).not.toHaveBeenCalled();
    expect(console.warn).toHaveBeenCalled();
  });

  // ── Analyses ──

  it('should update requestedExams on analyses change', () => {
    component.onAnalysesChanged('Hématologie, Biochimie');
    expect(component.editingConsultation.requestedExams).toBe('Hématologie, Biochimie');
  });

  // ── Error Handling ──

  it('should handle complete consultation error', () => {
    spyOn(console, 'error');
    mockConsultationService.completeConsultation.and.returnValue(throwError(() => new Error('API error')));
    mockPatientService.updatePatientMedicalRecord.and.returnValue(of({} as any));

    component.selectedConsultation = mockConsultations[0];
    component.completeConsultation();

    expect(console.error).toHaveBeenCalledWith('Complete consultation failed:', jasmine.any(Error));
  });

  it('should handle prescription fetch error in email sending', () => {
    spyOn(console, 'error');
    component.selectedConsultation = mockConsultations[0];
    component.existingPrescriptionId = 999;
    (component as any).savedPrescriptionItems = [];
    component.editingConsultation.requestedExams = '';

    mockPrescriptionService.getPrescription.and.returnValue(throwError(() => new Error('Not found')));

    (component as any).sendPrescriptionEmailAfterCompletion();

    expect(console.error).toHaveBeenCalledWith('Erreur récupération prescription:', jasmine.any(Error));
    expect(mockPrescriptionService.sendPrescriptionEmail).not.toHaveBeenCalled();
  });
});
