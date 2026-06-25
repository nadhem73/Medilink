import { Component, OnInit } from '@angular/core';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-patient-medical-records',
  templateUrl: './medical-records.component.html',
  styleUrls: ['./medical-records.component.scss']
})
export class MedicalRecordsComponent implements OnInit {
  loading = true;
  currentUser: any;

  record: MedicalRecord | null = null;

  bloodGroup = 'Non renseigne';
  height: number | null = null;
  weight: number | null = null;
  bmi: number | null = null;

  allergies: string[] = [];
  chronicDiseases: string[] = [];

  treatments: { name: string; detail: string; status: string }[] = [];

  emergencyName = '';
  emergencyPhone = '';
  assurance: { label: string; value: string }[] = [];

  errorMessage = '';

  constructor(
    private patientService: PatientService,
    private authService: AuthService
  ) {
    this.currentUser = this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    this.patientService.getMyMedicalRecord().subscribe({
      next: (record) => {
        this.applyRecord(record);
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Impossible de charger le dossier medical. Veuillez reessayer plus tard.';
        console.error('Erreur chargement dossier medical', err);
        this.loading = false;
      }
    });
  }

  get vitals(): { label: string; value: string; sub?: string }[] {
    return [
      { label: 'Groupe sanguin', value: this.bloodGroup },
      { label: 'Taille', value: this.height ? `${this.height} cm` : '—' },
      { label: 'Poids', value: this.weight ? `${this.weight} kg` : '—' },
      { label: 'IMC', value: this.bmi !== null ? `${this.bmi}` : '—', sub: this.bmiCategory },
    ];
  }

  get hasAllergies(): boolean {
    return this.allergies.length > 0;
  }

  get hasChronicDiseases(): boolean {
    return this.chronicDiseases.length > 0;
  }

  private applyRecord(record: MedicalRecord): void {
    this.record = record;

    this.bloodGroup = record.bloodGroup || 'Non renseigne';
    this.height = record.height ?? null;
    this.weight = record.weight ?? null;
    this.bmi = this.computeBmi(record.height, record.weight);

    this.allergies = this.splitList(record.allergies);
    this.chronicDiseases = this.splitList(record.chronicDiseases);

    this.emergencyName = record.emergencyContactName || '';
    this.emergencyPhone = record.emergencyContactPhone || '';

    this.assurance = [
      { label: 'Compagnie', value: record.insuranceCompany || 'Non renseignee' },
      { label: "Numero d'assure", value: record.insuranceNumber || 'Non renseigne' }
    ];

    if (record.currentTreatments && record.currentTreatments.trim()) {
      this.treatments = record.currentTreatments
        .split(/[,\n;]/)
        .map(t => t.trim())
        .filter(Boolean)
        .map(t => ({ name: t, detail: 'Prescrit par votre medecin', status: 'En cours' }));
    }
  }

  private computeBmi(height?: number, weight?: number): number | null {
    if (!height || !weight) return null;
    const meters = height / 100;
    return Math.round((weight / (meters * meters)) * 10) / 10;
  }

  get bmiCategory(): string {
    if (this.bmi === null) return '';
    if (this.bmi < 18.5) return 'Insuffisance ponderale';
    if (this.bmi < 25) return 'Corpulence normale';
    if (this.bmi < 30) return 'Surpoids';
    return 'Obesite';
  }

  private splitList(value?: string): string[] {
    if (!value || !value.trim()) return [];
    return value.split(/[,\n;]/).map(v => v.trim()).filter(Boolean);
  }

  get patientName(): string {
    if (this.currentUser) {
      return `${this.currentUser.firstName || ''} ${this.currentUser.lastName || ''}`.trim();
    }
    return '';
  }

  get initials(): string {
    if (!this.currentUser) return 'PT';
    const first = (this.currentUser.firstName || '').charAt(0);
    const last = (this.currentUser.lastName || '').charAt(0);
    return `${first}${last}`.toUpperCase() || 'PT';
  }

  get hasEmergencyContact(): boolean {
    return !!(this.emergencyName || this.emergencyPhone);
  }

  getVitalIcon(label: string): string {
    const map: Record<string, string> = {
      'Groupe sanguin': 'blood',
      'Taille': 'height',
      'Poids': 'weight',
      'IMC': 'bmi'
    };
    return map[label] || 'blood';
  }
}
