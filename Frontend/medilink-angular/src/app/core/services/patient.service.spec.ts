import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { PatientService, MedicalRecord } from './patient.service';

describe('PatientService', () => {
  let service: PatientService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
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

      service.getMyMedicalRecord().subscribe(record => {
        expect(record).toEqual(mockRecord);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/me/medical-record');
      expect(req.request.method).toBe('GET');
      req.flush(mockRecord);
    });

    it('should handle error response', () => {
      service.getMyMedicalRecord().subscribe({
        error: err => {
          expect(err.status).toBe(404);
        }
      });

      const req = httpMock.expectOne('http://localhost:8765/api/patients/me/medical-record');
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });
});
