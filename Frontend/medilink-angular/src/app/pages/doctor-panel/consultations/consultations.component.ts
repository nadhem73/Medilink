import { Component, OnInit } from '@angular/core';
import { ConsultationService, ConsultationResponse, ConsultationRequest } from '../../../core/services/consultation.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { PrescriptionService, PrescriptionEmailRequest } from '../../../core/services/prescription.service';
import { jsPDF } from 'jspdf';
import { MINISTRY_LOGO_BASE64 } from './ministry-logo';

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
  private patientMap = new Map<number, { firstName: string; lastName: string; gender?: string; email?: string }>();

  get isCompleted(): boolean {
    return this.selectedConsultation?.status === 'COMPLETED';
  }
  loadingSlots = false;
  availableSlots: string[] = [];
  private workingHours = { debutMatin: '08:00', finMatin: '13:00', debutApresMidi: '15:00', finApresMidi: '19:00' };

  showPrescriptionModal = false;
  existingPrescriptionId: number | null = null;
  prescriptionError = '';
  private savedPrescriptionItems: any[] = [];

  private doctorProfile: any = null;

  constructor(
    private consultationService: ConsultationService,
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    private prescriptionService: PrescriptionService
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
        this.doctorProfile = profile;
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
        patients.forEach(p => this.patientMap.set(p.id, { firstName: p.firstName, lastName: p.lastName, gender: p.gender, email: p.email }));
      }
    });
  }

  getPatientName(patientId: number): string {
    const p = this.patientMap.get(patientId);
    return p ? `${p.firstName} ${p.lastName}` : `Patient #${patientId}`;
  }

  private getCurrentDoctorName(): string {
    const user = this.authService.getCurrentUser();
    if (!user) return '';
    const fn = user.firstName || (this.doctorProfile?.firstName ?? '');
    const ln = user.lastName || (this.doctorProfile?.lastName ?? '');
    return fn || ln ? `Dr. ${fn} ${ln}`.trim() : '';
  }

  private getCurrentDoctorSpecialty(): string {
    return this.doctorProfile?.specialty || '';
  }

  private getCurrentDoctorLicense(): string {
    return this.doctorProfile?.licenseNumber || '';
  }

  private getCurrentDoctorPhone(): string {
    const user = this.authService.getCurrentUser();
    return user?.phone || this.doctorProfile?.phone || '';
  }

  private getCurrentDoctorEmail(): string {
    const user = this.authService.getCurrentUser();
    return user?.email || this.doctorProfile?.email || '';
  }

  getPatientEmail(patientId: number): string {
    return this.patientMap.get(patientId)?.email || '';
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
    this.consultationService.getAllConsultations().subscribe({
      next: (allConsultations) => {
        const consultationAppointmentIds = new Set(
          allConsultations.map(c => c.appointmentId).filter(id => id != null)
        );
        this.appointmentService.getDoctorAppointments().subscribe({
          next: (data) => {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const tomorrow = new Date(today);
            tomorrow.setDate(tomorrow.getDate() + 1);

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
      },
      error: () => {
        this.appointmentService.getDoctorAppointments().subscribe({
          next: (data) => {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const tomorrow = new Date(today);
            tomorrow.setDate(tomorrow.getDate() + 1);

            this.todayAppointments = data
              .filter(app => {
                const appDate = new Date(app.dateTime);
                return app.status === 'CONFIRMED'
                  && appDate >= today
                  && appDate < tomorrow;
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
    this.existingPrescriptionId = c.prescriptionId || null;
  }

  backToList(): void {
    this.selectedConsultation = null;
    this.patientMedicalRecord = null;
    this.existingPrescriptionId = null;
    this.prescriptionError = '';
  }

  openPrescriptionModal(): void {
    this.showPrescriptionModal = true;
    this.prescriptionError = '';
  }

  onAnalysesChanged(analyses: string): void {
    this.editingConsultation.requestedExams = analyses;
  }

  closePrescriptionModal(): void {
    this.showPrescriptionModal = false;
  }

  onPrescriptionSaved(): void {
    this.showPrescriptionModal = false;
    if (this.selectedConsultation) {
      const exams = this.editingConsultation.requestedExams;
      this.consultationService.getConsultation(this.selectedConsultation.id).subscribe({
        next: (updated) => {
          this.selectedConsultation = updated;
          this.existingPrescriptionId = updated.prescriptionId || null;
          this.initEditingConsultation(updated);
          if (exams) {
            this.editingConsultation.requestedExams = exams;
          }
          if (this.existingPrescriptionId) {
            this.prescriptionService.getPrescription(this.existingPrescriptionId).subscribe({
              next: (p) => { this.savedPrescriptionItems = p.items || []; }
            });
          }
        }
      });
    }
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
        this.existingPrescriptionId = updated.prescriptionId || null;
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
        this.sendPrescriptionEmailAfterCompletion();
      },
      error: (err) => {
        console.error('Complete consultation failed:', err);
      }
    });
  }

  private sendPrescriptionEmailAfterCompletion(): void {
    if (!this.selectedConsultation) return;
    const patientName = this.getPatientName(this.selectedConsultation.patientId);
    const patientEmail = this.getPatientEmail(this.selectedConsultation.patientId);
    if (!patientEmail) {
      console.warn('Aucun email trouvé pour le patient, email non envoyé.');
      return;
    }

    const hasAnalyses = !!(this.editingConsultation.requestedExams?.trim());
    const analyses = hasAnalyses
      ? this.editingConsultation.requestedExams!.split(',').map(s => s.trim()).filter(s => s)
      : [];

    const prescriptionId = this.selectedConsultation.prescriptionId
      || this.existingPrescriptionId;

    if (!prescriptionId && !hasAnalyses) return;

    const sendWithItems = (items: any[]) => {
      let pdfMeds: string | null = null;
      let pdfAnalyses: string | null = null;

      if (items.length > 0) {
        try {
          pdfMeds = this.generateMedicationPdfBase64(items);
        } catch (e) { console.error('PDF medicaments error:', e); }
      }

      if (hasAnalyses) {
        try {
          pdfAnalyses = this.generateAnalysesPdfBase64(analyses);
        } catch (e) { console.error('PDF analyses error:', e); }
      }

      if (!pdfMeds && !pdfAnalyses) return;

      const request: PrescriptionEmailRequest = {
        patientEmail,
        patientName,
        pdfMedicationsBase64: pdfMeds || undefined,
        pdfAnalysesBase64: pdfAnalyses || undefined,
        medicationsFileName: pdfMeds ? 'Ordonnance_Medicaments.pdf' : undefined,
        analysesFileName: pdfAnalyses ? 'Ordonnance_Analyses.pdf' : undefined,
      };

      this.prescriptionService.sendPrescriptionEmail(request).subscribe({
        next: () => console.log('Email d\'ordonnance envoyé avec succès'),
        error: (err) => console.error('Erreur envoi email ordonnance:', err)
      });
    };

    if (this.savedPrescriptionItems.length > 0) {
      sendWithItems(this.savedPrescriptionItems);
    } else if (prescriptionId) {
      this.prescriptionService.getPrescription(prescriptionId).subscribe({
        next: (p) => sendWithItems(p.items || []),
        error: (err) => console.error('Erreur récupération prescription:', err)
      });
    } else {
      sendWithItems([]);
    }
  }

  private downloadBase64Pdf(base64: string, fileName: string): void {
    const link = document.createElement('a');
    link.href = 'data:application/pdf;base64,' + base64;
    link.download = fileName;
    link.click();
  }

  //
  // ─── PDF TEMPLATE CONSTANTS ──────────────────────────────────────────────
  //

  // MINISTRY_LOGO_BASE64 now imported from ./ministry-logo

  private readonly PDF = {
    M: 20,
    W: 170,
    C: 105,
    gold: [184, 134, 44] as [number, number, number],
    navy: [26, 43, 60] as [number, number, number],
    green: [38, 115, 73] as [number, number, number],
    blue: [0, 90, 140] as [number, number, number],
    paper: [247, 245, 240] as [number, number, number],
    rule: [230, 228, 220] as [number, number, number],
    text: [30, 30, 30] as [number, number, number],
    white: [255, 255, 255] as [number, number, number],
  };

  //
  // ─── SHARED PDF HELPERS ─────────────────────────────────────────────────
  //

  private pdfHeader(doc: jsPDF): number {
    const M = this.PDF.M, C = this.PDF;
    doc.addImage(MINISTRY_LOGO_BASE64, 'PNG', M, 7, 16, 16);
    doc.setTextColor(C.navy[0], C.navy[1], C.navy[2]);
    doc.setFont('helvetica', 'bold'); doc.setFontSize(9);
    doc.text('RÉPUBLIQUE TUNISIENNE', M + 20, 11);
    doc.setFont('helvetica', 'normal'); doc.setFontSize(8);
    doc.text('MINISTÈRE DE LA SANTÉ', M + 20, 17);
    doc.setFont('helvetica', 'bold'); doc.setFontSize(16);
    doc.text('ORDONNANCE MÉDICALE', 105, 12, { align: 'center' });
    const lic = this.getCurrentDoctorLicense();
    doc.setFont('helvetica', 'normal'); doc.setFontSize(7);
    doc.setTextColor(140, 140, 140);
    doc.text(`N° TOM : ${lic || '__________'}`, 105, 18, { align: 'center' });
    doc.setDrawColor(C.gold[0], C.gold[1], C.gold[2]);
    doc.setLineWidth(0.4);
    doc.line(M, 23, 210 - M, 23);
    doc.setFillColor(C.green[0], C.green[1], C.green[2]);
    doc.rect(M, 27, 3, 16, 'F');
    doc.setTextColor(C.text[0], C.text[1], C.text[2]);
    doc.setFont('helvetica', 'bold'); doc.setFontSize(10);
    doc.text(this.getCurrentDoctorName() || 'Médecin traitant', M + 6, 32);
    doc.setFont('helvetica', 'normal'); doc.setFontSize(7.5);
    const spec = this.getCurrentDoctorSpecialty();
    const phone = this.getCurrentDoctorPhone();
    const email = this.getCurrentDoctorEmail();
    const hosp = this.getCurrentDoctorHospital();
    const detail = [spec, phone, email].filter(Boolean).join(' · ');
    if (detail) doc.text(detail, M + 6, 38);
    if (hosp) doc.text(hosp, M + 6, 43);
    doc.setDrawColor(C.rule[0], C.rule[1], C.rule[2]);
    doc.setLineWidth(0.2);
    doc.line(M, 48, 210 - M, 48);
    doc.setFont('helvetica', 'bold'); doc.setFontSize(8);
    doc.setTextColor(C.navy[0], C.navy[1], C.navy[2]);
    doc.text('Patient', M, 53);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(C.text[0], C.text[1], C.text[2]);
    doc.text(': ' + this.getPatientName(this.selectedConsultation?.patientId || 0), M + 14, 53);
    const cnam = this.patientMedicalRecord?.insuranceNumber;
    if (cnam) {
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(C.navy[0], C.navy[1], C.navy[2]);
      doc.text('N° CNAM', 210 - M - 55, 53);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(C.text[0], C.text[1], C.text[2]);
      doc.text(': ' + cnam, 210 - M - 35, 53);
    }
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(C.navy[0], C.navy[1], C.navy[2]);
    doc.text('Date', 210 - M - 55, 58);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(C.text[0], C.text[1], C.text[2]);
    doc.text(': ' + new Date().toLocaleDateString('fr-FR'), 210 - M - 35, 58);
    doc.setDrawColor(C.rule[0], C.rule[1], C.rule[2]);
    doc.setLineWidth(0.2);
    doc.line(M, 61, 210 - M, 61);
    return 67;
  }

  private getCurrentDoctorHospital(): string {
    return this.doctorProfile?.hospital || '';
  }

  private pdfFooter(doc: jsPDF, y: number): void {
    const M = this.PDF.M;
    doc.setDrawColor(this.PDF.rule[0], this.PDF.rule[1], this.PDF.rule[2]);
    doc.setLineWidth(0.2);
    doc.line(M, y, 210 - M, y);
    doc.setFont('helvetica', 'normal'); doc.setFontSize(6.5);
    doc.setTextColor(160, 160, 160);
    doc.text('Document généré par MediLink Tunisia — valable 3 mois à compter de la date d\'émission', 105, y + 5, { align: 'center' });
  }

  private pdfSigCachet(doc: jsPDF, y: number): number {
    const M = this.PDF.M, C = this.PDF;
    doc.setDrawColor(C.rule[0], C.rule[1], C.rule[2]);
    doc.setLineWidth(0.2);
    doc.line(M, y, 210 - M, y); y += 3;
    doc.setFont('helvetica', 'bold'); doc.setFontSize(7.5);
    doc.setTextColor(C.text[0], C.text[1], C.text[2]);
    doc.text('Signature du médecin', M, y + 6);
    doc.setFont('helvetica', 'italic'); doc.setFontSize(6.5);
    doc.setTextColor(160, 160, 160);
    doc.text('Cachet & signature obligatoires', M, y + 11);
    const dash = (x1: number, y1: number, x2: number, y2: number) => {
      for (let t = 0; t < 1; t += 0.03) {
        const s = Math.min(t + 0.015, 1);
        doc.line(x1 + t * (x2 - x1), y1 + t * (y2 - y1), x1 + s * (x2 - x1), y1 + s * (y2 - y1));
      }
    };
    dash(M, y + 4, M + 68, y + 4);
    doc.setDrawColor(C.gold[0], C.gold[1], C.gold[2]);
    doc.setLineWidth(0.5);
    doc.rect(210 - M - 45, y - 2, 45, 20);
    doc.setFont('helvetica', 'bold'); doc.setFontSize(6.5);
    doc.setTextColor(C.gold[0], C.gold[1], C.gold[2]);
    doc.text('CACHET', 210 - M - 22.5, y + 5, { align: 'center' });
    doc.text('DU MÉDECIN', 210 - M - 22.5, y + 12, { align: 'center' });
    return y + 24;
  }

  private pdfBg(doc: jsPDF): void {
    doc.setFillColor(this.PDF.paper[0], this.PDF.paper[1], this.PDF.paper[2]);
    doc.rect(0, 0, 210, 297, 'F');
    doc.setFillColor(this.PDF.gold[0], this.PDF.gold[1], this.PDF.gold[2]);
    doc.rect(0, 0, 210, 3, 'F');
  }

  //
  // ─── MÉDICAMENTS PDF ──────────────────────────────────────────────────
  //

  private generateMedicationPdfBase64(items: any[]): string {
    const doc = new jsPDF({ unit: 'mm', format: 'a4' });
    const M = this.PDF.M, C = this.PDF;
    const hasInstr = items.some(i => i.instructions);
    const col = hasInstr
      ? [M, M + 52, M + 92, M + 124, M + 148]
      : [M, M + 58, M + 108, M + 138, M + 158];
    this.pdfBg(doc);
    let y = this.pdfHeader(doc);
    y += 2;
    doc.setFillColor(C.green[0], C.green[1], C.green[2]);
    doc.rect(M, y, 3, 12, 'F');
    doc.setTextColor(C.navy[0], C.navy[1], C.navy[2]);
    doc.setFont('helvetica', 'bold'); doc.setFontSize(9);
    doc.text('Médicaments prescrits', M + 6, y + 4);
    doc.setFont('helvetica', 'normal'); doc.setFontSize(6.5);
    doc.setTextColor(150, 150, 150);
    doc.text('Prescription médicamenteuse', M + 6, y + 9); y += 16;

    doc.setFillColor(C.navy[0], C.navy[1], C.navy[2]);
    doc.rect(M, y, C.W, 5.5, 'F');
    doc.setTextColor(C.white[0], C.white[1], C.white[2]);
    doc.setFont('helvetica', 'bold'); doc.setFontSize(7);
    doc.text('Médicament', col[0] + 2, y + 4);
    doc.text('Posologie', col[1] + 2, y + 4);
    doc.text('Voie', col[2] + 2, y + 4);
    doc.text('Durée', col[3] + 2, y + 4);
    if (hasInstr) doc.text('Instructions', col[4] + 2, y + 4);
    else doc.text('Qté', col[4] + 2, y + 4);
    y += 7;

    doc.setTextColor(C.text[0], C.text[1], C.text[2]);
    doc.setFont('helvetica', 'normal'); doc.setFontSize(7.5);

    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (y > 265) { this.pdfFooter(doc, y); doc.addPage(); this.pdfBg(doc); y = this.pdfHeader(doc); y += 2;
        doc.setFillColor(C.green[0], C.green[1], C.green[2]);
        doc.rect(M, y, 3, 12, 'F');
        doc.setTextColor(C.navy[0], C.navy[1], C.navy[2]);
        doc.setFont('helvetica', 'bold'); doc.setFontSize(9);
        doc.text('Médicaments prescrits (suite)', M + 6, y + 4);
        doc.setFont('helvetica', 'normal'); doc.setFontSize(6.5);
        doc.setTextColor(150, 150, 150);
        doc.text('Prescription médicamenteuse', M + 6, y + 9); y += 16;
        doc.setFillColor(C.navy[0], C.navy[1], C.navy[2]);
        doc.rect(M, y, C.W, 5.5, 'F');
        doc.setTextColor(C.white[0], C.white[1], C.white[2]);
        doc.setFont('helvetica', 'bold'); doc.setFontSize(7);
        doc.text('Médicament', col[0] + 2, y + 4);
        doc.text('Posologie', col[1] + 2, y + 4);
        doc.text('Voie', col[2] + 2, y + 4);
        doc.text('Durée', col[3] + 2, y + 4);
        if (hasInstr) doc.text('Instructions', col[4] + 2, y + 4);
        else doc.text('Qté', col[4] + 2, y + 4);
        y += 7; }

      if (i % 2 === 0) { doc.setFillColor(242, 241, 237); doc.rect(M, y - 1, C.W, 6.5, 'F'); }

      doc.setFont('helvetica', 'bold'); doc.setFontSize(7.5);
      doc.text(`${item.medicamentName || ''} ${item.dosage || ''}`, col[0] + 2, y + 1);
      doc.setFont('helvetica', 'normal'); doc.setFontSize(7);
      doc.text(item.posologie || '', col[1] + 2, y + 1);
      doc.text(item.voieAdministration || '', col[2] + 2, y + 1);
      const d = item.dureeTraitement ? `${item.dureeTraitement} j` : '';
      doc.text(d, col[3] + 2, y + 1);
      if (hasInstr) doc.text(item.instructions || '—', col[4] + 2, y + 1);
      else doc.text(`${item.quantitePrescrite ?? ''}`, col[4] + 2, y + 1);
      y += 6;
    }

    y += 4;
    y = this.pdfSigCachet(doc, y);
    this.pdfFooter(doc, y);
    return doc.output('datauristring').split(',')[1];
  }

  //
  // ─── ANALYSES PDF ─────────────────────────────────────────────────────
  //

  private generateAnalysesPdfBase64(analyses: string[]): string {
    const doc = new jsPDF({ unit: 'mm', format: 'a4' });
    const M = this.PDF.M, C = this.PDF;
    this.pdfBg(doc);
    let y = this.pdfHeader(doc);
    y += 2;
    doc.setFillColor(C.blue[0], C.blue[1], C.blue[2]);
    doc.rect(M, y, 3, 12, 'F');
    doc.setTextColor(C.navy[0], C.navy[1], C.navy[2]);
    doc.setFont('helvetica', 'bold'); doc.setFontSize(9);
    doc.text('Analyses demandées', M + 6, y + 4);
    doc.setFont('helvetica', 'normal'); doc.setFontSize(6.5);
    doc.setTextColor(150, 150, 150);
    doc.text('Examens de laboratoire prescrits', M + 6, y + 9); y += 18;

    if (analyses.length === 0) {
      doc.setTextColor(160, 160, 160);
      doc.setFont('helvetica', 'italic'); doc.setFontSize(8);
      doc.text('Aucune analyse prescrite.', M, y);
    } else {
      doc.setTextColor(C.text[0], C.text[1], C.text[2]);
      doc.setFont('helvetica', 'normal'); doc.setFontSize(8.5);
      for (let i = 0; i < analyses.length; i++) {
        if (y > 262) { this.pdfFooter(doc, y); doc.addPage(); this.pdfBg(doc); y = this.pdfHeader(doc); y += 2;
          doc.setFillColor(C.blue[0], C.blue[1], C.blue[2]);
          doc.rect(M, y, 3, 12, 'F');
          doc.setTextColor(C.navy[0], C.navy[1], C.navy[2]);
          doc.setFont('helvetica', 'bold'); doc.setFontSize(9);
          doc.text('Analyses demandées (suite)', M + 6, y + 4);
          doc.setFont('helvetica', 'normal'); doc.setFontSize(6.5);
          doc.setTextColor(150, 150, 150);
          doc.text('Examens de laboratoire prescrits', M + 6, y + 9); y += 18; }

        doc.setDrawColor(C.blue[0], C.blue[1], C.blue[2]);
        doc.setLineWidth(0.1);
        doc.setFont('helvetica', 'bold'); doc.setFontSize(8);
        doc.text('✓', M + 2, y);
        doc.setFont('helvetica', 'normal'); doc.setFontSize(8.5);
        const aLines = doc.splitTextToSize(analyses[i], C.W - 14);
        doc.text(aLines, M + 8, y);
        y += Math.max(aLines.length * 4.5, 6);
        if (i < analyses.length - 1) {
          doc.setDrawColor(C.rule[0], C.rule[1], C.rule[2]);
          doc.setLineWidth(0.1);
          doc.line(M + 8, y, 210 - M, y);
          y += 1.5;
        }
      }
    }

    y += 6;
    doc.setFillColor(243, 242, 238);
    doc.rect(M, y, C.W, 16, 'F');
    doc.setDrawColor(C.blue[0], C.blue[1], C.blue[2]);
    doc.setLineWidth(0.35);
    doc.line(M, y, M, y + 16);
    doc.setFont('helvetica', 'italic'); doc.setFontSize(7);
    doc.setTextColor(C.text[0], C.text[1], C.text[2]);
    doc.text('Présentez cette ordonnance à votre laboratoire', M + 5, y + 5.5);
    doc.text("d'analyses médicales pour effectuer les examens prescrits.", M + 5, y + 11);
    y += 20;

    y = this.pdfSigCachet(doc, y);
    this.pdfFooter(doc, y);
    return doc.output('datauristring').split(',')[1];
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
