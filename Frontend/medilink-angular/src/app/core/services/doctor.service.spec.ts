import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DoctorService, Doctor, DoctorProfile, DoctorWithProfile } from './doctor.service';

describe('DoctorService', () => {
  let service: DoctorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(DoctorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllDoctors()', () => {
    it('should GET /api/auth/doctors and return doctors', () => {
      const doctors: Doctor[] = [
        { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@test.com', phone: '123', specialty: 'Cardio', hospital: 'Hospital A', licenseNumber: 'LIC-001' },
        { id: 2, firstName: 'Jane', lastName: 'Doe', email: 'jane@test.com', phone: '456', specialty: 'Neuro', hospital: 'Hospital B', licenseNumber: 'LIC-002' }
      ];

      service.getAllDoctors().subscribe(res => {
        expect(res).toEqual(doctors);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/auth/doctors');
      expect(req.request.method).toBe('GET');
      req.flush(doctors);
    });
  });

  describe('getAllDoctorProfiles()', () => {
    it('should GET /api/doctors/all and return profiles', () => {
      const profiles: DoctorProfile[] = [
        { userId: 1, available: true, biography: 'Bio', fee: 50, debutMatin: '08:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00' },
        { userId: 2, available: false, biography: 'Bio2', fee: 60, debutMatin: '09:00', finMatin: '14:00', debutApresMidi: '16:00', finApresMidi: '20:00' }
      ];

      service.getAllDoctorProfiles().subscribe(res => {
        expect(res).toEqual(profiles);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/doctors/all');
      expect(req.request.method).toBe('GET');
      req.flush(profiles);
    });
  });

  describe('getDoctorsWithProfiles()', () => {
    it('should combine doctors with profiles via forkJoin and merge profile data', () => {
      const doctors: Doctor[] = [
        { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@test.com', phone: '123', specialty: 'Cardio', hospital: 'Hospital A', licenseNumber: 'LIC-001' },
        { id: 3, firstName: 'Alice', lastName: 'Smith', email: 'alice@test.com', phone: '789', specialty: 'Derma', hospital: 'Hospital C', licenseNumber: 'LIC-003' }
      ];

      const profiles: DoctorProfile[] = [
        { userId: 1, available: true, biography: 'Profile bio', fee: 100, debutMatin: '08:00', finMatin: '12:00', debutApresMidi: '14:00', finApresMidi: '18:00' },
        { userId: 2, available: false, biography: 'Other', fee: 50, debutMatin: '09:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00' }
      ];

      const expected: DoctorWithProfile[] = [
        { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@test.com', phone: '123', specialty: 'Cardio', hospital: 'Hospital A', licenseNumber: 'LIC-001', available: true, biography: 'Profile bio', fee: 100, debutMatin: '08:00', finMatin: '12:00', debutApresMidi: '14:00', finApresMidi: '18:00' },
        { id: 3, firstName: 'Alice', lastName: 'Smith', email: 'alice@test.com', phone: '789', specialty: 'Derma', hospital: 'Hospital C', licenseNumber: 'LIC-003', available: true, biography: 'Aucune biographie renseignée', fee: 0, debutMatin: '08:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00' }
      ];

      service.getDoctorsWithProfiles().subscribe(res => {
        expect(res).toEqual(expected);
      });

      const reqDoctors = httpMock.expectOne('http://localhost:8765/api/auth/doctors');
      const reqProfiles = httpMock.expectOne('http://localhost:8765/api/doctors/all');

      expect(reqDoctors.request.method).toBe('GET');
      expect(reqProfiles.request.method).toBe('GET');

      reqDoctors.flush(doctors);
      reqProfiles.flush(profiles);
    });
  });
});
