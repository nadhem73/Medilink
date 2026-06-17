import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PatientService, MedicalRecord } from './patient.service';

describe('PatientService', () => {
  let service: PatientService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PatientService]
    });
    service = TestBed.inject(PatientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get my medical record', () => {
    const mock: MedicalRecord = { userId: 1, bloodGroup: 'O+', height: 175, weight: 70 };

    service.getMyMedicalRecord().subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne('http://localhost:8765/api/patients/me/medical-record');
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('should get patient medical record by userId', () => {
    const mock: MedicalRecord = { userId: 2, bloodGroup: 'A+', allergies: 'None' };

    service.getPatientMedicalRecord(2).subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne('http://localhost:8765/api/patients/2/medical-record');
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('should handle empty medical record', () => {
    service.getMyMedicalRecord().subscribe(res => {
      expect(res.userId).toBe(1);
      expect(res.bloodGroup).toBeUndefined();
    });
    const req = httpMock.expectOne('http://localhost:8765/api/patients/me/medical-record');
    req.flush({ userId: 1 });
  });

  it('should handle error on getMyMedicalRecord', () => {
    service.getMyMedicalRecord().subscribe({
      error: err => expect(err.status).toBe(500)
    });
    const req = httpMock.expectOne('http://localhost:8765/api/patients/me/medical-record');
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
  });

  it('should handle error on getPatientMedicalRecord', () => {
    service.getPatientMedicalRecord(99).subscribe({
      error: err => expect(err.status).toBe(404)
    });
    const req = httpMock.expectOne('http://localhost:8765/api/patients/99/medical-record');
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
  });
});
