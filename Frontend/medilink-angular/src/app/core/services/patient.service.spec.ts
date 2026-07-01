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

  describe('getMyMedicalRecord()', () => {
    it('should GET /api/patients/me/medical-record and return the medical record', () => {
      const mockRecord: MedicalRecord = {
        userId: 1,
        bloodGroup: 'A+',
        height: 175,
        weight: 70,
        allergies: 'Pollen',
        chronicDiseases: 'None',
        currentTreatments: 'None',
        emergencyContactName: 'Emergency Contact',
        emergencyContactPhone: '123456789',
        insuranceCompany: 'Company',
        insuranceNumber: 'INS-001'
      };

      service.getMyMedicalRecord().subscribe(res => expect(res).toEqual(mockRecord));
      const req = httpMock.expectOne('http://localhost:8765/api/patients/me/medical-record');
      expect(req.request.method).toBe('GET');
      req.flush(mockRecord);
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
  });

  describe('getPatientMedicalRecord()', () => {
    it('should GET /api/patients/:id/medical-record and return the record', () => {
      const mock: MedicalRecord = { userId: 2, bloodGroup: 'A+', allergies: 'None' };

      service.getPatientMedicalRecord(2).subscribe(res => expect(res).toEqual(mock));
      const req = httpMock.expectOne('http://localhost:8765/api/patients/2/medical-record');
      expect(req.request.method).toBe('GET');
      req.flush(mock);
    });

    it('should handle error on getPatientMedicalRecord', () => {
      service.getPatientMedicalRecord(99).subscribe({
        error: err => expect(err.status).toBe(404)
      });
      const req = httpMock.expectOne('http://localhost:8765/api/patients/99/medical-record');
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('updatePatientMedicalRecord()', () => {
    it('should PUT to update medical record and return updated record', () => {
      const mockUpdate: Partial<MedicalRecord> = { height: 180, weight: 75 };
      const mockResponse: MedicalRecord = { userId: 1, height: 180, weight: 75 };

      service.updatePatientMedicalRecord(1, mockUpdate).subscribe(res => expect(res).toEqual(mockResponse));
      const req = httpMock.expectOne('http://localhost:8765/api/patients/1/medical-record');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockUpdate);
      req.flush(mockResponse);
    });

    it('should handle error on updatePatientMedicalRecord', () => {
      service.updatePatientMedicalRecord(99, { height: 180 }).subscribe({
        error: err => expect(err.status).toBe(400)
      });
      const req = httpMock.expectOne('http://localhost:8765/api/patients/99/medical-record');
      req.flush('Bad request', { status: 400, statusText: 'Bad Request' });
    });
  });
});
