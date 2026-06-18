import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ConsultationRequest {
  patientId?: number;
  appointmentId?: number;
  type?: string;
  reason?: string;
  diagnosis?: string;
  observations?: string;
  bloodPressure?: string;
  pulse?: number;
  temperature?: number;
  weight?: number;
  height?: number;
  requestedExams?: string;
  followUpDate?: string;
  followUpTime?: string;
}

export interface ConsultationResponse {
  id: number;
  patientId: number;
  doctorId: number;
  appointmentId?: number;
  startTime: string;
  endTime?: string;
  status: string;
  type: string;
  reason?: string;
  diagnosis?: string;
  observations?: string;
  bloodPressure?: string;
  pulse?: number;
  temperature?: number;
  weight?: number;
  height?: number;
  bmi?: number;
  requestedExams?: string;
  followUpDate?: string;
  prescriptionId?: number;
  createdAt: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConsultationService {
  private readonly API_URL = 'http://localhost:8765/api/doctors/consultations';

  constructor(private http: HttpClient) {}

  getTodayConsultations(): Observable<ConsultationResponse[]> {
    return this.http.get<ConsultationResponse[]>(`${this.API_URL}/today`);
  }

  getAllConsultations(status?: string): Observable<ConsultationResponse[]> {
    const params = status ? { status } : undefined;
    return this.http.get<ConsultationResponse[]>(this.API_URL, { params });
  }

  getConsultation(id: number): Observable<ConsultationResponse> {
    return this.http.get<ConsultationResponse>(`${this.API_URL}/${id}`);
  }

  startConsultation(request: ConsultationRequest): Observable<ConsultationResponse> {
    return this.http.post<ConsultationResponse>(this.API_URL, request);
  }

  updateConsultation(id: number, request: ConsultationRequest): Observable<ConsultationResponse> {
    return this.http.put<ConsultationResponse>(`${this.API_URL}/${id}`, request);
  }

  completeConsultation(id: number, request: ConsultationRequest): Observable<ConsultationResponse> {
    return this.http.put<ConsultationResponse>(`${this.API_URL}/${id}/complete`, request);
  }

  cancelConsultation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
