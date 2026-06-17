import { Component, OnInit } from '@angular/core';
import { ConsultationService, ConsultationResponse, ConsultationRequest } from '../../../core/services/consultation.service';

@Component({
  selector: 'app-consultations',
  templateUrl: './consultations.component.html',
  styleUrls: ['./consultations.component.scss']
})
export class ConsultationsComponent implements OnInit {
  consultations: ConsultationResponse[] = [];
  selectedConsultation: ConsultationResponse | null = null;
  editingConsultation: ConsultationRequest = {};
  loading = false;
  editing = false;
  filterStatus = '';

  constructor(private consultationService: ConsultationService) {}

  ngOnInit(): void {
    this.loadConsultations();
  }

  loadConsultations(): void {
    this.loading = true;
    this.consultationService.getAllConsultations(this.filterStatus || undefined).subscribe({
      next: (data) => {
        this.consultations = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  filterByStatus(status: string): void {
    this.filterStatus = status;
    this.loadConsultations();
  }

  selectConsultation(c: ConsultationResponse): void {
    this.selectedConsultation = c;
    this.editing = false;
  }

  backToList(): void {
    this.selectedConsultation = null;
    this.editing = false;
  }

  startEdit(c: ConsultationResponse): void {
    this.selectedConsultation = c;
    this.editingConsultation = {
      patientId: c.patientId,
      appointmentId: c.appointmentId,
      type: c.type,
      reason: c.reason,
      diagnosis: c.diagnosis,
      observations: c.observations,
      bloodPressure: c.bloodPressure,
      pulse: c.pulse,
      temperature: c.temperature,
      weight: c.weight,
      height: c.height,
      requestedExams: c.requestedExams,
      followUpDate: c.followUpDate
    };
    this.editing = true;
  }

  cancelEdit(): void {
    this.editing = false;
  }

  saveDraft(): void {
    if (!this.selectedConsultation) return;
    this.consultationService.updateConsultation(this.selectedConsultation.id, this.editingConsultation).subscribe({
      next: (updated) => {
        this.selectedConsultation = updated;
        this.editing = false;
        this.loadConsultations();
      }
    });
  }

  completeConsultation(): void {
    if (!this.selectedConsultation) return;
    this.consultationService.completeConsultation(this.selectedConsultation.id, this.editingConsultation).subscribe({
      next: (updated) => {
        this.selectedConsultation = updated;
        this.editing = false;
        this.loadConsultations();
      }
    });
  }

  cancelConsultation(c: ConsultationResponse): void {
    this.consultationService.cancelConsultation(c.id).subscribe({
      next: () => this.loadConsultations()
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'badge-warning';
      case 'IN_PROGRESS': return 'badge-info';
      case 'COMPLETED': return 'badge-success';
      case 'CANCELLED': return 'badge-danger';
      default: return '';
    }
  }
}
