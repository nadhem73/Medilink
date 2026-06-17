import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PatientSectionComponent } from './patient-section.component';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService, DoctorWithProfile } from '../../../core/services/doctor.service';
import { AppointmentService, AppointmentDto } from '../../../core/services/appointment.service';

describe('PatientSectionComponent', () => {
  let component: PatientSectionComponent;
  let fixture: ComponentFixture<PatientSectionComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let doctorServiceSpy: jasmine.SpyObj<DoctorService>;
  let appointmentServiceSpy: jasmine.SpyObj<AppointmentService>;
  let activatedRouteStub: Partial<ActivatedRoute>;

  const mockUser = {
    firstName: 'Mohamed',
    lastName: 'Aloui',
    email: 'mohamed@test.com',
    phone: '20123456',
    address: 'Tunis',
    birthDate: '1990-01-15',
    gender: 'M'
  };

  const mockDoctors: DoctorWithProfile[] = [
    { id: 1, firstName: 'Yasmine', lastName: 'Ben Salem', email: 'yasmine@test.com', phone: '20111111', specialty: 'Cardiologie', hospital: 'Clinique El Manar', licenseNumber: '12345', available: true, biography: 'Cardiologue', fee: 60, debutMatin: '08:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00' },
    { id: 2, firstName: 'Ines', lastName: 'Gharbi', email: 'ines@test.com', phone: '20222222', specialty: 'Medecine generale', hospital: 'Hopital Charles Nicolle', licenseNumber: '67890', available: true, biography: 'Generaliste', fee: 50, debutMatin: '09:00', finMatin: '14:00', debutApresMidi: '16:00', finApresMidi: '20:00' }
  ];

  const mockAppointments: AppointmentDto[] = [
    { id: 1, patientId: 1, doctorId: 1, dateTime: '2026-06-20T09:30:00', status: 'CONFIRMED', mode: 'PRESENTIEL', notes: 'Consultation', createdAt: '2026-06-10T10:00:00' },
    { id: 2, patientId: 1, doctorId: 2, dateTime: '2026-06-22T11:00:00', status: 'PENDING', mode: 'TELECONSULTATION', notes: 'Suivi', createdAt: '2026-06-10T10:00:00' }
  ];

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    doctorServiceSpy = jasmine.createSpyObj('DoctorService', ['getDoctorsWithProfiles']);
    appointmentServiceSpy = jasmine.createSpyObj('AppointmentService', ['getMyAppointments', 'getActiveDoctorIds', 'getAvailableSlots', 'createAppointment', 'cancelAppointment']);
    activatedRouteStub = {
      data: of({ section: 'appointments', title: 'Mes Rendez-vous' })
    };

    authServiceSpy.getCurrentUser.and.returnValue(mockUser);

    await TestBed.configureTestingModule({
      declarations: [PatientSectionComponent],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: DoctorService, useValue: doctorServiceSpy },
        { provide: AppointmentService, useValue: appointmentServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(PatientSectionComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([1]));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should render appointments section with data', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([1]));
    fixture.detectChanges();

    expect(component.section).toBe('appointments');
    expect(component.title).toBe('Mes Rendez-vous');
    expect(component.appointments.length).toBe(2);
    expect(component.doctors.length).toBe(2);
  });

  it('should load doctors and appointments on init when section is appointments', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([1]));
    fixture.detectChanges();

    expect(appointmentServiceSpy.getMyAppointments).toHaveBeenCalled();
    expect(doctorServiceSpy.getDoctorsWithProfiles).toHaveBeenCalled();
    expect(appointmentServiceSpy.getActiveDoctorIds).toHaveBeenCalled();
    expect(component.bookedDoctorIds).toEqual([1]);
  });

  it('should handle load appointments error', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(throwError(() => new Error('error')));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    expect(component.loadingAppointments).toBeFalse();
  });

  it('should handle load doctors error', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(throwError(() => new Error('error')));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    expect(component.loadingDoctors).toBeFalse();
  });

  it('should handle getActiveDoctorIds error', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();

    expect(component.bookedDoctorIds).toEqual([]);
  });

  it('should check if a doctor is booked', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([1]));
    fixture.detectChanges();

    expect(component.isDoctorBooked(1)).toBeTrue();
    expect(component.isDoctorBooked(2)).toBeFalse();
  });

  it('should filter doctors by specialty', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    component.selectedSpecialty = 'Cardiologie';
    component.filterDoctors();
    expect(component.filteredDoctors.length).toBe(1);
    expect(component.filteredDoctors[0].specialty).toBe('Cardiologie');
  });

  it('should filter doctors by search query', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    component.searchQuery = 'Yasmine';
    component.filterDoctors();
    expect(component.filteredDoctors.length).toBe(1);
    expect(component.filteredDoctors[0].firstName).toBe('Yasmine');
  });

  it('should switch tabs', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    component.switchTab('book');
    expect(component.activeTab).toBe('book');

    component.switchTab('list');
    expect(component.activeTab).toBe('list');
  });

  it('should select a doctor for booking', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    component.selectDoctor(mockDoctors[0]);
    expect(component.selectedDoctor).toEqual(mockDoctors[0]);
  });

  it('should cancel booking and clear selection', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();
    component.selectDoctor(mockDoctors[0]);

    component.cancelBooking();
    expect(component.selectedDoctor).toBeNull();
    expect(component.availableSlots).toEqual([]);
    expect(component.bookingTime).toBe('');
  });

  it('should extract unique specialties from doctors', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    expect(component.specialties).toContain('Cardiologie');
    expect(component.specialties).toContain('Medecine generale');
  });

  it('should get status class correctly', () => {
    expect(component.getStatusClass('CONFIRMED')).toBe('status-confirmed');
    expect(component.getStatusClass('CANCELLED')).toBe('status-cancelled');
    expect(component.getStatusClass('PENDING')).toBe('status-pending');
    expect(component.getStatusClass('UNKNOWN')).toBe('status-pending');
  });

  it('should get status label correctly', () => {
    expect(component.getStatusLabel('CONFIRMED')).toBe('Confirmé');
    expect(component.getStatusLabel('CANCELLED')).toBe('Annulé');
    expect(component.getStatusLabel('PENDING')).toBe('En attente');
  });

  it('should return pending count', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    expect(component.getPendingCount()).toBe(1);
  });

  it('should load available slots on date change', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    appointmentServiceSpy.getAvailableSlots.and.returnValue(of(['09:00', '09:30', '10:00']));
    fixture.detectChanges();
    component.selectDoctor(mockDoctors[0]);

    component.bookingDate = '2026-06-25';
    component.onDateChange();

    expect(appointmentServiceSpy.getAvailableSlots).toHaveBeenCalled();
    expect(component.availableSlots.length).toBe(3);
  });

  it('should not load slots when no doctor or date selected', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    component.onDateChange();
    expect(appointmentServiceSpy.getAvailableSlots).not.toHaveBeenCalled();
  });

  it('should handle getAvailableSlots error', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    appointmentServiceSpy.getAvailableSlots.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();
    component.selectDoctor(mockDoctors[0]);
    component.bookingDate = '2026-06-25';
    component.onDateChange();

    expect(component.availableSlots).toEqual([]);
    expect(component.loadingSlots).toBeFalse();
  });

  it('should show error when booking without date or time', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();
    component.selectDoctor(mockDoctors[0]);

    component.bookAppointment();
    expect(component.errorMessage).toBe('Veuillez selectionner une date et une heure.');
  });

  it('should create appointment successfully', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    appointmentServiceSpy.getAvailableSlots.and.returnValue(of(['09:00']));
    appointmentServiceSpy.createAppointment.and.returnValue(of(mockAppointments[0]));
    fixture.detectChanges();
    component.selectDoctor(mockDoctors[0]);
    component.bookingDate = '2026-06-25';
    component.bookingTime = '09:00';

    component.bookAppointment();

    expect(appointmentServiceSpy.createAppointment).toHaveBeenCalled();
    expect(component.submitting).toBeFalse();
  });

  it('should handle create appointment error', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    appointmentServiceSpy.getAvailableSlots.and.returnValue(of(['09:00']));
    appointmentServiceSpy.createAppointment.and.returnValue(throwError(() => new Error('error')));
    fixture.detectChanges();
    component.selectDoctor(mockDoctors[0]);
    component.bookingDate = '2026-06-25';
    component.bookingTime = '09:00';

    component.bookAppointment();

    expect(component.submitting).toBeFalse();
    expect(component.errorMessage).toBe('Une erreur est survenue lors de la reservation du rendez-vous. Veuillez reessayer.');
  });

  it('should cancel an appointment', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    appointmentServiceSpy.cancelAppointment.and.returnValue(of(mockAppointments[0]));
    fixture.detectChanges();

    component.cancelAppointment(1);
    expect(appointmentServiceSpy.cancelAppointment).toHaveBeenCalledWith(1);
  });

  it('should not cancel appointment if user declines confirm', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    component.cancelAppointment(1);
    expect(appointmentServiceSpy.cancelAppointment).not.toHaveBeenCalled();
  });

  it('should get doctor details by id', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    const details = component.getDoctorDetails(1);
    expect(details.name).toBe('Dr. Yasmine Ben Salem');
    expect(details.specialty).toBe('Cardiologie');
  });

  it('should return fallback for unknown doctor id', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    fixture.detectChanges();

    const details = component.getDoctorDetails(999);
    expect(details.name).toBe('Medecin #999');
    expect(details.specialty).toBe('General');
  });

  it('should not load appointments for non-appointments sections', () => {
    activatedRouteStub.data = of({ section: 'prescriptions', title: 'Mes Ordonnances' });
    const fixture2 = TestBed.createComponent(PatientSectionComponent);
    const component2 = fixture2.componentInstance;
    fixture2.detectChanges();

    expect(component2.section).toBe('prescriptions');
    expect(component2.title).toBe('Mes Ordonnances');
  });

  it('should handle create appointment error with server message', () => {
    doctorServiceSpy.getDoctorsWithProfiles.and.returnValue(of(mockDoctors));
    appointmentServiceSpy.getMyAppointments.and.returnValue(of(mockAppointments));
    appointmentServiceSpy.getActiveDoctorIds.and.returnValue(of([]));
    const serverError = { error: 'Ce creneau est deja pris.' };
    appointmentServiceSpy.createAppointment.and.returnValue(throwError(() => serverError));
    fixture.detectChanges();
    component.selectDoctor(mockDoctors[0]);
    component.bookingDate = '2026-06-25';
    component.bookingTime = '09:00';

    component.bookAppointment();

    expect(component.errorMessage).toBe('Ce creneau est deja pris.');
  });
});
