import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { PrescriptionService, PrescriptionResponse, PickupCodeResponse } from '../../../core/services/prescription.service';

@Component({
  selector: 'app-pharmacy-prescriptions',
  templateUrl: './pharmacy-prescriptions.component.html',
  styleUrls: ['./pharmacy-prescriptions.component.scss']
})
export class PharmacyPrescriptionsComponent implements OnInit {
  allPrescriptions: PrescriptionResponse[] = [];
  loading = false;
  selectedPrescription: PrescriptionResponse | null = null;
  pickupCode: string | null = null;
  pickupCodeInput = '';
  pickupCodeError = '';
  pickupCodeSuccess = '';
  actionError = '';

  searchQuery = '';
  statusFilter = '';
  sortOrder: 'desc' | 'asc' = 'desc';

  private patientMap = new Map<number, string>();
  private doctorMap = new Map<number, string>();
  private pharmacieId: number | null = null;

  constructor(
    private authService: AuthService,
    private doctorService: DoctorService,
    private prescriptionService: PrescriptionService
  ) {}

  readonly statuses = [
    { value: '', label: 'Toutes' },
    { value: 'SOUMISE', label: 'Soumise' },
    { value: 'EN_PREPARATION', label: 'En préparation' },
    { value: 'PREPAREE', label: 'Préparée' },
    { value: 'RETIREE', label: 'Retirée' },
    { value: 'DISPENSEE', label: 'Dispensée' },
    { value: 'ANNULEE', label: 'Annulée' },
  ];

  setStatusFilter(value: string): void {
    this.statusFilter = this.statusFilter === value ? '' : value;
  }

