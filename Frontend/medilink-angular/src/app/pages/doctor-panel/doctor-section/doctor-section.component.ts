import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, PatientListDto } from '../../../core/services/auth.service';
import { AppointmentService, AppointmentDto } from '../../../core/services/appointment.service';
import { ConsultationService, ConsultationRequest, ConsultationResponse } from '../../../core/services/consultation.service';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';
import { DoctorService, Doctor } from '../../../core/services/doctor.service';
import { PrescriptionService, PrescriptionItemResponse } from '../../../core/services/prescription.service';
import { BilanService, BilanSummary, ScanBilanResponse } from '../../../core/services/bilan.service';


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
  patientId: number;
  patient: string;
  reason: string;
  date: string;
  hour: string;
  mode: string;
  modeIcon: string;
  rawMode: string;
  status: string;
  statusClass: string;
  rawDateTime: string;
}

@Component({
  selector: 'app-doctor-section',
  templateUrl: './doctor-section.component.html',
  styleUrls: ['./doctor-section.component.scss']
})
export class DoctorSectionComponent implements OnInit, AfterViewChecked {

  section: DoctorSectionKey = 'patients';
  title = '';
  currentUser: any;

  // ── Patients ────────────────────────────────────────────────────────────
  patients: any[] = [];
  registeredPatients: PatientListDto[] = [];
  selectedPatient: any = null;
  patientSearch = '';

  get filteredPatients(): any[] {
    if (!this.patientSearch.trim()) return this.patients;
    const q = this.patientSearch.toLowerCase().trim();
    return this.patients.filter(p =>
      p.name.toLowerCase().includes(q) ||
      p.reason.toLowerCase().includes(q)
    );
  }
  patientConsultations: ConsultationResponse[] = [];
  loadingPatientConsultations = false;
  patientMedicalRecord: MedicalRecord | null = null;
  ficheCurrentPage = 0;

  // Pages de RDV calculées dynamiquement selon la hauteur réelle des blocs
  @ViewChild('measureContainer') measureContainer?: ElementRef<HTMLElement>;
  computedPages: { items: ConsultationResponse[]; start: number }[] = [];
  private lastPaginationSignature = '';

  get totalFichePages(): number {
    // Page 0 = couverture, page 1 = données patient, pages 2+ = RDVs
    return 2 + this.computedPages.length;
  }

  get totalRdv(): number {
    return this.patientConsultations.length;
  }

  ngAfterViewChecked(): void {
    this.paginateByHeight();
  }

