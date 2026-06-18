import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

export interface Doctor {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  specialty: string;
  hospital: string;
  licenseNumber: string;
}

export interface DoctorProfile {
  userId: number;
  available: boolean;
  biography?: string;
  fee?: number;
  debutMatin: string;
  finMatin: string;
  debutApresMidi: string;
  finApresMidi: string;
}

export interface DoctorWithProfile extends Doctor {
  available: boolean;
  biography?: string;
  fee?: number;
  debutMatin: string;
  finMatin: string;
  debutApresMidi: string;
  finApresMidi: string;
}

@Injectable({
  providedIn: 'root'
})
export class DoctorService {
  private readonly AUTH_API_URL = 'http://localhost:8765/api/auth';
  private readonly DOCTOR_API_URL = 'http://localhost:8765/api/doctors';

  constructor(private http: HttpClient) {}

  getAllDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.AUTH_API_URL}/doctors`);
  }

  getAllDoctorProfiles(): Observable<DoctorProfile[]> {
    return this.http.get<DoctorProfile[]>(`${this.DOCTOR_API_URL}/all`);
  }

  getDoctorProfileById(doctorId: number): Observable<DoctorProfile | undefined> {
    return this.getAllDoctorProfiles().pipe(
      map(profiles => profiles.find(p => p.userId === doctorId))
    );
  }

  getDoctorsWithProfiles(): Observable<DoctorWithProfile[]> {
    return forkJoin({
      doctors: this.getAllDoctors(),
      profiles: this.getAllDoctorProfiles()
    }).pipe(
      map(({ doctors, profiles }) => {
        const profileMap = new Map<number, DoctorProfile>();
        profiles.forEach(p => profileMap.set(p.userId, p));

        return doctors.map(doctor => {
          const profile = profileMap.get(doctor.id);
          return {
            ...doctor,
            available: profile ? profile.available : true,
            biography: profile ? profile.biography : 'Aucune biographie renseignée',
            fee: profile ? profile.fee : 0,
            debutMatin: profile ? profile.debutMatin : '08:00',
            finMatin: profile ? profile.finMatin : '13:00',
            debutApresMidi: profile ? profile.debutApresMidi : '15:00',
            finApresMidi: profile ? profile.finApresMidi : '19:00'
          };
        });
      })
    );
  }
}
