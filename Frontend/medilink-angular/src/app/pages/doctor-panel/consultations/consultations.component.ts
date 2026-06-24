import { Component, OnInit } from '@angular/core';
import { ConsultationService, ConsultationResponse, ConsultationRequest } from '../../../core/services/consultation.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';
import { DoctorService } from '../../../core/services/doctor.service';

export interface TodayAppointment {
  id: number;
  patientId: number;
  reason: string;
  hour: string;
  mode: string;
  type: string;
}

@Component({
  selector: 'app-consultations',
  templateUrl: './consultations.component.html',
  styleUrls: ['./consultations.component.scss']
})
export class ConsultationsComponent implements OnInit {
  consultations: ConsultationResponse[] = [];
  filteredConsultations: ConsultationResponse[] = [];
  todayAppointments: TodayAppointment[] = [];
  selectedConsultation: ConsultationResponse | null = null;
  editingConsultation: ConsultationRequest = {};
  loading = false;
  startingId: number | null = null;
  filterStatus = '';
  searchTerm = '';
  patientMedicalRecord: MedicalRecord | null = null;
  editingMedicalRecord: Partial<MedicalRecord> = {};
  medicalLoading = false;

  readonly analysisTypes: string[] = [
    'Hématologie', 'Biochimie', 'Hépatologie', 'Néphrologie',
    'Endocrinologie', 'Infectiologie', 'Coagulation', 'Urines',
    'Hormonologie', 'Cardiologie', 'Oncologie', 'Microbiologie'
  ];
  private patientMap = new Map<number, { firstName: string; lastName: string; gender?: string }>();

  get isCompleted(): boolean {
    return this.selectedConsultation?.status === 'COMPLETED';
  }
  loadingSlots = false;
  availableSlots: string[] = [];
  private workingHours = { debutMatin: '08:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00' };

  constructor(
    private consultationService: ConsultationService,
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private patientService: PatientService,
    private doctorService: DoctorService
  ) {}

  ngOnInit(): void {
    this.loadPatients();
    this.loadConsultations();
    this.loadDoctorWorkingHours();
  }

  private loadDoctorWorkingHours(): void {
    const doctorId = this.authService.getCurrentUser()?.id;
    if (!doctorId) return;
    this.doctorService.getDoctorProfileById(doctorId).subscribe({
      next: (profile) => {
        if (profile) {
          this.workingHours = {
            debutMatin: profile.debutMatin || '08:00',
            finMatin: profile.finMatin || '13:00',
            debutApresMidi: profile.debutApresMidi || '15:00',
            finApresMidi: profile.finApresMidi || '19:00'
          };
        }
      }
    });
  }

  private loadPatients(): void {
    this.authService.getAllPatients().subscribe({
      next: (patients) => {
        patients.forEach(p => this.patientMap.set(p.id, { firstName: p.firstName, lastName: p.lastName, gender: p.gender }));
      }
    });
  }

  getPatientName(patientId: number): string {
    const p = this.patientMap.get(patientId);
    return p ? `${p.firstName} ${p.lastName}` : `Patient #${patientId}`;
  }

  getPatientInitials(patientId: number): string {
    const p = this.patientMap.get(patientId);
    if (p) {
      return (p.firstName.charAt(0) + p.lastName.charAt(0)).toUpperCase();
    }
    return `#${patientId}`;
  }

  getPatientGender(patientId: number): string {
    return this.patientMap.get(patientId)?.gender || '';
  }

  hasAnalysis(type: string): boolean {
    const exams = this.editingConsultation.requestedExams || '';
    return exams.split(',').map(s => s.trim()).includes(type);
  }

  toggleAnalysis(type: string, checked: boolean): void {
    const current = this.editingConsultation.requestedExams || '';
    let list = current ? current.split(',').map(s => s.trim()).filter(s => s) : [];
    if (checked) {
      if (!list.includes(type)) list.push(type);
    } else {
      list = list.filter(s => s !== type);
    }
    this.editingConsultation.requestedExams = list.join(', ');
  }

