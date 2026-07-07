import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MedicationDto {
  id: number;
  name: string;
  dosage: string;
  forme: string;
  presentation: string;
  price: number;
  remboursement: number;
  dci: string;
  type: string;
  prescriptionRequired: boolean;
  stockTotal: number;
  voieAdministration: string[];
}

export interface PrescriptionItemRequest {
  medicamentId: number;
  medicamentName: string;
  dosage: string;
  forme: string;
  posologie: string;
  dureeTraitement: number;
  voieAdministration: string;
  instructions: string;
}

export interface PrescriptionCreateRequest {
  consultationId: number;
  patientId: number;
  notes: string;
  items: PrescriptionItemRequest[];
}

export interface PrescriptionItemResponse {
  id: number;
  medicamentId: number;
  medicamentName: string;
  dosage: string;
  forme: string;
  posologie: string;
  dureeTraitement: number;
  voieAdministration: string;
  instructions: string;
  quantitePrescrite: number;
}

export interface PrescriptionResponse {
  id: number;
  consultationId: number;
  patientId: number;
  doctorId: number;
  pharmacieId: number;
  status: string;
  notes: string;
  items: PrescriptionItemResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface PrescriptionEmailRequest {
  patientEmail: string;
  patientName: string;
  pdfMedicationsBase64?: string;
  pdfAnalysesBase64?: string;
  medicationsFileName?: string;
  analysesFileName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PrescriptionService {
  private readonly PHARMACY_API = 'http://localhost:8765/api/pharmacy';
  private readonly PRESCRIPTION_API = 'http://localhost:8765/api/prescriptions';
  private readonly AUTH_API = 'http://localhost:8765/api/auth';

  constructor(private http: HttpClient) {}

  searchMedicaments(name: string, page: number = 0): Observable<any> {
    return this.http.get(`${this.PHARMACY_API}/medicaments/search`, {
      params: { name, page: page.toString(), size: '20' }
    });
  }

  getMedicament(id: number): Observable<MedicationDto> {
    return this.http.get<MedicationDto>(`${this.PHARMACY_API}/medicaments/${id}`);
  }

  checkStock(medicamentIds: number[]): Observable<Record<number, number>> {
    return this.http.post<Record<number, number>>(`${this.PHARMACY_API}/medicaments/stock-check`, medicamentIds);
  }

  createPrescription(request: PrescriptionCreateRequest): Observable<PrescriptionResponse> {
    return this.http.post<PrescriptionResponse>(this.PRESCRIPTION_API, request);
  }

  getPrescription(id: number): Observable<PrescriptionResponse> {
    return this.http.get<PrescriptionResponse>(`${this.PRESCRIPTION_API}/${id}`);
  }

  getPrescriptionByConsultation(consultationId: number): Observable<PrescriptionResponse> {
    return this.http.get<PrescriptionResponse>(`${this.PRESCRIPTION_API}/consultation/${consultationId}`);
  }

  getPrescriptionsByPatient(patientId: number): Observable<PrescriptionResponse[]> {
    return this.http.get<PrescriptionResponse[]>(`${this.PRESCRIPTION_API}/patient/${patientId}`);
  }

  cancelPrescription(id: number): Observable<void> {
    return this.http.delete<void>(`${this.PRESCRIPTION_API}/${id}`);
  }

  sendPrescriptionEmail(request: PrescriptionEmailRequest): Observable<any> {
    return this.http.post(`${this.AUTH_API}/email/prescriptions`, request);
  }
}
