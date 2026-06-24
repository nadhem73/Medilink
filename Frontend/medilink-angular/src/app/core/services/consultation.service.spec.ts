import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ConsultationService, ConsultationRequest, ConsultationResponse } from './consultation.service';

describe('ConsultationService', () => {
  let service: ConsultationService;
  let httpMock: HttpTestingController;
  const API_URL = 'http://localhost:8765/api/doctors/consultations';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ConsultationService]
    });
    service = TestBed.inject(ConsultationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch today consultations', () => {
    const mockResponse: ConsultationResponse[] = [
      { id: 1, patientId: 10, doctorId: 1, startTime: '2026-06-17T10:00:00', status: 'PENDING', type: 'PRESENTIEL', createdAt: '2026-06-17T09:00:00' }
    ];

    service.getTodayConsultations().subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${API_URL}/today`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should fetch all consultations', () => {
    const mockResponse: ConsultationResponse[] = [
      { id: 1, patientId: 10, doctorId: 1, startTime: '2026-06-17T10:00:00', status: 'PENDING', type: 'PRESENTIEL', createdAt: '2026-06-17T09:00:00' }
    ];

    service.getAllConsultations().subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should fetch consultations with status filter', () => {
    service.getAllConsultations('IN_PROGRESS').subscribe();

    const req = httpMock.expectOne(`${API_URL}?status=IN_PROGRESS`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should fetch a single consultation by id', () => {
    const mockResponse: ConsultationResponse = { id: 1, patientId: 10, doctorId: 1, startTime: '2026-06-17T10:00:00', status: 'PENDING', type: 'PRESENTIEL', createdAt: '2026-06-17T09:00:00' };

    service.getConsultation(1).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${API_URL}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should start a consultation', () => {
    const request: ConsultationRequest = { patientId: 10, appointmentId: 100, type: 'PRESENTIEL', reason: 'Check-up' };
    const mockResponse: ConsultationResponse = { id: 1, patientId: 10, doctorId: 1, startTime: '2026-06-17T10:00:00', status: 'IN_PROGRESS', type: 'PRESENTIEL', createdAt: '2026-06-17T09:00:00' };

    service.startConsultation(request).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should update a consultation', () => {
    const request: ConsultationRequest = { diagnosis: 'Migraine' };
    const mockResponse: ConsultationResponse = { id: 1, patientId: 10, doctorId: 1, startTime: '2026-06-17T10:00:00', status: 'IN_PROGRESS', type: 'PRESENTIEL', diagnosis: 'Migraine', createdAt: '2026-06-17T09:00:00' };

    service.updateConsultation(1, request).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${API_URL}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should complete a consultation', () => {
    const request: ConsultationRequest = { diagnosis: 'Final diagnosis' };
    const mockResponse: ConsultationResponse = { id: 1, patientId: 10, doctorId: 1, startTime: '2026-06-17T10:00:00', endTime: '2026-06-17T10:30:00', status: 'COMPLETED', type: 'PRESENTIEL', diagnosis: 'Final diagnosis', createdAt: '2026-06-17T09:00:00' };

    service.completeConsultation(1, request).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${API_URL}/1/complete`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should fetch consultations by patient', () => {
    const mockResponse: ConsultationResponse[] = [
      { id: 1, patientId: 10, doctorId: 1, startTime: '2026-06-17T10:00:00', status: 'COMPLETED', type: 'PRESENTIEL', diagnosis: 'Migraine', createdAt: '2026-06-17T09:00:00' },
      { id: 2, patientId: 10, doctorId: 1, startTime: '2026-06-18T14:00:00', status: 'PENDING', type: 'TELECONSULTATION', createdAt: '2026-06-18T13:00:00' }
    ];

    service.getPatientConsultations(10).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${API_URL}/patient/10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should cancel a consultation', () => {
    service.cancelConsultation(1).subscribe();

    const req = httpMock.expectOne(`${API_URL}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
