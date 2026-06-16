import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService, PatientListDto } from '../../../core/services/auth.service';
import { AppointmentService, AppointmentDto } from '../../../core/services/appointment.service';

type DoctorSectionKey = 'patients' | 'appointments' | 'consultations' | 'prescriptions' | 'labs' | 'profile';

// ── Types internes ──────────────────────────────────────────────────────────
interface CalendarDay {
  day: number | null;
  date: Date | null;
  isToday: boolean;
  isSelected: boolean;
  appointmentCount: number;
  hasPending: boolean;
  hasConfirmed: boolean;
}

interface AgendaAppointment {
  id: number;
  patient: string;
  reason: string;
  date: string;
  hour: string;
  mode: string;
  modeIcon: string;
  status: string;
  statusClass: string;
  rawDateTime: string;
}

@Component({
  selector: 'app-doctor-section',
  templateUrl: './doctor-section.component.html',
  styleUrls: ['./doctor-section.component.scss']
})
export class DoctorSectionComponent implements OnInit {

  section: DoctorSectionKey = 'patients';
  title = '';
  currentUser: any;

  // ── Patients ────────────────────────────────────────────────────────────
  patients: any[] = [];
  registeredPatients: PatientListDto[] = [];

  // ── Agenda ──────────────────────────────────────────────────────────────
  allAppointments: AgendaAppointment[] = [];
  loadingAppointments = false;
  actionLoading: { [id: number]: boolean } = {};

  // Calendrier
  calendarYear = 0;
  calendarMonth = 0;        // 0-based (JS)
  calendarWeekdays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  calendarDays: CalendarDay[] = [];
  selectedDate: Date | null = null;

  // Filtres
  statusFilter: 'ALL' | 'PENDING' | 'CONFIRMED' | 'CANCELLED' = 'ALL';
  searchQuery: string = '';

  // ── Données mock autres sections ─────────────────────────────────────────
  consultations = [
    { patient: 'Mohamed Aloui', summary: 'Tension stabilisée, traitement maintenu', doctor: 'Compte rendu rédigé', date: '02 juin 2026', status: 'Clôturée' },
    { patient: 'Fatma Khelifi', summary: "Ajustement de la posologie d'insuline", doctor: 'Compte rendu à finaliser', date: '28 mai 2026', status: 'À finaliser' },
    { patient: 'Sami Bouazizi', summary: 'Première consultation, bilan demandé', doctor: 'En attente des analyses', date: '15 mai 2026', status: 'En cours' }
  ];

  prescriptions = [
    { medication: 'Amlodipine 5 mg', dosage: '1 comprimé / jour', patient: 'Mohamed Aloui', renewal: 'À signer', status: 'En attente' },
    { medication: 'Metformine 850 mg', dosage: '2 comprimés / jour', patient: 'Fatma Khelifi', renewal: 'Éditée le 28 mai', status: 'Active' },
    { medication: 'Ventoline', dosage: 'En cas de crise', patient: 'Sami Bouazizi', renewal: 'Ordonnance récente', status: 'Nouvelle' }
  ];

  labResults = [
    { exam: 'Bilan lipidique - Karim Jelassi', lab: 'Centre de biologie El Menzah', date: '08 juin 2026', result: 'À valider', note: 'LDL en baisse, contrôle recommandé dans 3 mois.' },
    { exam: 'Glycémie à jeun - Fatma Khelifi', lab: 'Laboratoire Charles Nicolle', date: '06 juin 2026', result: 'Validé', note: 'Valeurs dans la norme.' },
    { exam: 'NFS complète - Sami Bouazizi', lab: 'Laboratoire Pasteur', date: "Aujourd'hui", result: 'En attente', note: "Publication estimée avant 18:00." }
  ];