  /**
   * Répartit les blocs de RDV sur des pages A4 en fonction de leur hauteur
   * réelle mesurée, pour afficher le maximum de blocs lisibles par page.
   */
  private paginateByHeight(): void {
    const container = this.measureContainer?.nativeElement;
    const consultations = this.patientConsultations;

    if (!container || consultations.length === 0) {
      if (this.computedPages.length) {
        this.computedPages = [];
        this.lastPaginationSignature = '';
      }
      return;
    }

    const blocks = Array.from(container.querySelectorAll<HTMLElement>('.measure-block'));
    // Les blocs ne sont pas encore rendus (ou pas à jour) : on attend le prochain cycle
    if (blocks.length !== consultations.length) return;

    // Signature de l'état courant : on ne recalcule que si quelque chose a changé
    const signature = [
      consultations.map(c => c.id).join(','),
      Array.from(this.prescriptionItemsMap.keys()).sort((a, b) => a - b).join(','),
      blocks.map(b => Math.round(b.getBoundingClientRect().height)).join(','),
      Math.round(container.getBoundingClientRect().width)
    ].join('|');
    if (signature === this.lastPaginationSignature) return;
    this.lastPaginationSignature = signature;

    // Hauteur A4 utile (px) : largeur mesurée × ratio 297/210 − paddings verticaux
    const styles = getComputedStyle(container);
    const padTop = parseFloat(styles.paddingTop) || 0;
    const padBottom = parseFloat(styles.paddingBottom) || 0;
    const outerWidth = container.getBoundingClientRect().width;
    const pageContentHeight = (outerWidth * 297 / 210) - padTop - padBottom;

    const TITLE_HEIGHT = 46; // titre « Rendez-vous » sur la 1re page de RDV
    const GAP = 12;          // espacement inter-blocs

    const pages: { items: ConsultationResponse[]; start: number }[] = [];
    let current: ConsultationResponse[] = [];
    let used = 0;
    let startIndex = 0;
    let limit = pageContentHeight - TITLE_HEIGHT; // 1re page RDV : moins le titre

    blocks.forEach((el, i) => {
      const h = el.getBoundingClientRect().height + GAP;
      if (current.length > 0 && used + h > limit) {
        pages.push({ items: current, start: startIndex });
        startIndex += current.length;
        current = [];
        used = 0;
        limit = pageContentHeight; // pages suivantes : pleine hauteur
      }
      current.push(consultations[i]);
      used += h;
    });
    if (current.length) pages.push({ items: current, start: startIndex });

    // Mise à jour hors du cycle de détection courant pour éviter
    // ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => {
      this.computedPages = pages;
      if (this.ficheCurrentPage > this.totalFichePages - 1) {
        this.ficheCurrentPage = this.totalFichePages - 1;
      }
    });
  }

  goToFichePage(index: number): void {
    this.ficheCurrentPage = Math.max(0, Math.min(index, this.totalFichePages - 1));
  }

  nextFichePage(): void {
    this.goToFichePage(this.ficheCurrentPage + 1);
  }

  prevFichePage(): void {
    this.goToFichePage(this.ficheCurrentPage - 1);
  }
  doctors: Doctor[] = [];
  prescriptionItemsMap: Map<number, PrescriptionItemResponse[]> = new Map();
  prescriptionLoadingMap: Map<number, boolean> = new Map();
  medicalLoading = false;
  today = new Date();

  // ── Comparaison RDVs ──────────────────────────────────────────────────
  compareMode = false;
  compareDoctorId: number | null = null;

  get availableCompareDoctors(): { id: number; name: string }[] {
    const seen = new Set<number>();
    const result: { id: number; name: string }[] = [];
    for (const c of this.patientConsultations) {
      if (!c.doctorId || seen.has(c.doctorId)) continue;
      seen.add(c.doctorId);
      result.push({ id: c.doctorId, name: this.getDoctorName(c.doctorId) });
    }
    return result;
  }

  get compareConsultations(): ConsultationResponse[] {
    if (!this.compareDoctorId) return [];
    return this.patientConsultations.filter(c => c.doctorId === this.compareDoctorId);
  }

  get compareLast(): ConsultationResponse | null {
    return this.compareConsultations.at(-1) || null;
  }

  get comparePrev(): ConsultationResponse | null {
    return this.compareConsultations.at(-2) || null;
  }

  toggleCompareMode(): void {
    this.compareMode = !this.compareMode;
    if (!this.compareMode) {
      this.compareDoctorId = null;
    } else {
      const avail = this.availableCompareDoctors;
      this.compareDoctorId = avail.length ? avail[0].id : null;
    }
  }

  onCompareDoctorChange(doctorId: number): void {
    this.compareDoctorId = Number(doctorId);
  }

  // ── Agenda ──────────────────────────────────────────────────────────────
  allAppointments: AgendaAppointment[] = [];
  loadingAppointments = false;
  actionLoading: { [id: number]: boolean } = {};
  consultationsByAppointment: Map<number, ConsultationResponse> = new Map();
  loadingConsultations = false;
  consultExpanded: Set<number> = new Set();

  // Calendrier
  calendarYear = 0;
  calendarMonth = 0;        // 0-based (JS)
  calendarWeekdays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  calendarDays: CalendarDay[] = [];
  selectedDate: Date | null = null;

  // Filtres
  statusFilter: 'ALL' | 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' = 'ALL';
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

  // ── Bilans ──────────────────────────────────────────────────────────────
  doctorBilans: BilanSummary[] = [];
  filteredDoctorBilans: BilanSummary[] = [];
  doctorBilanSearchQuery = '';
  doctorBilanSortOrder: 'recent' | 'ancien' = 'recent';
  selectedDoctorBilan: ScanBilanResponse | null = null;
  loadingDoctorBilans = false;
  loadingDoctorBilanDetail = false;
  reviewUpdating: Map<string, boolean> = new Map();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private appointmentService: AppointmentService,
    private consultationService: ConsultationService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    private prescriptionService: PrescriptionService,
    private bilanService: BilanService
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
      if (this.section === 'labs') {
        this.loadDoctorBilans();
      }
    });
  }

  // ── Actions patients ─────────────────────────────────────────────────────

  get patientDetails(): PatientListDto | undefined {
    return this.registeredPatients.find(p => p.id === this.selectedPatient?.id);
  }

  selectPatient(patient: any): void {
    if (this.selectedPatient?.id === patient.id) {
      this.backToPatientList();
      return;
    }
    this.selectedPatient = patient;
    this.loadPatientConsultations(patient.id);
    this.loadPatientMedicalRecord(patient.id);
  }

  backToPatientList(): void {
    this.selectedPatient = null;
    this.patientConsultations = [];
    this.patientMedicalRecord = null;
    this.prescriptionItemsMap.clear();
    this.computedPages = [];
    this.lastPaginationSignature = '';
    this.ficheCurrentPage = 0;
  }

  exportPDF(): void {
    const ficheEl = document.querySelector<HTMLElement>('.fiche-patient');
    if (!ficheEl) return;

    const savedPage = this.ficheCurrentPage;
    this.ficheCurrentPage = 0;

    const clone = ficheEl.cloneNode(true) as HTMLElement;

    const sheets = clone.querySelectorAll<HTMLElement>('.fiche-sheet');
    sheets.forEach(p => p.style.display = 'block');

    const pagination = clone.querySelector<HTMLElement>('.fiche-pagination');
    if (pagination) pagination.style.display = 'none';

    const measure = clone.querySelector<HTMLElement>('.fiche-measure');
    if (measure) measure.remove();

    const actionBtns = clone.querySelectorAll<HTMLElement>('.appt-btn');
    actionBtns.forEach(b => { b.style.display = 'none'; });

    const name = this.selectedPatient?.name?.replace(/\s+/g, '_') || 'patient';
    const html = `<html><head><title>Fiche_${name}</title><style>
      @page { margin: 15mm; size: A4; }
      * { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; color-adjust: exact !important; }
      body { margin: 0; padding: 0; font-family: 'Inter', 'Segoe UI', Arial, Helvetica, sans-serif; color: #1a2b3c; font-size: 11pt; line-height: 1.4; background: #f5f0e8; }

      .page-header, .patients-list-panel, .back-link, .appt-btn, .compare-container,
      .loading-state, .fiche-empty, .fiche-pagination { display: none !important; }

      .patients-layout.has-selected { display: block !important; width: 100% !important; margin: 0 !important; padding: 0 !important; }
      .patients-fiche-panel { animation: none !important; opacity: 1 !important; transform: none !important; width: 100% !important; }

      .fiche-patient { box-shadow: none !important; border: none !important; border-radius: 0 !important; padding: 0 !important; max-width: 100% !important; width: 100% !important; background: transparent !important; }
      .fiche-patient-inner { padding: 0 !important; border-left: none !important; }
      .fiche-sheets { display: block !important; margin: 0 !important; }
      .fiche-sheets-stack { display: block !important; }

      .fiche-sheet {
        display: block !important; position: static !important;
        width: auto !important; min-height: 267mm !important;
        padding: 20mm 25mm !important; margin: 0 auto !important;
        background: #f5f0e8 !important; border: 1px solid #d5cdbd !important;
        border-radius: 4px !important; box-shadow: none !important;
        aspect-ratio: auto !important; height: auto !important;
        overflow: visible !important; page-break-after: always;
        box-sizing: border-box !important;
      }
      .fiche-sheet:last-child { page-break-after: auto; }

      .fiche-cover-page { background: #f5f0e8 !important; display: flex !important; align-items: center !important; justify-content: center !important; }
      .fiche-cover { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: auto !important; padding: 24px 0; gap: 10px; }
      .fiche-cover-logos { display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px; }
      .fiche-cover-emblem { display: flex; flex-direction: column; align-items: center; gap: 6px; }
      .fiche-cover-emblem-label { font-size: 8pt; font-weight: 700; color: #6a5f4e; text-align: center; line-height: 1.3; letter-spacing: .06em; text-transform: uppercase; }
      .fiche-cover-brand { display: flex; flex-direction: column; align-items: center; gap: 4px; flex: 1; text-align: center; }
      .fiche-cover-brand-name { font-family: 'Georgia', 'Playfair Display', serif; font-size: 26pt; font-weight: 700; color: #1a2b3c; letter-spacing: .04em; }
      .fiche-cover-brand-sub { font-size: 11pt; font-weight: 600; color: #6a5f4e; letter-spacing: .12em; text-transform: uppercase; }
      .fiche-cover-divider { display: flex; align-items: center; gap: 14px; width: 60%; margin: 14px auto; }
      .fiche-cover-divider-line { flex: 1; height: 1px; background: #6a5f4e; opacity: .35; }
      .fiche-cover-divider-diamond { color: #c9953a; font-size: 14pt; }
      .fiche-cover-patient { display: flex; flex-direction: column; align-items: center; gap: 6px; margin: 4px 0; }
      .fiche-cover-patient-label { font-size: 9pt; font-weight: 600; color: #6a5f4e; letter-spacing: .15em; text-transform: uppercase; }
      .fiche-cover-patient-name { margin: 0; font-family: 'Georgia', 'Playfair Display', serif; font-size: 22pt; font-weight: 700; color: #1a2b3c; text-align: center; word-break: break-word; }
      .fiche-cover-footer { display: flex; flex-direction: column; align-items: center; gap: 5px; margin-top: auto; padding-top: 14px; }
      .fiche-cover-footer-line { display: block; width: 40px; height: 1px; background: #6a5f4e; opacity: .2; }
      .fiche-cover-footer-line:nth-child(2) { width: 28px; }
      .fiche-cover-footer-line:nth-child(3) { width: 16px; }

      .fiche-page-title { font-family: 'Georgia', 'Playfair Display', serif; font-size: 18pt; font-weight: 700; color: #1a2b3c; border-bottom: 2px solid #c8bfab; padding-bottom: 6px; margin: 0 0 14px; }
      .fiche-rdv-summary { font-size: 10pt; font-weight: 600; color: #6a5f4e; text-align: right; margin: 12px 0 0; }
      .fiche-empty-note { font-size: 12pt; font-style: italic; color: #6a5f4e; }

      .fiche-identity-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 6px; }
      .fiche-identity-field { display: flex; flex-direction: column; gap: 1px; padding: 4px 8px; border-radius: 3px; background: #f2ede4; }
      .fiche-identity-label { font-size: 8pt; font-weight: 600; color: #6a5f4e; text-transform: uppercase; letter-spacing: .05em; }
      .fiche-identity-value { font-size: 12pt; font-weight: 500; color: #1a2b3c; line-height: 1.3; }

      .fiche-rdv, .fiche-rdv-fullwidth {
        break-inside: avoid; page-break-inside: avoid;
        margin-bottom: 8px; border: 1px solid #d5cdbd;
        border-radius: 4px; border-left: 4px solid #1a2b3c;
        background: #faf8f5; overflow: hidden;
      }
      .fiche-rdv.status-completed, .fiche-rdv-fullwidth.status-completed { border-left-color: #0d7a6e; }
      .fiche-rdv.status-progress, .fiche-rdv-fullwidth.status-progress { border-left-color: #2b6f9e; }
      .fiche-rdv.status-pending, .fiche-rdv-fullwidth.status-pending { border-left-color: #c9953a; }
      .fiche-rdv.status-cancelled, .fiche-rdv-fullwidth.status-cancelled { border-left-color: #c0392b; }

      .fiche-rdv-header { padding: 8px 12px; background: #efe9df; border-bottom: 1px solid #d5cdbd; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
      .fiche-rdv-num { font-size: 14pt; font-weight: 700; color: #1a2b3c; text-transform: uppercase; letter-spacing: .04em; }
      .fiche-rdv-date { font-size: 12pt; font-weight: 500; color: #6a5f4e; }
      .fiche-rdv-doctor { font-size: 12pt; font-weight: 500; color: #0d7a6e; margin-left: auto; }

      .fiche-rdv-content { padding: 10px 12px; display: grid; grid-template-columns: 1fr 1fr; gap: 6px; font-size: 12pt; color: #1a2b3c; line-height: 1.5; }
      .fiche-field-full { grid-column: 1 / -1; }
      .fiche-field-card { background: #f2ede4; border: 1px solid #dcd4c2; border-radius: 3px; padding: 6px 8px; }
      .fiche-field-label { font-size: 9pt; font-weight: 600; color: #6a5f4e; text-transform: uppercase; letter-spacing: .03em; display: block; margin-bottom: 2px; }
      .fiche-field-text { font-size: 12pt; font-weight: 500; color: #1a2b3c; line-height: 1.4; display: block; padding: 1px 0; }

      .fiche-vitals { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 2px; }
      .vital-item { font-size: 11pt; font-weight: 500; color: #1a2b3c; padding: 2px 8px; background: #faf8f5; border: 1px solid #d5cdbd; border-radius: 3px; display: inline-flex; align-items: center; gap: 3px; line-height: 1.4; }

      .prescription-meds ul { margin: 4px 0 0; padding: 0; list-style: none; }
      .prescription-meds ul li { font-size: 11pt; font-weight: 500; color: #1a2b3c; line-height: 1.5; padding: 2px 0 2px 14px; position: relative; break-inside: avoid; }
      .prescription-meds ul li::before { content: '💊'; position: absolute; left: 0; top: 2px; font-size: 9pt; }
      .prescription-meds ul li + li { border-top: 1px dashed #d5cdbd; margin-top: 1px; padding-top: 3px; }

      .fiche-measure { display: none !important; }
      body { -webkit-print-color-adjust: exact; print-color-adjust: exact; color-adjust: exact; }
    </style></head><body>${clone.outerHTML}</body></html>`;

    this.ficheCurrentPage = savedPage;

    const win = window.open('', '_blank');
    if (win) {
      win.document.write(html);
      win.document.close();
      setTimeout(() => { win.print(); }, 300);
    }
  }

  private inlineStyles(source: HTMLElement, target: HTMLElement): void {
    const computed = getComputedStyle(source);
    const skipProps = ['animation', 'transition', 'content', 'cursor', 'user-select'];
    for (let i = 0; i < computed.length; i++) {
      const prop = computed[i];
      if (skipProps.some(p => prop.startsWith(p))) continue;
      target.style.setProperty(prop, computed.getPropertyValue(prop), computed.getPropertyPriority(prop));
    }
    if (source.children.length && target.children.length) {
      for (let i = 0; i < source.children.length; i++) {
        const s = source.children[i] as HTMLElement;
        const t = target.children[i] as HTMLElement;
        if (s && t) this.inlineStyles(s, t);
      }
    }
  }

  private loadPatientConsultations(patientId: number): void {
    this.loadingPatientConsultations = true;
    this.patientConsultations = [];
    this.prescriptionItemsMap.clear();
    this.prescriptionLoadingMap.clear();
    this.computedPages = [];
    this.lastPaginationSignature = '';
    this.ficheCurrentPage = 0;
    this.consultationService.getPatientConsultations(patientId).subscribe({
      next: (data) => {
        // On n'affiche que les RDV dont la consultation est terminée,
        // triés du premier au dernier (ordre chronologique croissant).
        const completed = data
          .filter(c => c.status === 'COMPLETED')
          .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
        this.patientConsultations = completed;
        completed.forEach(c => {
          this.prescriptionLoadingMap.set(c.id, true);
          if (c.prescriptionId) {
            this.prescriptionService.getPrescription(c.prescriptionId).subscribe({
              next: (p) => {
        if (p?.items?.length) {
                  this.prescriptionItemsMap.set(c.id, p.items);
                }
                this.prescriptionLoadingMap.set(c.id, false);
              },
              error: () => {
                this.prescriptionLoadingMap.set(c.id, false);
              }
            });
          } else {
            this.loadPrescriptionByConsultation(c.id);
          }
        });
        this.loadingPatientConsultations = false;
      },
      error: () => {
        this.loadingPatientConsultations = false;
      }
    });
  }

  private loadPrescriptionByConsultation(consultationId: number): void {
    this.prescriptionLoadingMap.set(consultationId, true);
    this.prescriptionService.getPrescriptionByConsultation(consultationId).subscribe({
      next: (p) => {
        if (p?.items?.length) {
          this.prescriptionItemsMap.set(consultationId, p.items);
        }
        this.prescriptionLoadingMap.set(consultationId, false);
      },
      error: () => {
        this.prescriptionLoadingMap.set(consultationId, false);
      }
    });
  }

  private loadPatientMedicalRecord(patientId: number): void {
    this.medicalLoading = true;
    this.patientMedicalRecord = null;
    this.patientService.getPatientMedicalRecord(patientId).subscribe({
      next: (record) => {
        this.patientMedicalRecord = record;
        this.medicalLoading = false;
      },
      error: () => {
        this.medicalLoading = false;
      }
    });
  }

  private loadDoctors(): void {
    this.doctorService.getAllDoctors().subscribe({
      next: (data) => {
        this.doctors = data;
      },
      error: () => {}
    });
  }

  getDoctorName(doctorId: number): string {
    const doctor = this.doctors.find(d => d.id === doctorId);
    return doctor ? `Dr ${doctor.firstName} ${doctor.lastName}` : `Dr #${doctorId}`;
  }

  // ── Chargement données ────────────────────────────────────────────────────

  loadPatients(): void {
    this.loadDoctors();
    this.authService.getAllPatients().subscribe({
      next: (data) => {
        this.registeredPatients = data;
        this.patients = data.map(p => ({
          id: p.id,
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

  private loadConsultations(): void {
    this.loadingConsultations = true;
    this.consultationService.getAllConsultations().subscribe({
      next: (consultations) => {
        this.consultationsByAppointment.clear();
        consultations.forEach(c => {
          if (c.appointmentId) {
            this.consultationsByAppointment.set(c.appointmentId, c);
          }
        });
        this.loadingConsultations = false;
      },
      error: () => {
        this.loadingConsultations = false;
      }
    });
  }

  loadAppointments(): void {
    this.loadingAppointments = true;
    this.appointmentService.getDoctorAppointments().subscribe({
      next: (data) => {
        this.allAppointments = data.map(app => this.mapAppointment(app));
        this.buildCalendar();
        this.loadingAppointments = false;
        this.loadConsultations();
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
      patientId: app.patientId,
      patient: this.getPatientName(app.patientId),
      reason: app.notes || 'Consultation générale',
      date: this.formatDate(app.dateTime),
      hour: this.formatHour(app.dateTime),
      mode: app.mode === 'TELECONSULTATION' ? 'Téléconsultation' : 'Présentiel',
      modeIcon: app.mode === 'TELECONSULTATION' ? '💻' : '🏥',
      rawMode: app.mode,
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
        COMPLETED: 'status-completed',
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

  startConsultation(appt: AgendaAppointment): void {
    if (!confirm(`Démarrer la consultation pour ${appt.patient} ?`)) return;
    this.actionLoading[appt.id] = true;
    const request: ConsultationRequest = {
      patientId: appt.patientId,
      appointmentId: appt.id,
      reason: appt.reason,
      type: appt.rawMode === 'TELECONSULTATION' ? 'TELECONSULTATION' : 'PRESENTIEL'
    };
    this.consultationService.startConsultation(request).subscribe({
      next: () => {
        this.actionLoading[appt.id] = false;
        this.router.navigate(['/dashboard/doctor/consultations']);
      },
      error: (err) => {
        console.error('Erreur création consultation', err);
        this.actionLoading[appt.id] = false;
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

  getConsultationForAppointment(appointmentId: number): ConsultationResponse | undefined {
    return this.consultationsByAppointment.get(appointmentId);
  }

  toggleConsultExpand(consultationId: number): void {
    if (this.consultExpanded.has(consultationId)) {
      this.consultExpanded.delete(consultationId);
    } else {
      this.consultExpanded.add(consultationId);
    }
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

  isAppointmentToday(appt: AgendaAppointment): boolean {
    try {
      const today = new Date();
      const apptDate = new Date(appt.rawDateTime);
      return apptDate.getFullYear() === today.getFullYear()
        && apptDate.getMonth() === today.getMonth()
        && apptDate.getDate() === today.getDate();
    } catch {
      return false;
    }
  }

  get medicalHeight(): string {
    return this.patientMedicalRecord?.height ? this.patientMedicalRecord.height + ' cm' : '—';
  }

  get medicalWeight(): string {
    return this.patientMedicalRecord?.weight ? this.patientMedicalRecord.weight + ' kg' : '—';
  }

  get insuranceLabel(): string {
    const company = this.patientMedicalRecord?.insuranceCompany;
    const number = this.patientMedicalRecord?.insuranceNumber;
    if (company && number) return company + ' — ' + number;
    if (company) return company;
    if (number) return number;
    return 'Non renseignée';
  }

  getConsultationStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'En attente';
      case 'IN_PROGRESS': return 'En cours';
      case 'COMPLETED': return 'Terminée';
      case 'CANCELLED': return 'Annulée';
      default: return status;
    }
  }

  getConsultationStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'IN_PROGRESS': return 'status-progress';
      case 'COMPLETED': return 'status-completed';
      case 'CANCELLED': return 'status-cancelled';
      default: return '';
    }
  }

  getStatusLabel(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'CONFIRMED': return 'Confirmé';
      case 'CANCELLED': return 'Annulé';
      case 'COMPLETED': return 'Terminé';
      case 'PENDING': default: return 'En attente';
    }
  }

  getStatusClass(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'CONFIRMED': return 'status-confirmed';
      case 'CANCELLED': return 'status-cancelled';
      case 'COMPLETED': return 'status-completed';
      case 'PENDING': default: return 'status-pending';
    }
  }

  // ── Bilans ──────────────────────────────────────────────────────────────

  loadDoctorBilans(): void {
    this.loadingDoctorBilans = true;
    this.selectedDoctorBilan = null;
    this.bilanService.getDoctorBilans().subscribe({
      next: (data) => {
        this.doctorBilans = data.content;
        this.applyDoctorBilanFilters();
        this.loadingDoctorBilans = false;
      },
      error: (err) => {
        console.error('Erreur chargement bilans docteur', err);
        this.loadingDoctorBilans = false;
      }
    });
  }

  applyDoctorBilanFilters(): void {
    let list = [...this.doctorBilans];

    if (this.doctorBilanSearchQuery.trim()) {
      const q = this.doctorBilanSearchQuery.trim().toLowerCase();
      list = list.filter(b =>
        (b.typeBilan || '').toLowerCase().includes(q) ||
        this.getPatientNameById(b.patientId).toLowerCase().includes(q)
      );
    }

    if (this.doctorBilanSortOrder === 'recent') {
      list.sort((a, b) => new Date(b.dateBilan).getTime() - new Date(a.dateBilan).getTime());
    } else {
      list.sort((a, b) => new Date(a.dateBilan).getTime() - new Date(b.dateBilan).getTime());
    }

    this.filteredDoctorBilans = list;
    if (this.selectedDoctorBilan && !this.filteredDoctorBilans.find(b => b.id === this.selectedDoctorBilan!.id)) {
      this.selectedDoctorBilan = null;
    }
  }

  selectDoctorBilan(id: string): void {
    this.loadingDoctorBilanDetail = true;
    this.selectedDoctorBilan = null;
    this.bilanService.getBilanForDoctor(id).subscribe({
      next: (data) => {
        this.selectedDoctorBilan = data;
        this.loadingDoctorBilanDetail = false;
      },
      error: (err) => {
        console.error('Erreur chargement détail bilan', err);
        this.loadingDoctorBilanDetail = false;
      }
    });
  }

  deselectDoctorBilan(): void {
    this.selectedDoctorBilan = null;
  }

  updateReviewStatus(bilanId: string, newStatus: string): void {
    this.reviewUpdating.set(bilanId, true);
    this.bilanService.updateReviewStatus(bilanId, newStatus).subscribe({
      next: () => {
        const idx = this.doctorBilans.findIndex(b => b.id === bilanId);
        if (idx !== -1) {
          this.doctorBilans[idx] = { ...this.doctorBilans[idx], reviewStatus: newStatus };
        }
        if (this.selectedDoctorBilan?.id === bilanId) {
          this.selectedDoctorBilan = { ...this.selectedDoctorBilan, reviewStatus: newStatus };
        }
        this.reviewUpdating.set(bilanId, false);
      },
      error: (err) => {
        console.error('Erreur mise à jour statut review', err);
        this.reviewUpdating.set(bilanId, false);
      }
    });
  }

  getDoctorReviewStatusLabel(status: string): string {
    switch (status) {
      case 'LU': return 'Consulté';
      case 'TRAITE': return 'Traité';
      case 'EN_ATTENTE':
      default: return 'En attente';
    }
  }

  getDoctorReviewStatusClass(status: string): string {
    switch (status) {
      case 'LU': return 'review-lu';
      case 'TRAITE': return 'review-traite';
      case 'EN_ATTENTE':
      default: return 'review-en-attente';
    }
  }

  getBilanStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'En attente';
      case 'CONFIRMED': return 'Confirmé';
      default: return status;
    }
  }

  getBilanStatusClass(status: string): string {
    switch (status) {
      case 'CONFIRMED': return 'status-confirmed';
      case 'PENDING':
      default: return 'status-pending';
    }
  }

  getResultStatusLabel(status: string): string {
    switch (status) {
      case 'NORMAL': return 'Normal';
      case 'ANORMAL': return 'Anormal';
      case 'CRITIQUE': return 'Critique';
      default: return 'N/A';
    }
  }

  getResultStatusClass(status: string): string {
    switch (status) {
      case 'NORMAL': return 'result-normal';
      case 'ANORMAL': return 'result-anormal';
      case 'CRITIQUE': return 'result-critique';
      default: return 'result-na';
    }
  }

  getPatientNameById(patientId: number): string {
    return this.getPatientName(patientId);
  }
}
