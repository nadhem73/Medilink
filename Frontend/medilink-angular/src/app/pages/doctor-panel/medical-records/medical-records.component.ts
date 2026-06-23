import { Component, OnInit } from '@angular/core';
import { AuthService, PatientListDto } from '../../../core/services/auth.service';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';

@Component({
  selector: 'app-doctor-medical-records',
  templateUrl: './medical-records.component.html',
  styleUrls: ['./medical-records.component.scss']
})
export class MedicalRecordsComponent implements OnInit {
  patients: PatientListDto[] = [];
  selectedPatient: PatientListDto | null = null;
  medicalRecord: MedicalRecord | null = null;
  loadingList = false;
  loadingRecord = false;
  error = '';
  searchQuery = '';

  constructor(
    private authService: AuthService,
    private patientService: PatientService
  ) {}

  ngOnInit(): void {
    this.loadPatients();
  }

  get filteredPatients(): PatientListDto[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return this.patients;
    return this.patients.filter(p =>
      `${p.firstName} ${p.lastName}`.toLowerCase().includes(q) ||
      (p.email && p.email.toLowerCase().includes(q)) ||
      (p.phone && p.phone.includes(q))
    );
  }

  loadPatients(): void {
    this.loadingList = true;
    this.authService.getAllPatients().subscribe({
      next: (data) => {
        this.patients = data;
        this.loadingList = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement de la liste des patients.';
        this.loadingList = false;
      }
    });
  }

  selectPatient(patient: PatientListDto): void {
    if (this.selectedPatient?.id === patient.id) return;
    this.selectedPatient = patient;
    this.medicalRecord = null;
    this.error = '';
    this.loadingRecord = true;

    this.patientService.getPatientMedicalRecord(patient.id).subscribe({
      next: (record) => {
        this.medicalRecord = record;
        this.loadingRecord = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement du dossier médical.';
        this.loadingRecord = false;
      }
    });
  }

  closeDetail(): void {
    this.selectedPatient = null;
    this.medicalRecord = null;
  }
}
