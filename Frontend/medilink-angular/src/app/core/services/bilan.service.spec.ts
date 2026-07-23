import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BilanService, BilanSummary, ScanBilanResponse } from './bilan.service';
import { environment } from '../../../environments/environment';

describe('BilanService', () => {
  let service: BilanService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiBaseUrl}/bilans`;

  const mockSummary: BilanSummary = {
    id: '550e8400-e29b-41d4-a716-446655440000',
    typeBilan: 'Bilan sanguin',
    dateBilan: '2026-06-15',
    status: 'PENDING',
    resultCount: 5,
    abnormalCount: 2,
    reviewStatus: 'EN_ATTENTE',
    patientId: 1,
    doctorId: 2
  };

  const mockBilan: ScanBilanResponse = {
    id: '550e8400-e29b-41d4-a716-446655440000',
    typeBilan: 'Bilan sanguin',
    format: 'NOUVEAU_PATIENT',
    dateBilan: '2026-06-15',
    laboratoire: 'Laboratoire Tunis',
    status: 'PENDING',
    reviewStatus: 'EN_ATTENTE',
    patientId: 1,
    doctorId: 2,
    resultats: [
      { id: 1, testName: 'Glycemie', valeur: '5.2', unite: 'mmol/L', referenceMin: 3.9, referenceMax: 6.1, referenceText: null, valeurAncienne: null, dateAncienne: null, status: 'NORMAL' }
    ]
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BilanService]
    });
    service = TestBed.inject(BilanService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should GET bilans with pagination', () => {
    const mockPage = { content: [mockSummary], totalPages: 1, totalElements: 1 };

    service.getBilans(0, 20).subscribe(res => {
      expect(res.content.length).toBe(1);
      expect(res.content[0].typeBilan).toBe('Bilan sanguin');
    });

    const req = httpMock.expectOne(`${apiUrl}?page=0&size=20`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should GET bilan by id', () => {
    service.getBilan('550e8400-e29b-41d4-a716-446655440000').subscribe(res => {
      expect(res.typeBilan).toBe('Bilan sanguin');
      expect(res.resultats.length).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/550e8400-e29b-41d4-a716-446655440000`);
    expect(req.request.method).toBe('GET');
    req.flush(mockBilan);
  });

  it('should DELETE bilan', () => {
    service.deleteBilan('550e8400-e29b-41d4-a716-446655440000').subscribe(res => {
      expect(res).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/550e8400-e29b-41d4-a716-446655440000`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should GET doctor bilans with pagination', () => {
    const mockPage = { content: [mockSummary], totalPages: 1, totalElements: 1 };

    service.getDoctorBilans(0, 50).subscribe(res => {
      expect(res.content.length).toBe(1);
      expect(res.content[0].reviewStatus).toBe('EN_ATTENTE');
    });

    const req = httpMock.expectOne(`${apiUrl}/doctor?page=0&size=50`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should GET bilan for doctor by id', () => {
    service.getBilanForDoctor('550e8400-e29b-41d4-a716-446655440000').subscribe(res => {
      expect(res.doctorId).toBe(2);
    });

    const req = httpMock.expectOne(`${apiUrl}/doctor/550e8400-e29b-41d4-a716-446655440000`);
    expect(req.request.method).toBe('GET');
    req.flush(mockBilan);
  });

  it('should PATCH review status', () => {
    const updatedBilan = { ...mockBilan, reviewStatus: 'LU' };

    service.updateReviewStatus('550e8400-e29b-41d4-a716-446655440000', 'LU').subscribe(res => {
      expect(res.reviewStatus).toBe('LU');
    });

    const req = httpMock.expectOne(`${apiUrl}/doctor/550e8400-e29b-41d4-a716-446655440000/review`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ reviewStatus: 'LU' });
    req.flush(updatedBilan);
  });

  it('should handle HTTP error gracefully', () => {
    service.getBilans(0, 20).subscribe({
      error: err => {
        expect(err.status).toBe(500);
      }
    });

    const req = httpMock.expectOne(`${apiUrl}?page=0&size=20`);
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
  });
});
