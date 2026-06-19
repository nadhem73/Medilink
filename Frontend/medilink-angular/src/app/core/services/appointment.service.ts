import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

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
  private readonly API_URL = `${environment.apiBaseUrl}/patients/appointments`;

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

  /** Retourne la liste des IDs des médecins chez qui le patient a déjà un rendez-vous actif */
  getActiveDoctorIds(): Observable<number[]> {
    return this.http.get<number[]>(`${this.API_URL}/active-doctor-ids`);
  }

  /** Retourne la liste des créneaux disponibles (HH:mm) pour un médecin à une date donnée */
  getAvailableSlots(doctorId: number, date: string, debutMatin: string, finMatin: string, debutApresMidi: string, finApresMidi: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/available-slots`, {
      params: { doctorId: doctorId.toString(), date, debutMatin, finMatin, debutApresMidi, finApresMidi }
    });
  }

  /** Vérifie si un créneau est disponible pour un médecin (consultation = 30 min) */
  checkAvailability(doctorId: number, dateTime: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/check-availability`, {
      params: { doctorId: doctorId.toString(), dateTime }
    });
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
