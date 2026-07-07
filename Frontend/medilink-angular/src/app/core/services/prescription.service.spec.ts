import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PrescriptionService, PrescriptionCreateRequest, PrescriptionEmailRequest } from './prescription.service';

describe('PrescriptionService', () => {
  let service: PrescriptionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PrescriptionService]
    });
    service = TestBed.inject(PrescriptionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should search medicaments', () => {
    const mockResponse = { content: [], totalElements: 0 };
    service.searchMedicaments('DOLIPRANE', 0).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(r =>
      r.url.includes('/medicaments/search') && r.params.get('name') === 'DOLIPRANE'
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    req.flush(mockResponse);
  });

  it('should get medicament by id', () => {
    const mockMedicament = { id: 1, name: 'DOLIPRANE', dosage: '500mg' };
    service.getMedicament(1).subscribe(res => {
      expect(res).toEqual(mockMedicament as any);
    });

    const req = httpMock.expectOne('http://localhost:8765/api/pharmacy/medicaments/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockMedicament);
  });

  it('should check stock', () => {
    const ids = [1, 2];
    const mockStock = { 1: 100, 2: 50 };
    service.checkStock(ids).subscribe(res => {
      expect(res).toEqual(mockStock);
    });

    const req = httpMock.expectOne('http://localhost:8765/api/pharmacy/medicaments/stock-check');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(ids);
    req.flush(mockStock);
  });

  it('should create prescription', () => {
    const request: PrescriptionCreateRequest = {
      consultationId: 100,
      patientId: 10,
      notes: 'Test',
      items: [{ medicamentId: 1, medicamentName: 'DOLIPRANE', dosage: '500mg', forme: 'Comprimé', posologie: '1x3/j', dureeTraitement: 7, voieAdministration: 'Orale', instructions: '' }]
    };
    const mockResponse = { id: 1, consultationId: 100, patientId: 10, doctorId: 1, status: 'SOUMISE', items: [], notes: 'Test' };

    service.createPrescription(request).subscribe(res => {
      expect(res).toEqual(mockResponse as any);
    });

    const req = httpMock.expectOne('http://localhost:8765/api/prescriptions');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should get prescription by id', () => {
    const mockResponse = { id: 1, status: 'SOUMISE' };
    service.getPrescription(1).subscribe(res => {
      expect(res).toEqual(mockResponse as any);
    });

    const req = httpMock.expectOne('http://localhost:8765/api/prescriptions/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get prescription by consultation', () => {
    const mockResponse = { id: 1, consultationId: 100 };
    service.getPrescriptionByConsultation(100).subscribe(res => {
      expect(res).toEqual(mockResponse as any);
    });

    const req = httpMock.expectOne('http://localhost:8765/api/prescriptions/consultation/100');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get prescriptions by patient', () => {
    const mockResponse = [{ id: 1 }, { id: 2 }];
    service.getPrescriptionsByPatient(10).subscribe(res => {
      expect(res).toEqual(mockResponse as any);
    });

    const req = httpMock.expectOne('http://localhost:8765/api/prescriptions/patient/10');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should cancel prescription', () => {
    service.cancelPrescription(1).subscribe(res => {
      expect(res).toBeNull();
    });

    const req = httpMock.expectOne('http://localhost:8765/api/prescriptions/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should send prescription email', () => {
    const request: PrescriptionEmailRequest = {
      patientEmail: 'test@test.com',
      patientName: 'John Doe',
      pdfMedicationsBase64: 'base64data'
    };

    service.sendPrescriptionEmail(request).subscribe(res => {
      expect(res).toEqual({ success: true });
    });

    const req = httpMock.expectOne('http://localhost:8765/api/auth/email/prescriptions');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ success: true });
  });

  it('should handle HTTP error gracefully', () => {
    service.getPrescription(999).subscribe({
      next: () => fail('should have failed'),
      error: (err) => {
        expect(err.status).toBe(404);
      }
    });

    const req = httpMock.expectOne('http://localhost:8765/api/prescriptions/999');
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
  });
});
