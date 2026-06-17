import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppointmentService, AppointmentDto } from './appointment.service';

function testGetMethod(
  methodName: string,
  urlPath: string,
  getService: () => AppointmentService,
  getHttpMock: () => HttpTestingController,
  buildResponse: () => any
) {
  describe(`${methodName}()`, () => {
    it(`should GET ${urlPath}`, () => {
      const service = getService();
      const httpMock = getHttpMock();
      const response = buildResponse();
      (service as any)[methodName]().subscribe((res: any) => {
        expect(res).toEqual(response);
      });
      const req = httpMock.expectOne(`http://localhost:8765${urlPath}`);
      expect(req.request.method).toBe('GET');
      req.flush(response);
    });
  });
}

function testPutMethod(
  methodName: string,
  urlPath: string,
  status: string,
  getService: () => AppointmentService,
  getHttpMock: () => HttpTestingController
) {
  describe(`${methodName}()`, () => {
    it(`should PUT ${urlPath}`, () => {
      const service = getService();
      const httpMock = getHttpMock();
      const response: AppointmentDto = {
        id: 1, patientId: 1, doctorId: 2, dateTime: '2025-01-15T10:00:00',
        status: status as AppointmentDto['status'], mode: 'PRESENTIEL', createdAt: '2025-01-10T00:00:00'
      };
      (service as any)[methodName](1).subscribe((res: any) => {
        expect(res).toEqual(response);
      });
      const req = httpMock.expectOne(`http://localhost:8765${urlPath}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({});
      req.flush(response);
    });
  });
}

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;

  const baseResponse: AppointmentDto = {
    id: 1, patientId: 1, doctorId: 2, dateTime: '2025-01-15T10:00:00',
    status: 'PENDING', mode: 'PRESENTIEL', createdAt: '2025-01-10T00:00:00'
  };

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
      const request = { doctorId: 1, dateTime: '2025-01-15T10:00:00', mode: 'PRESENTIEL' as const, notes: 'Checkup' };
      const response = { ...baseResponse, doctorId: 1 };

      service.createAppointment(request).subscribe(res => {
        expect(res).toEqual(response);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/appointments');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(response);
    });
  });

  testGetMethod('getMyAppointments', '/api/patients/appointments', () => service, () => httpMock, () => [baseResponse]);
  testGetMethod('getDoctorAppointments', '/api/patients/appointments/doctor', () => service, () => httpMock, () => [{ ...baseResponse, status: 'CONFIRMED', mode: 'TELECONSULTATION' as const }]);

  testPutMethod('cancelAppointment', '/api/patients/appointments/1/cancel', 'CANCELLED', () => service, () => httpMock);
  testPutMethod('confirmAppointment', '/api/patients/appointments/1/confirm', 'CONFIRMED', () => service, () => httpMock);
  testPutMethod('cancelByDoctor', '/api/patients/appointments/1/doctor-cancel', 'CANCELLED', () => service, () => httpMock);

  describe('getAvailableSlots()', () => {
    it('should GET with query params', () => {
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
