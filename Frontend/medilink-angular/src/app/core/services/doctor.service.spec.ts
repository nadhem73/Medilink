import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DoctorService, Doctor, DoctorProfile, DoctorWithProfile } from './doctor.service';

describe('DoctorService', () => {
  let service: DoctorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DoctorService]
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

  it('should get all doctors', () => {
    const mock: Doctor[] = [{ id: 1, firstName: 'Ali', lastName: 'Ben', email: 'ali@test.com', phone: '123', specialty: 'Cardio', hospital: 'Hopital', licenseNumber: '12345' }];

    service.getAllDoctors().subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne('http://localhost:8765/api/auth/doctors');
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('should get all doctor profiles', () => {
    const mock: DoctorProfile[] = [{ userId: 1, available: true, debutMatin: '08:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00' }];

    service.getAllDoctorProfiles().subscribe(res => expect(res).toEqual(mock));
    const req = httpMock.expectOne('http://localhost:8765/api/doctors/all');
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('should merge doctors with profiles', () => {
    const doctors: Doctor[] = [{ id: 1, firstName: 'Ali', lastName: 'Ben', email: 'ali@test.com', phone: '123', specialty: 'Cardio', hospital: 'Hopital', licenseNumber: '12345' }];
    const profiles: DoctorProfile[] = [{ userId: 1, available: true, biography: 'Expert', fee: 80, debutMatin: '08:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00' }];

    service.getDoctorsWithProfiles().subscribe((res: DoctorWithProfile[]) => {
      expect(res.length).toBe(1);
      expect(res[0].available).toBeTrue();
      expect(res[0].biography).toBe('Expert');
      expect(res[0].fee).toBe(80);
    });

    const reqDoctors = httpMock.expectOne('http://localhost:8765/api/auth/doctors');
    const reqProfiles = httpMock.expectOne('http://localhost:8765/api/doctors/all');
    expect(reqDoctors.request.method).toBe('GET');
    expect(reqProfiles.request.method).toBe('GET');
    reqDoctors.flush(doctors);
    reqProfiles.flush(profiles);
  });

  it('should use defaults when profile is missing', () => {
    const doctors: Doctor[] = [{ id: 1, firstName: 'Ali', lastName: 'Ben', email: 'ali@test.com', phone: '123', specialty: 'Cardio', hospital: 'Hopital', licenseNumber: '12345' }];

    service.getDoctorsWithProfiles().subscribe((res: DoctorWithProfile[]) => {
      expect(res[0].available).toBeTrue();
      expect(res[0].biography).toBe('Aucune biographie renseignée');
      expect(res[0].fee).toBe(0);
      expect(res[0].debutMatin).toBe('08:00');
    });

    const reqDoctors = httpMock.expectOne('http://localhost:8765/api/auth/doctors');
    const reqProfiles = httpMock.expectOne('http://localhost:8765/api/doctors/all');
    reqDoctors.flush(doctors);
    reqProfiles.flush([]);
  });
});
