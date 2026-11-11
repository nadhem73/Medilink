import { Component, OnInit } from '@angular/core';
import { DoctorService, Doctor } from '../../../core/services/doctor.service';
import { ConsultationService, ConsultationResponse } from '../../../core/services/consultation.service';
import { PrescriptionService, PrescriptionItemResponse } from '../../../core/services/prescription.service';
import { AuthService, PatientListDto } from '../../../core/services/auth.service';

@Component({
  selector: 'app-compare-doctor',
  templateUrl: './compare-doctor.component.html',
  styleUrls: ['./compare-doctor.component.scss']
})
export class CompareDoctorComponent implements OnInit {
  doctors: Doctor[] = [];
  patients: PatientListDto[] = [];
  selectedDoctor: Doctor | null = null;
  lastConsultation: ConsultationResponse | null = null;
  prevConsultation: ConsultationResponse | null = null;
  prescriptionItemsMap: Map<number, PrescriptionItemResponse[]> = new Map();
  loading = false;

  constructor(
    private doctorService: DoctorService,
    private consultationService: ConsultationService,
    private prescriptionService: PrescriptionService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadDoctors();
    this.loadPatients();
  }

  loadDoctors(): void {
    this.doctorService.getAllDoctors().subscribe(docs => {
      this.doctors = docs;
    });
  }

  loadPatients(): void {
    this.authService.getAllPatients().subscribe(pts => {
      this.patients = pts;
    });
  }

  onDoctorSelect(doctorId: number): void {
    this.selectedDoctor = this.doctors.find(d => d.id === doctorId) || null;
    this.lastConsultation = null;
    this.prevConsultation = null;
    this.prescriptionItemsMap.clear();

    if (!doctorId) return;

    this.loading = true;
    this.consultationService.getConsultationsByDoctorId(doctorId).subscribe({
      next: (consultations) => {
        if (consultations.length >= 1) {
          this.lastConsultation = consultations[0];
        }
        if (consultations.length >= 2) {
          this.prevConsultation = consultations[1];
        }

        const ids = consultations.slice(0, 2).map(c => c.id);
        ids.forEach(id => {
          this.prescriptionService.getPrescriptionByConsultation(id).subscribe({
            next: (prescription) => {
              if (prescription?.items?.length) {
                this.prescriptionItemsMap.set(id, prescription.items);
              }
            }
          });
        });

        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  getPatientName(patientId: number): string {
    const p = this.patients.find(pt => pt.id === patientId);
    return p ? `${p.firstName} ${p.lastName}` : `Patient #${patientId}`;
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'Terminé';
      case 'IN_PROGRESS': return 'En cours';
      case 'PENDING': return 'En attente';
      case 'CANCELLED': return 'Annulé';
      default: return status;
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'status-completed';
      case 'IN_PROGRESS': return 'status-progress';
      case 'PENDING': return 'status-pending';
      case 'CANCELLED': return 'status-cancelled';
      default: return '';
    }
  }

  getTypeLabel(type: string): string {
    return type === 'TELECONSULTATION' ? 'Téléconsultation' : 'Présentiel';
  }
}
