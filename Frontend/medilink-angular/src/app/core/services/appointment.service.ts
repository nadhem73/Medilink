import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AppointmentRequest {
  doctorId: number;
  dateTime: string; // Format ISO: 'YYYY-MM-DDTHH:mm:ss'
  mode: string; // PRESENTIEL / TELECONSULTATION
  notes?: string;
}

export interface AppointmentDto {
  id: number;
  patientId: number;
  doctorId: number;
  dateTime: string;
  status: string; // PENDING / CONFIRMED / CANCELLED
  mode: string; // PRESENTIEL / TELECONSULTATION
  notes?: string;
  createdAt: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private readonly API_URL = 'http://localhost:8765/api/patients/appointments';

  constructor(private http: HttpClient) {}

  createAppointment(request: AppointmentRequest): Observable<AppointmentDto> {
    return this.http.post<AppointmentDto>(this.API_URL, request);
  }

  getMyAppointments(): Observable<AppointmentDto[]> {
    return this.http.get<AppointmentDto[]>(this.API_URL);
  }

  getDoctorAppointments(): Observable<AppointmentDto[]> {
    return this.http.get<AppointmentDto[]>(`${this.API_URL}/doctor`);
  }

  cancelAppointment(id: number): Observable<AppointmentDto> {
    return this.http.put<AppointmentDto>(`${this.API_URL}/${id}/cancel`, {});
  }

  /** Confirme un rendez-vous (action médecin) */
  confirmAppointment(id: number): Observable<AppointmentDto> {
    return this.http.put<AppointmentDto>(`${this.API_URL}/${id}/confirm`, {});
  }

  /** Annule un rendez-vous depuis le panel médecin */
  cancelByDoctor(id: number): Observable<AppointmentDto> {
    return this.http.put<AppointmentDto>(`${this.API_URL}/${id}/doctor-cancel`, {});
  }
}