  get filteredPrescriptions(): PrescriptionResponse[] {
    let list = this.allPrescriptions;

    if (this.statusFilter) {
      list = list.filter(p => p.status === this.statusFilter);
    }

    if (this.searchQuery.trim()) {
      const q = this.searchQuery.trim().toLowerCase();
      list = list.filter(p => {
        const name = (this.patientMap.get(p.patientId) || '').toLowerCase();
        const meds = p.items.map(i => i.medicamentName.toLowerCase());
        return name.includes(q) || meds.some(m => m.includes(q));
      });
    }

    return list.sort((a, b) => {
      const d = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      return this.sortOrder === 'desc' ? -d : d;
    });
  }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.pharmacieId = user?.pharmacieId ?? null;
    this.loadPrescriptions();
  }

  selectPrescription(p: PrescriptionResponse): void {
    this.selectedPrescription = p;
    this.actionError = '';
    this.pickupCode = null;
    this.pickupCodeInput = '';
    this.pickupCodeError = '';
    this.pickupCodeSuccess = '';
    if (p.status === 'PREPAREE') {
      this.loadPickupCode(p.id);
    }
  }

  closeDetail(): void {
    this.selectedPrescription = null;
    this.pickupCode = null;
    this.pickupCodeInput = '';
    this.pickupCodeError = '';
    this.pickupCodeSuccess = '';
  }

  getPatientName(id: number): string {
    return this.patientMap.get(id) || `Patient #${id}`;
  }

  getDoctorName(id: number): string {
    return this.doctorMap.get(id) || `Dr #${id}`;
  }

  formatDate(dateTimeStr: string): string {
    if (!dateTimeStr) return '';
    try {
      const date = new Date(dateTimeStr);
      const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'long', year: 'numeric' };
      const s = date.toLocaleDateString('fr-FR', opts);
      return s.charAt(0).toUpperCase() + s.slice(1);
    } catch {
      return dateTimeStr;
    }
  }

  formatHour(dateTimeStr: string): string {
    if (!dateTimeStr) return '';
    try {
      const date = new Date(dateTimeStr);
      return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    } catch {
      return '';
    }
  }

  getCardBorderColor(status: string): string {
    switch (status) {
      case 'SOUMISE': return '#6d28d9';
      case 'EN_PREPARATION': return '#b45309';
      case 'PREPAREE': return '#0e7490';
      case 'RETIREE': return '#15803d';
      case 'DISPENSEE': return '#1e7a45';
      case 'ANNULEE': return '#b91c1c';
      default: return 'transparent';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'SOUMISE': return 'Soumise';
      case 'EN_PREPARATION': return 'En préparation';
      case 'PREPAREE': return 'Préparée';
      case 'RETIREE': return 'Retirée';
      case 'DISPENSEE': return 'Dispensée';
      case 'ANNULEE': return 'Annulée';
      case 'BROUILLON': return 'Brouillon';
      default: return status;
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'SOUMISE': return 'status-soumise';
      case 'EN_PREPARATION': return 'status-en-preparation';
      case 'PREPAREE': return 'status-preparee';
      case 'RETIREE': return 'status-retiree';
      case 'DISPENSEE': return 'status-dispensee';
      case 'ANNULEE': return 'status-annulee';
      case 'BROUILLON': return 'status-brouillon';
      default: return '';
    }
  }

  get canStartPreparation(): boolean {
    return this.selectedPrescription?.status === 'SOUMISE';
  }

  get canMarkAsPrepared(): boolean {
    return this.selectedPrescription?.status === 'EN_PREPARATION';
  }

  get canDispense(): boolean {
    return this.selectedPrescription?.status === 'RETIREE';
  }

  get canValidatePickup(): boolean {
    return this.selectedPrescription?.status === 'PREPAREE'
      && !!this.pickupCode;
  }

  startPreparation(): void {
    if (!this.selectedPrescription) return;
    this.actionError = '';
    this.updateStatus(this.selectedPrescription.id, 'EN_PREPARATION');
  }

  markAsPrepared(): void {
    if (!this.selectedPrescription) return;
    this.actionError = '';
    this.updateStatus(this.selectedPrescription.id, 'PREPAREE');
  }

  dispense(): void {
    if (!this.selectedPrescription) return;
    this.actionError = '';
    this.updateStatus(this.selectedPrescription.id, 'DISPENSEE');
  }

  validatePickup(): void {
    if (!this.selectedPrescription || !this.pickupCodeInput.trim()) return;
    this.pickupCodeError = '';
    this.pickupCodeSuccess = '';
    this.prescriptionService.validatePickupCode(
      this.selectedPrescription.id,
      this.pickupCodeInput.trim()
    ).subscribe({
      next: (updated) => {
        this.pickupCodeInput = '';
        this.pickupCode = null;
        this.pickupCodeSuccess = 'Code validé. Ordonnance retirée.';
        const idx = this.allPrescriptions.findIndex(p => p.id === updated.id);
        if (idx !== -1) {
          this.allPrescriptions = [
            ...this.allPrescriptions.slice(0, idx),
            updated,
            ...this.allPrescriptions.slice(idx + 1)
          ];
        }
        this.selectedPrescription = updated;
      },
      error: () => {
        this.pickupCodeError = 'Code invalide. Veuillez réessayer.';
      }
    });
  }

  private updateStatus(id: number, newStatus: string): void {
    this.prescriptionService.updateStatus(id, newStatus).subscribe({
      next: (updated) => {
        this.actionError = '';
        const idx = this.allPrescriptions.findIndex(p => p.id === id);
        if (idx !== -1) {
          this.allPrescriptions = [
            ...this.allPrescriptions.slice(0, idx),
            updated,
            ...this.allPrescriptions.slice(idx + 1)
          ];
        }
        this.selectedPrescription = updated;
        if (newStatus === 'PREPAREE') {
          this.loadPickupCode(id);
        } else {
          this.pickupCode = null;
        }
      },
      error: (err) => {
        this.actionError = err.error?.error || 'Action impossible, veuillez réessayer.';
        this.fetchPrescriptions();
      }
    });
  }

  private loadPickupCode(id: number): void {
    this.prescriptionService.getPickupCode(id).subscribe({
      next: (res) => {
        this.pickupCode = res.code;
      }
    });
  }

  get voieOptions(): string[] {
    return ['Orale', 'Intraveineuse', 'Intramusculaire', 'Sous-cutanée', 'Topique', 'Inhalée', 'Rectale', 'Sublinguale'];
  }

  getMedicationNames(items: any[]): string {
    return items.map(i => i.medicamentName).join(', ');
  }

  voieLabel(voie: string | undefined): string {
    if (!voie) return '—';
    const map: Record<string, string> = {
      'ORALE': 'Orale',
      'IV': 'Intraveineuse',
      'IM': 'Intramusculaire',
      'SC': 'Sous-cutanée',
      'TOPIC': 'Topique',
      'INHALATION': 'Inhalée',
      'RECTALE': 'Rectale',
      'SUBLINGUALE': 'Sublinguale'
    };
    return map[voie.toUpperCase()] || voie;
  }

  private loadPrescriptions(): void {
    this.loading = true;
    this.authService.getAllPatients().subscribe({
      next: (patients) => {
        patients.forEach(p => this.patientMap.set(p.id, `${p.firstName} ${p.lastName}`));
        this.loadDoctors();
      },
      error: () => {
        this.loadDoctors();
      }
    });
  }

  private loadDoctors(): void {
    this.doctorService.getAllDoctors().subscribe({
      next: (doctors) => {
        doctors.forEach(d => this.doctorMap.set(d.id, `Dr ${d.firstName} ${d.lastName}`));
        this.fetchPrescriptions();
      },
      error: () => {
        this.fetchPrescriptions();
      }
    });
  }

  private fetchPrescriptions(): void {
    const fetch$ = this.pharmacieId
      ? this.prescriptionService.getPrescriptionsByPharmacy(this.pharmacieId)
      : this.prescriptionService.getAllPrescriptions();
    fetch$.subscribe({
      next: (data) => {
        this.allPrescriptions = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
