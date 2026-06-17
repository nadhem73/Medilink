import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DoctorSectionComponent } from './doctor-section.component';
import { AuthService, PatientListDto } from '../../../core/services/auth.service';
import { AppointmentService, AppointmentDto } from '../../../core/services/appointment.service';

describe('DoctorSectionComponent', () => {
  let component: DoctorSectionComponent;
  let fixture: ComponentFixture<DoctorSectionComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let appointmentServiceSpy: jasmine.SpyObj<AppointmentService>;
  let activatedRouteStub: Partial<ActivatedRoute>;

  const mockPatients: PatientListDto[] = [
    { id: 1, firstName: 'Mohamed', lastName: 'Aloui', email: 'm@test.com', phone: '20123456' },
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
    appointmentServiceSpy = jasmine.createSpyObj('AppointmentService', ['getDoctorAppointments', 'confirmAppointment', 'cancelByDoctor']);
    activatedRouteStub = {
      data: of({ section: 'patients', title: 'Mes Patients' })
    };

    authServiceSpy.getCurrentUser.and.returnValue(mockUser);

    await TestBed.configureTestingModule({
      declarations: [DoctorSectionComponent],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: AppointmentService, useValue: appointmentServiceSpy }
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

  it('should cancel appointment', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    const updatedDto: AppointmentDto = { ...mockAppointments[0], status: 'CANCELLED' };
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.cancelByDoctor.and.returnValue(of(updatedDto));
    fixture.detectChanges();

    const targetApp = component.allAppointments[0];
    component.cancelAppointment(targetApp);

    expect(appointmentServiceSpy.cancelByDoctor).toHaveBeenCalledWith(1);
    const updated = component.allAppointments.find(a => a.id === 1);
    expect(updated?.status).toBe('Annulé');
    expect(updated?.statusClass).toBe('status-cancelled');
  });

  it('should not cancel appointment if user declines confirm', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    fixture.detectChanges();

    const targetApp = component.allAppointments[0];
    component.cancelAppointment(targetApp);

    expect(appointmentServiceSpy.cancelByDoctor).not.toHaveBeenCalled();
  });

  it('should handle cancel appointment error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    authServiceSpy.getAllPatients.and.returnValue(of(mockPatients));
    appointmentServiceSpy.getDoctorAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.cancelByDoctor.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    const targetApp = component.allAppointments[0];
    component.cancelAppointment(targetApp);

    expect(appointmentServiceSpy.cancelByDoctor).toHaveBeenCalledWith(1);
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
});
