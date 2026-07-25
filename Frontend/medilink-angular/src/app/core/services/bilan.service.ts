import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface BilanResultDto {
  id: number;
  testName: string;
  valeur: string;
  unite: string;
  referenceMin: number | null;
  referenceMax: number | null;
  referenceText: string | null;
  valeurAncienne: string | null;
  dateAncienne: string | null;
  status: string;
  confiance?: string;
}

export interface ScanBilanResponse {
  id: string;
  typeBilan: string;
  format: string;
  dateBilan: string;
  laboratoire: string;
  status: string;
  reviewStatus: string;
  patientId: number;
  doctorId: number;
  resultats: BilanResultDto[];
}

export interface BilanSummary {
  id: string;
  typeBilan: string;
  dateBilan: string;
  status: string;
  resultCount: number;
  abnormalCount: number;
  reviewStatus: string;
  patientId: number;
  doctorId: number;
}

export interface UpdateReviewStatusRequest {
  reviewStatus: string;
}

@Injectable({
  providedIn: 'root'
})
export class BilanService {
  private readonly API_URL = `${environment.apiBaseUrl}/bilans`;

  constructor(private http: HttpClient) {}

  getBilans(page: number = 0, size: number = 20): Observable<{ content: BilanSummary[] }> {
    return this.http.get<{ content: BilanSummary[] }>(this.API_URL, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  getBilan(id: string): Observable<ScanBilanResponse> {
    return this.http.get<ScanBilanResponse>(`${this.API_URL}/${id}`);
  }

  deleteBilan(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getDoctorBilans(page: number = 0, size: number = 50): Observable<{ content: BilanSummary[] }> {
    return this.http.get<{ content: BilanSummary[] }>(`${this.API_URL}/doctor`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  getBilanForDoctor(id: string): Observable<ScanBilanResponse> {
    return this.http.get<ScanBilanResponse>(`${this.API_URL}/doctor/${id}`);
  }

  updateReviewStatus(id: string, reviewStatus: string): Observable<ScanBilanResponse> {
    return this.http.patch<ScanBilanResponse>(`${this.API_URL}/doctor/${id}/review`, { reviewStatus });
  }
}
