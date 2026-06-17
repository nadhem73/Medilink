import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';
import { ConsultationsComponent } from './consultations.component';
import { ConsultationService, ConsultationResponse } from '../../../core/services/consultation.service';

describe('ConsultationsComponent', () => {
  let component: ConsultationsComponent;
  let fixture: ComponentFixture<ConsultationsComponent>;
  let mockService: jasmine.SpyObj<ConsultationService>;

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
    mockService = jasmine.createSpyObj('ConsultationService', [
      'getAllConsultations', 'getTodayConsultations', 'getConsultation',
      'startConsultation', 'updateConsultation', 'completeConsultation', 'cancelConsultation'
    ]);
    mockService.getAllConsultations.and.returnValue(of(mockConsultations));

    await TestBed.configureTestingModule({
      declarations: [ConsultationsComponent],
      providers: [{ provide: ConsultationService, useValue: mockService }],
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
    expect(mockService.getAllConsultations).toHaveBeenCalledWith(undefined);
    expect(component.consultations.length).toBe(2);
  });

  it('should filter consultations by status', () => {
    mockService.getAllConsultations.and.returnValue(of([mockConsultations[1]]));
    component.filterByStatus('IN_PROGRESS');
    expect(mockService.getAllConsultations).toHaveBeenCalledWith('IN_PROGRESS');
  });

  it('should select a consultation', () => {
    component.selectConsultation(mockConsultations[0]);
    expect(component.selectedConsultation).toEqual(mockConsultations[0]);
  });

  it('should go back to list', () => {
    component.selectedConsultation = mockConsultations[0];
    component.backToList();
    expect(component.selectedConsultation).toBeNull();
  });

  it('should start editing a consultation', () => {
    component.startEdit(mockConsultations[1]);
    expect(component.editing).toBeTrue();
    expect(component.editingConsultation.patientId).toBe(11);
    expect(component.editingConsultation.diagnosis).toBe('Migraine');
  });

  it('should cancel edit', () => {
    component.editing = true;
    component.cancelEdit();
    expect(component.editing).toBeFalse();
  });

  it('should save draft', () => {
    const updated = { ...mockConsultations[0], diagnosis: 'Tension' };
    mockService.updateConsultation.and.returnValue(of(updated));
    component.selectedConsultation = mockConsultations[0];
    component.editingConsultation = { diagnosis: 'Tension' };
    component.saveDraft();
    expect(mockService.updateConsultation).toHaveBeenCalledWith(1, { diagnosis: 'Tension' });
  });

  it('should complete consultation', () => {
    const completed = { ...mockConsultations[1], status: 'COMPLETED' };
    mockService.completeConsultation.and.returnValue(of(completed));
    component.selectedConsultation = mockConsultations[1];
    component.completeConsultation();
    expect(mockService.completeConsultation).toHaveBeenCalledWith(2, {});
  });

  it('should cancel consultation', () => {
    mockService.cancelConsultation.and.returnValue(of(void 0));
    component.cancelConsultation(mockConsultations[0]);
    expect(mockService.cancelConsultation).toHaveBeenCalledWith(1);
  });

  it('should return correct status badge class', () => {
    expect(component.getStatusClass('PENDING')).toBe('badge-warning');
    expect(component.getStatusClass('IN_PROGRESS')).toBe('badge-info');
    expect(component.getStatusClass('COMPLETED')).toBe('badge-success');
    expect(component.getStatusClass('CANCELLED')).toBe('badge-danger');
    expect(component.getStatusClass('UNKNOWN')).toBe('');
  });
});
