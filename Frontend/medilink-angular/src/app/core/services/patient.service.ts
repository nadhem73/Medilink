import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/** Dossier médical du patient renvoyé par le patient-service. */
export interface MedicalRecord {
  userId: number;
  bloodGroup?: string;
  height?: number;
  weight?: number;
  allergies?: string;
  chronicDiseases?: string;
  currentTreatments?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  insuranceCompany?: string;
  insuranceNumber?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private readonly API_URL = 'http://localhost:8765/api/patients';

  constructor(private http: HttpClient) {}

  /** Dossier médical de l'utilisateur connecté (token JWT ajouté par l'intercepteur). */
  getMyMedicalRecord(): Observable<MedicalRecord> {
    return this.http.get<MedicalRecord>(`${this.API_URL}/me/medical-record`);
  }

  /** Dossier médical d'un patient spécifique (accessible par un médecin). */
  getPatientMedicalRecord(userId: number): Observable<MedicalRecord> {
    return this.http.get<MedicalRecord>(`${this.API_URL}/${userId}/medical-record`);
  }
}
