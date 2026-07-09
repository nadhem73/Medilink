import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { PrescriptionService } from '../../../core/services/prescription.service';
import { PharmacyPrescriptionsComponent } from './pharmacy-prescriptions.component';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('PharmacyPrescriptionsComponent', () => {
  let component: PharmacyPrescriptionsComponent;
  let fixture: ComponentFixture<PharmacyPrescriptionsComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let doctorServiceSpy: jasmine.SpyObj<DoctorService>;
  let prescriptionServiceSpy: jasmine.SpyObj<PrescriptionService>;

  const mockPatients = [
    { id: 1, firstName: 'Nadhem', lastName: 'Hmidani' },
    { id: 2, firstName: 'Sami', lastName: 'Ben Ahmed' }
  ];

  const mockDoctors = [
    { id: 1, firstName: 'Ali', lastName: 'Mezni' }
  ];

  const mockPrescriptions = [
    {
      id: 1,
      patientId: 1,
      doctorId: 1,
      status: 'SOUMISE',
      notes: '',
      items: [{ medicamentName: 'DOLIPRANE', dosage: '500mg', forme: 'Comprimé', posologie: '1x3/j' }],
      createdAt: '2026-07-08T10:00:00',
      updatedAt: '2026-07-08T10:00:00',
      consultationId: 100,
      pharmacieId: 5
    },
    {
      id: 2,
      patientId: 2,
      doctorId: 1,
      status: 'EN_PREPARATION',
      notes: '',
      items: [{ medicamentName: 'AMOXICILLINE', dosage: '250mg', forme: 'Gélule', posologie: '1x2/j' }],
      createdAt: '2026-07-07T14:30:00',
      updatedAt: '2026-07-08T09:00:00',
      consultationId: 101,
      pharmacieId: 5
    }
  ];

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser', 'getAllPatients']);
    doctorServiceSpy = jasmine.createSpyObj('DoctorService', ['getAllDoctors']);
    prescriptionServiceSpy = jasmine.createSpyObj('PrescriptionService', [
      'getAllPrescriptions',
      'getPrescriptionsByPharmacy',
      'updateStatus',
      'getPickupCode',
      'validatePickupCode'
    ]);

    authServiceSpy.getCurrentUser.and.returnValue({ pharmacieId: 5 } as any);
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients as any));
    doctorServiceSpy.getAllDoctors.and.returnValue(of(mockDoctors as any));
    prescriptionServiceSpy.getPrescriptionsByPharmacy.and.returnValue(of(mockPrescriptions as any));

    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [PharmacyPrescriptionsComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: DoctorService, useValue: doctorServiceSpy },
        { provide: PrescriptionService, useValue: prescriptionServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PharmacyPrescriptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load prescriptions on init', () => {
    expect(authServiceSpy.getCurrentUser).toHaveBeenCalled();
    expect(authServiceSpy.getAllPatients).toHaveBeenCalled();
    expect(doctorServiceSpy.getAllDoctors).toHaveBeenCalled();
    expect(prescriptionServiceSpy.getPrescriptionsByPharmacy).toHaveBeenCalledWith(5);
    expect(component.allPrescriptions.length).toBe(2);
  });

  it('should map patient names', () => {
    expect(component.getPatientName(1)).toBe('Nadhem Hmidani');
    expect(component.getPatientName(2)).toBe('Sami Ben Ahmed');
    expect(component.getPatientName(99)).toBe('Patient #99');
  });

  it('should map doctor names', () => {
    expect(component.getDoctorName(1)).toBe('Dr Ali Mezni');
    expect(component.getDoctorName(99)).toBe('Dr #99');
  });

  it('should filter prescriptions by status', () => {
    component.statusFilter = 'SOUMISE';
    expect(component.filteredPrescriptions.length).toBe(1);
    expect(component.filteredPrescriptions[0].id).toBe(1);
  });

  it('should filter prescriptions by search query', () => {
    component.searchQuery = 'doliprane';
    expect(component.filteredPrescriptions.length).toBe(1);
    expect(component.filteredPrescriptions[0].id).toBe(1);
  });

  it('should toggle status filter', () => {
    component.setStatusFilter('SOUMISE');
    expect(component.statusFilter).toBe('SOUMISE');
    component.setStatusFilter('SOUMISE');
    expect(component.statusFilter).toBe('');
  });

  it('should sort prescriptions ascending', () => {
    component.sortOrder = 'asc';
    const filtered = component.filteredPrescriptions;
    expect(filtered.length).toBe(2);
    expect(filtered[0].id).toBe(2);
    expect(filtered[1].id).toBe(1);
  });

  it('should select prescription and reset pickup state', () => {
    component.selectPrescription(mockPrescriptions[0] as any);
    expect(component.selectedPrescription?.id).toBe(1);
    expect(component.actionError).toBe('');
    expect(component.pickupCode).toBeNull();
    expect(component.pickupCodeInput).toBe('');
    expect(component.pickupCodeError).toBe('');
    expect(component.pickupCodeSuccess).toBe('');
  });

  it('should load pickup code when selecting prepared prescription', () => {
    const prepared = { ...mockPrescriptions[0], id: 3, status: 'PREPAREE' };
    prescriptionServiceSpy.getPickupCode.and.returnValue(of({ code: '123456' } as any));
    component.selectPrescription(prepared as any);
    expect(prescriptionServiceSpy.getPickupCode).toHaveBeenCalledWith(3);
  });

  it('should close detail and reset state', () => {
    component.selectedPrescription = mockPrescriptions[0] as any;
    component.pickupCode = '123456';
    component.pickupCodeInput = '654321';
    component.pickupCodeError = 'error';
    component.pickupCodeSuccess = 'success';
    component.closeDetail();
    expect(component.selectedPrescription).toBeNull();
    expect(component.pickupCode).toBeNull();
    expect(component.pickupCodeInput).toBe('');
    expect(component.pickupCodeError).toBe('');
    expect(component.pickupCodeSuccess).toBe('');
  });

  it('should return correct card border color per status', () => {
    expect(component.getCardBorderColor('SOUMISE')).toBe('#6d28d9');
    expect(component.getCardBorderColor('EN_PREPARATION')).toBe('#b45309');
    expect(component.getCardBorderColor('PREPAREE')).toBe('#0e7490');
    expect(component.getCardBorderColor('RETIREE')).toBe('#15803d');
    expect(component.getCardBorderColor('DISPENSEE')).toBe('#1e7a45');
    expect(component.getCardBorderColor('ANNULEE')).toBe('#b91c1c');
    expect(component.getCardBorderColor('UNKNOWN')).toBe('transparent');
  });

  it('should return correct status label', () => {
    expect(component.getStatusLabel('SOUMISE')).toBe('Soumise');
    expect(component.getStatusLabel('EN_PREPARATION')).toBe('En préparation');
    expect(component.getStatusLabel('PREPAREE')).toBe('Préparée');
    expect(component.getStatusLabel('RETIREE')).toBe('Retirée');
    expect(component.getStatusLabel('DISPENSEE')).toBe('Dispensée');
    expect(component.getStatusLabel('ANNULEE')).toBe('Annulée');
    expect(component.getStatusLabel('BROUILLON')).toBe('Brouillon');
    expect(component.getStatusLabel('OTHER')).toBe('OTHER');
  });

  it('should return correct status class', () => {
    expect(component.getStatusClass('SOUMISE')).toBe('status-soumise');
    expect(component.getStatusClass('EN_PREPARATION')).toBe('status-en-preparation');
    expect(component.getStatusClass('PREPAREE')).toBe('status-preparee');
    expect(component.getStatusClass('RETIREE')).toBe('status-retiree');
    expect(component.getStatusClass('DISPENSEE')).toBe('status-dispensee');
    expect(component.getStatusClass('ANNULEE')).toBe('status-annulee');
    expect(component.getStatusClass('BROUILLON')).toBe('status-brouillon');
    expect(component.getStatusClass('OTHER')).toBe('');
  });

  it('should compute canStartPreparation correctly', () => {
    component.selectedPrescription = null;
    expect(component.canStartPreparation).toBeFalse();
    component.selectedPrescription = { status: 'SOUMISE' } as any;
    expect(component.canStartPreparation).toBeTrue();
    component.selectedPrescription = { status: 'EN_PREPARATION' } as any;
    expect(component.canStartPreparation).toBeFalse();
  });

  it('should compute canMarkAsPrepared correctly', () => {
    component.selectedPrescription = null;
    expect(component.canMarkAsPrepared).toBeFalse();
    component.selectedPrescription = { status: 'EN_PREPARATION' } as any;
    expect(component.canMarkAsPrepared).toBeTrue();
  });

  it('should compute canDispense correctly', () => {
    component.selectedPrescription = null;
    expect(component.canDispense).toBeFalse();
    component.selectedPrescription = { status: 'RETIREE' } as any;
    expect(component.canDispense).toBeTrue();
  });

  it('should compute canValidatePickup correctly', () => {
    component.selectedPrescription = null;
    expect(component.canValidatePickup).toBeFalse();
    component.selectedPrescription = { status: 'PREPAREE' } as any;
    component.pickupCode = null;
    expect(component.canValidatePickup).toBeFalse();
    component.pickupCode = '123456';
    expect(component.canValidatePickup).toBeTrue();
  });

  it('should update status and refresh list on success', () => {
    const updated = { ...mockPrescriptions[0], status: 'EN_PREPARATION' };
    prescriptionServiceSpy.updateStatus.and.returnValue(of(updated as any));
    prescriptionServiceSpy.getPrescriptionsByPharmacy.and.returnValue(of(mockPrescriptions as any));
    component.selectedPrescription = mockPrescriptions[0] as any;
    component.startPreparation();
    expect(prescriptionServiceSpy.updateStatus).toHaveBeenCalledWith(1, 'EN_PREPARATION');
    expect(component.selectedPrescription?.status).toBe('EN_PREPARATION');
  });

  it('should set actionError on update status failure', () => {
    prescriptionServiceSpy.updateStatus.and.returnValue(throwError(() => ({
      error: { error: 'Erreur test' }
    })));
    prescriptionServiceSpy.getPrescriptionsByPharmacy.and.returnValue(of(mockPrescriptions as any));
    component.selectedPrescription = mockPrescriptions[0] as any;
    component.startPreparation();
    expect(component.actionError).toBe('Erreur test');
  });

  it('should validate pickup code and update prescription', () => {
    const updated = { ...mockPrescriptions[0], status: 'RETIREE' };
    prescriptionServiceSpy.validatePickupCode.and.returnValue(of(updated as any));
    component.selectedPrescription = mockPrescriptions[0] as any;
    component.pickupCodeInput = '123456';
    component.validatePickup();
    expect(prescriptionServiceSpy.validatePickupCode).toHaveBeenCalledWith(1, '123456');
    expect(component.pickupCodeSuccess).toBe('Code validé. Ordonnance retirée.');
    expect(component.pickupCodeInput).toBe('');
    expect(component.pickupCode).toBeNull();
  });

  it('should set error on invalid pickup code', () => {
    prescriptionServiceSpy.validatePickupCode.and.returnValue(throwError(() => ({})));
    component.selectedPrescription = mockPrescriptions[0] as any;
    component.pickupCodeInput = '000000';
    component.validatePickup();
    expect(component.pickupCodeError).toBe('Code invalide. Veuillez réessayer.');
  });

  it('should format date correctly', () => {
    const result = component.formatDate('2026-07-08T10:00:00');
    expect(result).toContain('2026');
    expect(result).toContain('juillet');
  });

  it('should format hour correctly', () => {
    const result = component.formatHour('2026-07-08T10:05:00');
    expect(result).toBe('10:05');
  });

  it('should join medication names', () => {
    const items = [{ medicamentName: 'DOLIPRANE' }, { medicamentName: 'AMOXICILLINE' }];
    expect(component.getMedicationNames(items)).toBe('DOLIPRANE, AMOXICILLINE');
  });

  it('should map voie labels', () => {
    expect(component.voieLabel('ORALE')).toBe('Orale');
    expect(component.voieLabel('')).toBe('—');
    expect(component.voieLabel(undefined)).toBe('—');
  });

  it('should provide voie options', () => {
    expect(component.voieOptions).toContain('Orale');
    expect(component.voieOptions.length).toBe(8);
  });
});