  private loadMedicalRecord(patientId: number): void {
    this.medicalLoading = true;
    this.patientMedicalRecord = null;
    this.editingMedicalRecord = {};
    this.patientService.getPatientMedicalRecord(patientId).subscribe({
      next: (record) => {
        this.patientMedicalRecord = record;
        this.editingMedicalRecord = { ...record };
        this.medicalLoading = false;
      },
      error: () => {
        this.medicalLoading = false;
      }
    });
  }

  private initEditingConsultation(c: ConsultationResponse): void {
    let followUpDate = '';
    let followUpTime = '';
    if (c.followUpDate) {
      try {
        const parts = c.followUpDate.split('T');
        followUpDate = parts[0];
        followUpTime = parts.length > 1 ? parts[1].substring(0, 5) : '';
      } catch {
        followUpDate = c.followUpDate;
      }
    }
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
      followUpDate,
      followUpTime
    };
  }

  loadConsultations(): void {
    this.loading = true;
    this.consultationService.getAllConsultations(this.filterStatus || undefined).subscribe({
      next: (data) => {
        this.consultations = data;
        this.applyFilter();
        this.loading = false;
        this.loadTodayAppointments();
      },
      error: () => {
        this.loading = false;
        this.loadTodayAppointments();
      }
    });
  }

  private applyFilter(): void {
    let filtered = this.consultations;
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(c => {
        const name = this.getPatientName(c.patientId).toLowerCase();
        return name.includes(term) ||
          (c.patientId && c.patientId.toString().includes(term)) ||
          (c.reason && c.reason.toLowerCase().includes(term)) ||
          (c.diagnosis && c.diagnosis.toLowerCase().includes(term));
      });
    }
    this.filteredConsultations = filtered;
  }

  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.applyFilter();
  }

  loadTodayAppointments(): void {
    this.appointmentService.getDoctorAppointments().subscribe({
      next: (data) => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);

        const consultationAppointmentIds = new Set(
          this.consultations.map(c => c.appointmentId).filter(id => id != null)
        );

        this.todayAppointments = data
          .filter(app => {
            const appDate = new Date(app.dateTime);
            return app.status === 'CONFIRMED'
              && appDate >= today
              && appDate < tomorrow
              && !consultationAppointmentIds.has(app.id);
          })
          .map(app => ({
            id: app.id,
            patientId: app.patientId,
            reason: app.notes || 'Consultation générale',
            hour: this.formatHour(app.dateTime),
            mode: app.mode === 'TELECONSULTATION' ? 'Téléconsultation' : 'Présentiel',
            type: app.mode === 'TELECONSULTATION' ? 'TELECONSULTATION' : 'PRESENTIEL'
          }));
      },
      error: () => {}
    });
  }

  private formatHour(dateTimeStr: string): string {
    try {
      const d = new Date(dateTimeStr);
      return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    } catch {
      return dateTimeStr;
    }
  }

  startConsultationFromAppointment(appt: TodayAppointment): void {
    this.startingId = appt.id;
    const request: ConsultationRequest = {
      patientId: appt.patientId,
      appointmentId: appt.id,
      reason: appt.reason,
      type: appt.type
    };
    this.consultationService.startConsultation(request).subscribe({
      next: (created) => {
        this.todayAppointments = this.todayAppointments.filter(a => a.id !== appt.id);
        this.startingId = null;
        this.selectedConsultation = created;
        this.initEditingConsultation(created);
        this.loadMedicalRecord(created.patientId);
        this.loadConsultations();
      },
      error: (err) => {
        console.error('Erreur création consultation', err);
        this.startingId = null;
      }
    });
  }

  filterByStatus(status: string): void {
    this.filterStatus = status;
    this.loadConsultations();
  }

  selectConsultation(c: ConsultationResponse): void {
    this.selectedConsultation = c;
    this.initEditingConsultation(c);
    this.loadMedicalRecord(c.patientId);
  }

  backToList(): void {
    this.selectedConsultation = null;
    this.patientMedicalRecord = null;
  }

  toggleFollowUp(enabled: boolean): void {
    if (enabled) {
      const nextWeek = new Date();
      nextWeek.setDate(nextWeek.getDate() + 7);
      this.editingConsultation.followUpDate = nextWeek.toISOString().split('T')[0];
      this.editingConsultation.followUpTime = '';
      this.loadAvailableSlots();
    } else {
      this.editingConsultation.followUpDate = '';
      this.editingConsultation.followUpTime = '';
      this.availableSlots = [];
    }
  }

  onFollowUpDateChange(): void {
    this.editingConsultation.followUpTime = '';
    this.loadAvailableSlots();
  }

  private loadAvailableSlots(): void {
    const doctorId = this.authService.getCurrentUser()?.id;
    const date = this.editingConsultation.followUpDate;
    if (!doctorId || !date) { this.availableSlots = []; return; }
    this.loadingSlots = true;
    const wh = this.workingHours;
    this.appointmentService.getAvailableSlots(
      doctorId, date, wh.debutMatin, wh.finMatin, wh.debutApresMidi, wh.finApresMidi
    ).subscribe({
      next: (slots) => {
        this.availableSlots = slots;
        this.loadingSlots = false;
      },
      error: () => {
        this.availableSlots = [];
        this.loadingSlots = false;
      }
    });
  }

  private syncMedicalRecord(): void {
    if (!this.selectedConsultation) return;
    this.editingMedicalRecord.insuranceCompany = 'CNAM';
    this.patientService.updatePatientMedicalRecord(this.selectedConsultation.patientId, this.editingMedicalRecord).subscribe({
      next: (record) => {
        this.patientMedicalRecord = record;
        this.editingMedicalRecord = { ...record };
      },
      error: () => {}
    });
  }

  saveMedicalRecord(): void {
    if (!this.selectedConsultation) return;
    this.editingMedicalRecord.insuranceCompany = 'CNAM';
    this.patientService.updatePatientMedicalRecord(this.selectedConsultation.patientId, this.editingMedicalRecord).subscribe({
      next: (record) => {
        this.patientMedicalRecord = record;
        this.editingMedicalRecord = { ...record };
      },
      error: () => {}
    });
  }

  private createFollowUpAppointment(): void {
    if (!this.selectedConsultation) return;
    const followUpDate = this.editingConsultation.followUpDate;
    const followUpTime = this.editingConsultation.followUpTime;
    if (!followUpDate || followUpDate === '' || !followUpTime) return;
    const dateTime = followUpDate.includes('T') ? followUpDate : `${followUpDate}T${followUpTime}:00`;
    this.appointmentService.createFollowUpAppointment(this.selectedConsultation.patientId, dateTime).subscribe({
      error: (err) => console.error('Follow-up appointment creation failed:', err)
    });
  }

  completeConsultation(): void {
    if (!this.selectedConsultation) return;
    const fd = this.editingConsultation.followUpDate;
    const ft = this.editingConsultation.followUpTime;
    const combinedDate = fd && ft ? `${fd}T${ft}:00` : null;
    if (combinedDate) {
      this.editingConsultation.followUpDate = combinedDate;
    }
    this.syncMedicalRecord();
    this.consultationService.completeConsultation(this.selectedConsultation.id, this.editingConsultation).subscribe({
      next: (updated) => {
        this.selectedConsultation = updated;
        this.initEditingConsultation(updated);
        this.loadConsultations();
        if (combinedDate) {
          this.editingConsultation.followUpDate = fd || '';
        }
        if (updated.appointmentId) {
          this.appointmentService.markAppointmentAsCompleted(updated.appointmentId).subscribe({
            error: (err) => console.error('Mark appointment completed failed:', err)
          });
        }
        this.createFollowUpAppointment();
      },
      error: (err) => {
        console.error('Complete consultation failed:', err);
      }
    });
  }

  cancelConsultation(c: ConsultationResponse): void {
    this.consultationService.cancelConsultation(c.id).subscribe({
      next: () => {
        this.selectedConsultation = null;
        this.loadConsultations();
      }
    });
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'En attente';
      case 'IN_PROGRESS': return 'En cours';
      case 'COMPLETED': return 'Terminée';
      case 'CANCELLED': return 'Annulée';
      default: return status;
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'pending';
      case 'IN_PROGRESS': return 'progress';
      case 'COMPLETED': return 'completed';
      case 'CANCELLED': return 'cancelled';
      default: return '';
    }
  }
}
