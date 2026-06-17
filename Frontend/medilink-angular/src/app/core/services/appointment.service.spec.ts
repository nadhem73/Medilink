import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { AppointmentService, AppointmentRequest, AppointmentDto } from './appointment.service';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
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

  describe('createAppointment()', () => {
    it('should POST to /api/patients/appointments', () => {
      const request: AppointmentRequest = { doctorId: 1, dateTime: '2025-01-15T10:00:00', mode: 'PRESENTIEL', notes: 'Checkup' };
      const response: AppointmentDto = { id: 1, patientId: 1, doctorId: 1, dateTime: '2025-01-15T10:00:00', status: 'PENDING', mode: 'PRESENTIEL', notes: 'Checkup', createdAt: '2025-01-10T00:00:00' };

      service.createAppointment(request).subscribe(res => {
        expect(res).toEqual(response);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(response);
    });
  });

  describe('getMyAppointments()', () => {
    it('should GET /api/patients/appointments', () => {
      const appointments: AppointmentDto[] = [
        { id: 1, patientId: 1, doctorId: 2, dateTime: '2025-01-15T10:00:00', status: 'PENDING', mode: 'PRESENTIEL', createdAt: '2025-01-10T00:00:00' }
      ];

      service.getMyAppointments().subscribe(res => {
        expect(res).toEqual(appointments);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments');
      expect(req.request.method).toBe('GET');
      req.flush(appointments);
    });
  });

  describe('getDoctorAppointments()', () => {
    it('should GET /api/patients/appointments/doctor', () => {
      const appointments: AppointmentDto[] = [
        { id: 1, patientId: 1, doctorId: 2, dateTime: '2025-01-15T10:00:00', status: 'CONFIRMED', mode: 'TELECONSULTATION', createdAt: '2025-01-10T00:00:00' }
      ];

      service.getDoctorAppointments().subscribe(res => {
        expect(res).toEqual(appointments);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/doctor');
      expect(req.request.method).toBe('GET');
      req.flush(appointments);
    });
  });

  describe('cancelAppointment()', () => {
    it('should PUT to /api/patients/appointments/{id}/cancel', () => {
      const response: AppointmentDto = { id: 1, patientId: 1, doctorId: 2, dateTime: '2025-01-15T10:00:00', status: 'CANCELLED', mode: 'PRESENTIEL', createdAt: '2025-01-10T00:00:00' };

      service.cancelAppointment(1).subscribe(res => {
        expect(res).toEqual(response);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/1/cancel');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({});
      req.flush(response);
    });
  });

  describe('confirmAppointment()', () => {
    it('should PUT to /api/patients/appointments/{id}/confirm', () => {
      const response: AppointmentDto = { id: 1, patientId: 1, doctorId: 2, dateTime: '2025-01-15T10:00:00', status: 'CONFIRMED', mode: 'PRESENTIEL', createdAt: '2025-01-10T00:00:00' };

      service.confirmAppointment(1).subscribe(res => {
        expect(res).toEqual(response);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/1/confirm');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({});
      req.flush(response);
    });
  });

  describe('cancelByDoctor()', () => {
    it('should PUT to /api/patients/appointments/{id}/doctor-cancel', () => {
      const response: AppointmentDto = { id: 1, patientId: 1, doctorId: 2, dateTime: '2025-01-15T10:00:00', status: 'CANCELLED', mode: 'PRESENTIEL', createdAt: '2025-01-10T00:00:00' };

      service.cancelByDoctor(1).subscribe(res => {
        expect(res).toEqual(response);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/1/doctor-cancel');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({});
      req.flush(response);
    });
  });

  describe('getAvailableSlots()', () => {
    it('should GET with query params for doctorId, date, and time ranges', () => {
      const slots = ['08:00', '08:30', '09:00'];

      service.getAvailableSlots(1, '2025-01-15', '08:00', '13:00', '15:00', '19:00').subscribe(res => {
        expect(res).toEqual(slots);
      });

      const req = httpMock.expectOne(r => r.url === 'http://localhost:8765/api/patients/appointments/available-slots');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('doctorId')).toBe('1');
      expect(req.request.params.get('date')).toBe('2025-01-15');
      expect(req.request.params.get('debutMatin')).toBe('08:00');
      expect(req.request.params.get('finMatin')).toBe('13:00');
      expect(req.request.params.get('debutApresMidi')).toBe('15:00');
      expect(req.request.params.get('finApresMidi')).toBe('19:00');
      req.flush(slots);
    });
  });

  describe('checkAvailability()', () => {
    it('should GET with query params for doctorId and dateTime', () => {
      service.checkAvailability(1, '2025-01-15T10:00:00').subscribe(res => {
        expect(res).toBeTrue();
      });

      const req = httpMock.expectOne(r => r.url === 'http://localhost:8765/api/patients/appointments/check-availability');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('doctorId')).toBe('1');
      expect(req.request.params.get('dateTime')).toBe('2025-01-15T10:00:00');
      req.flush(true);
    });
  });

  describe('getActiveDoctorIds()', () => {
    it('should GET /api/patients/appointments/active-doctor-ids', () => {
      const ids = [1, 2, 3];

      service.getActiveDoctorIds().subscribe(res => {
        expect(res).toEqual(ids);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments/active-doctor-ids');
      expect(req.request.method).toBe('GET');
      req.flush(ids);
    });
  });
});