  profileCards: { label: string; value: string }[] = [];
  profileChecklist = [
    "Maintenir vos disponibilités à jour dans l'agenda.",
    'Finaliser les comptes rendus après chaque consultation.',
    "Valider rapidement les résultats d'analyses reçus."
  ];

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private appointmentService: AppointmentService
  ) {
    this.currentUser = this.authService.getCurrentUser();
    this.profileCards = [
      { label: 'Nom complet', value: `Dr. ${this.currentUser?.firstName || ''} ${this.currentUser?.lastName || ''}`.trim() },
      { label: 'Email', value: this.currentUser?.email || 'Non renseigné' },
      { label: 'Téléphone', value: this.currentUser?.phone || 'Non renseigné' },
      { label: 'Spécialité', value: this.currentUser?.specialty || 'Non renseignée' },
      { label: "Numéro d'ordre", value: this.currentUser?.licenseNumber || 'Non renseigné' },
      { label: 'Établissement', value: this.currentUser?.facility || 'Non renseigné' }
    ];

    // Initialiser le calendrier sur le mois courant
    const now = new Date();
    this.calendarYear = now.getFullYear();
    this.calendarMonth = now.getMonth();
    this.selectedDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.section = data['section'] as DoctorSectionKey;
      this.title = data['title'] as string;
      this.loadPatients();
    });
  }

  // ── Chargement données ────────────────────────────────────────────────────

  loadPatients(): void {
    this.authService.getAllPatients().subscribe({
      next: (data) => {
        this.registeredPatients = data;
        this.patients = data.map(p => ({
          name: `${p.firstName} ${p.lastName}`,
          reason: 'Dossier médical',
          age: p.phone ? `Tél: ${p.phone}` : 'Patient MediLink',
          lastVisit: 'Consultations en cours',
          status: 'Actif'
        }));
        this.loadAppointments();
      },
      error: () => this.loadAppointments()
    });
  }

  loadAppointments(): void {
    this.loadingAppointments = true;
    this.appointmentService.getDoctorAppointments().subscribe({
      next: (data) => {
        this.allAppointments = data.map(app => this.mapAppointment(app));
        this.buildCalendar();
        this.loadingAppointments = false;
      },
      error: (err) => {
        console.error('Erreur chargement agenda', err);
        this.loadingAppointments = false;
        this.buildCalendar();
      }
    });
  }

  private mapAppointment(app: AppointmentDto): AgendaAppointment {
    return {
      id: app.id,
      patient: this.getPatientName(app.patientId),
      reason: app.notes || 'Consultation générale',
      date: this.formatDate(app.dateTime),
      hour: this.formatHour(app.dateTime),
      mode: app.mode === 'TELECONSULTATION' ? 'Téléconsultation' : 'Présentiel',
      modeIcon: app.mode === 'TELECONSULTATION' ? '💻' : '🏥',
      status: this.getStatusLabel(app.status),
      statusClass: this.getStatusClass(app.status),
      rawDateTime: app.dateTime
    };
  }

  // ── Calendrier ────────────────────────────────────────────────────────────

  buildCalendar(): void {
    const year = this.calendarYear;
    const month = this.calendarMonth;
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const today = new Date();

    // Lundi = 0, Dimanche = 6 (format EU)
    let startDow = firstDay.getDay() - 1;
    if (startDow < 0) startDow = 6;

    const days: CalendarDay[] = [];

    // Cellules vides avant le 1er
    for (let i = 0; i < startDow; i++) {
      days.push({ day: null, date: null, isToday: false, isSelected: false, appointmentCount: 0, hasPending: false, hasConfirmed: false });
    }

    for (let d = 1; d <= lastDay.getDate(); d++) {
      const date = new Date(year, month, d);
      const isToday = date.toDateString() === today.toDateString();
      const isSelected = this.selectedDate ? date.toDateString() === this.selectedDate.toDateString() : false;

      const dayApps = this.allAppointments.filter(app => {
        const appDate = new Date(app.rawDateTime);
        return appDate.getFullYear() === year && appDate.getMonth() === month && appDate.getDate() === d;
      });

      days.push({
        day: d,
        date,
        isToday,
        isSelected,
        appointmentCount: dayApps.length,
        hasPending: dayApps.some(a => a.statusClass === 'status-pending'),
        hasConfirmed: dayApps.some(a => a.statusClass === 'status-confirmed')
      });
    }

    this.calendarDays = days;
  }

  prevMonth(): void {
    if (this.calendarMonth === 0) {
      this.calendarMonth = 11;
      this.calendarYear--;
    } else {
      this.calendarMonth--;
    }
    this.buildCalendar();
  }

  nextMonth(): void {
    if (this.calendarMonth === 11) {
      this.calendarMonth = 0;
      this.calendarYear++;
    } else {
      this.calendarMonth++;
    }
    this.buildCalendar();
  }

  selectDay(day: CalendarDay): void {
    if (!day.date) return;
    this.selectedDate = day.date;
    this.buildCalendar();
  }

  get calendarMonthLabel(): string {
    const months = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
      'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
    return `${months[this.calendarMonth]} ${this.calendarYear}`;
  }

  // ── Filtres & affichage ───────────────────────────────────────────────────

  get filteredAppointments(): AgendaAppointment[] {
    let list = this.allAppointments;

    if (this.selectedDate) {
      const sel = this.selectedDate;
      list = list.filter(app => {
        const d = new Date(app.rawDateTime);
        return d.getFullYear() === sel.getFullYear()
          && d.getMonth() === sel.getMonth()
          && d.getDate() === sel.getDate();
      });
    }

    // Filtre par statut
    if (this.statusFilter !== 'ALL') {
      const classMap: Record<string, string> = {
        PENDING: 'status-pending',
        CONFIRMED: 'status-confirmed',
        CANCELLED: 'status-cancelled'
      };
      list = list.filter(app => app.statusClass === classMap[this.statusFilter]);
    }

    // Recherche textuelle dynamique
    const q = this.searchQuery.trim().toLowerCase();
    if (q) {
      list = list.filter(app =>
        app.patient.toLowerCase().includes(q) ||
        app.reason.toLowerCase().includes(q) ||
        app.mode.toLowerCase().includes(q)
      );
    }

    return list;
  }

  get selectedDateLabel(): string {
    if (!this.selectedDate) return 'Tous les rendez-vous';
    const opts: Intl.DateTimeFormatOptions = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
    const s = this.selectedDate.toLocaleDateString('fr-FR', opts);
    return s.charAt(0).toUpperCase() + s.slice(1);
  }

  clearDateFilter(): void {
    this.selectedDate = null;
    this.buildCalendar();
  }

  // ── Actions ───────────────────────────────────────────────────────────────

  confirmAppointment(app: AgendaAppointment): void {
    this.actionLoading[app.id] = true;
    this.appointmentService.confirmAppointment(app.id).subscribe({
      next: (updated) => {
        const idx = this.allAppointments.findIndex(a => a.id === app.id);
        if (idx !== -1) {
          this.allAppointments[idx] = this.mapAppointmentFromDto(updated, app);
        }
        this.buildCalendar();
        this.actionLoading[app.id] = false;
      },
      error: (err) => {
        console.error('Erreur confirmation', err);
        this.actionLoading[app.id] = false;
      }
    });
  }

  cancelAppointment(app: AgendaAppointment): void {
    if (!confirm(`Annuler le RDV de ${app.patient} le ${app.date} à ${app.hour} ?`)) return;
    this.actionLoading[app.id] = true;
    this.appointmentService.cancelByDoctor(app.id).subscribe({
      next: (updated) => {
        const idx = this.allAppointments.findIndex(a => a.id === app.id);
        if (idx !== -1) {
          this.allAppointments[idx] = this.mapAppointmentFromDto(updated, app);
        }
        this.buildCalendar();
        this.actionLoading[app.id] = false;
      },
      error: (err) => {
        console.error('Erreur annulation', err);
        this.actionLoading[app.id] = false;
      }
    });
  }

  private mapAppointmentFromDto(dto: AppointmentDto, original: AgendaAppointment): AgendaAppointment {
    return {
      ...original,
      status: this.getStatusLabel(dto.status),
      statusClass: this.getStatusClass(dto.status)
    };
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  getPatientName(patientId: number): string {
    const patient = this.registeredPatients.find(p => p.id === patientId);
    return patient ? `${patient.firstName} ${patient.lastName}` : `Patient #${patientId}`;
  }

  formatDate(dateTimeStr: string): string {
    if (!dateTimeStr) return '';
    try {
      const date = new Date(dateTimeStr);
      const opts: Intl.DateTimeFormatOptions = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
      const s = date.toLocaleDateString('fr-FR', opts);
      return s.charAt(0).toUpperCase() + s.slice(1);
    } catch { return dateTimeStr; }
  }

  formatHour(dateTimeStr: string): string {
    if (!dateTimeStr) return '';
    try {
      const date = new Date(dateTimeStr);
      return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    } catch { return dateTimeStr; }
  }

  getStatusLabel(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'CONFIRMED': return 'Confirmé';
      case 'CANCELLED': return 'Annulé';
      case 'PENDING': default: return 'En attente';
    }
  }

  getStatusClass(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'CONFIRMED': return 'status-confirmed';
      case 'CANCELLED': return 'status-cancelled';
      case 'PENDING': default: return 'status-pending';
    }
  }
}
