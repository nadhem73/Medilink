import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppointmentService, AppointmentRequest, AppointmentDto } from './appointment.service';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AppointmentService]
    });
    service = TestBed.inject(AppointmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create an appointment', () => {
    const dto: AppointmentRequest = { doctorId: 1, dateTime: '2026-06-20T10:00:00', mode: 'PRESENTIEL' };
    const mock: AppointmentDto = { id: 1, patientId: 1, doctorId: 1, dateTime: '2026-06-20T10:00:00', status: 'PENDING', mode: 'PRESENTIEL', createdAt: '2026-06-18T12:00:00' };

    service.createAppointment(dto).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(mock);
  });

  it('should get my appointments', () => {
    const mock: AppointmentDto[] = [{ id: 1, patientId: 1, doctorId: 1, dateTime: '2026-06-20T10:00:00', status: 'PENDING', mode: 'PRESENTIEL', createdAt: '2026-06-18T12:00:00' }];

    service.getMyAppointments().subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments');
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('should get doctor appointments', () => {
    service.getDoctorAppointments().subscribe();
    const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/doctor');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should cancel an appointment', () => {
    const mock: AppointmentDto = { id: 1, patientId: 1, doctorId: 1, dateTime: '2026-06-20T10:00:00', status: 'CANCELLED', mode: 'PRESENTIEL', createdAt: '2026-06-18T12:00:00' };

    service.cancelAppointment(1).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/1/cancel');
    expect(req.request.method).toBe('PUT');
    req.flush(mock);
  });

  it('should get active doctor ids', () => {
    service.getActiveDoctorIds().subscribe(res => expect(res).toEqual([1, 2]));
    const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/active-doctor-ids');
    expect(req.request.method).toBe('GET');
    req.flush([1, 2]);
  });

  it('should get available slots', () => {
    service.getAvailableSlots(1, '2026-06-20', '08:00', '13:00', '15:00', '19:00').subscribe(res => expect(res).toEqual(['08:00', '08:30']));
    const req = httpMock.expectOne(r => r.url.includes('available-slots'));
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('doctorId')).toBe('1');
    expect(req.request.params.get('date')).toBe('2026-06-20');
    req.flush(['08:00', '08:30']);
  });

  it('should check availability', () => {
    service.checkAvailability(1, '2026-06-20T10:00:00').subscribe(res => expect(res).toBeTrue());
    const req = httpMock.expectOne(r => r.url.includes('check-availability'));
    expect(req.request.method).toBe('GET');
    req.flush(true);
  });

  it('should confirm an appointment', () => {
    service.confirmAppointment(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/1/confirm');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should cancel by doctor', () => {
    service.cancelByDoctor(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/1/doctor-cancel');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });
});
