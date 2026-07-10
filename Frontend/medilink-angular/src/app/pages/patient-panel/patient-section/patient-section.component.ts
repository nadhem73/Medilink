import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService, DoctorWithProfile } from '../../../core/services/doctor.service';
import { AppointmentService, AppointmentDto, AppointmentRequest } from '../../../core/services/appointment.service';
import { PrescriptionService, PrescriptionResponse } from '../../../core/services/prescription.service';

type PatientSectionKey = 'appointments' | 'prescriptions' | 'labs' | 'profile';

@Component({
  selector: 'app-patient-section',
  templateUrl: './patient-section.component.html',
  styleUrls: ['./patient-section.component.scss']
})
export class PatientSectionComponent implements OnInit {
  section: PatientSectionKey = 'appointments';
  title = '';
  currentUser: any;

  // Dynamic Data
  appointments: AppointmentDto[] = [];
  doctors: DoctorWithProfile[] = [];
  filteredDoctors: DoctorWithProfile[] = [];
  specialties: string[] = [];
  selectedSpecialty: string = '';

  // UI state
  activeTab: 'list' | 'book' = 'list';
  selectedDoctor: DoctorWithProfile | null = null;
  loadingAppointments = false;
  loadingDoctors = false;
  submitting = false;
  bookedDoctorIds: number[] = []; // IDs des médecins avec RDV déjà pris
  searchQuery: string = '';

  // Booking form model
  bookingDate: string = '';
  bookingTime: string = '';
  bookingMode: string = 'PRESENTIEL';
  bookingNotes: string = '';

  // Available time slots
  availableSlots: string[] = [];
  loadingSlots = false;

  // Notifications
  successMessage: string = '';
  errorMessage: string = '';

  prescriptions: PrescriptionResponse[] = [];
  filteredPrescriptions: PrescriptionResponse[] = [];
  pagedPrescriptions: PrescriptionResponse[] = [];
  archivedPrescriptions: PrescriptionResponse[] = [];
  loadingPrescriptions = false;
  selectedPrescription: PrescriptionResponse | null = null;
  showArchiveModal = false;

  prescSearchQuery = '';
  prescSortOrder: 'recent' | 'ancien' = 'recent';
  prescPage = 0;
  prescPageSize = 5;

  labResults = [
    {
      exam: 'Bilan lipidique',
      lab: 'Centre de biologie El Menzah',
      date: '08 juin 2026',
      result: 'Disponible',
      note: 'LDL en baisse, controle recommande dans 3 mois.'
    },
    {
      exam: 'Glycemie a jeun',
      lab: 'Laboratoire Charles Nicolle',
      date: '06 juin 2026',
      result: 'Disponible',
      note: 'Valeurs dans la norme.'
    },
    {
      exam: 'NFS complete',
      lab: 'Laboratoire Pasteur',
      date: 'Aujourd hui',
      result: 'En attente',
      note: 'Publication estimee avant 18:00.'
    }
  ];

  profileCards: { label: string; value: string }[] = [];

  profileChecklist = [
    'Verifier les informations de contact avant chaque rendez-vous.',
    'Televerser les analyses importantes dans votre dossier.',
    'Mettre a jour les traitements et allergies apres chaque consultation.'
  ];

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private doctorService: DoctorService,
    private appointmentService: AppointmentService,
    private prescriptionService: PrescriptionService
  ) {
    this.currentUser = this.authService.getCurrentUser();
    this.profileCards = [
      { label: 'Nom complet', value: `${this.currentUser?.firstName || 'Patient'} ${this.currentUser?.lastName || ''}`.trim() },
      { label: 'Email', value: this.currentUser?.email || 'Non renseigne' },
      { label: 'Telephone', value: this.currentUser?.phone || 'Non renseigne' },
      { label: 'Adresse', value: this.currentUser?.address || 'Non renseignee' },
      { label: 'Date de naissance', value: this.currentUser?.birthDate || 'Non renseignee' },
      { label: 'Genre', value: this.currentUser?.gender || 'Non renseigne' }
    ];
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.section = data['section'] as PatientSectionKey;
      this.title = data['title'] as string;
      
      if (this.section === 'appointments') {
        this.loadAppointments();
        this.loadDoctors();
      } else if (this.section === 'prescriptions') {
        this.loadPrescriptions();
        this.loadDoctors();
      }
    });
  }

  // Load appointments from backend
  loadAppointments(): void {
    this.loadingAppointments = true;
    this.appointmentService.getMyAppointments().subscribe({
      next: (data) => {
        this.appointments = data.filter(a => a.status !== 'COMPLETED');
        this.loadingAppointments = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des rendez-vous', err);
        this.loadingAppointments = false;
      }
    });
  }

  // Load prescriptions from backend
  loadPrescriptions(): void {
    const patientId = this.currentUser?.id;
    if (!patientId) return;
    this.loadingPrescriptions = true;
    this.prescriptionService.getPrescriptionsByPatient(patientId).subscribe({
      next: (data) => {
        this.archivedPrescriptions = data.filter(p => p.status === 'ARCHIVEE');
        this.prescriptions = data.filter(p => p.status !== 'ARCHIVEE');
        this.prescPage = 0;
        this.applyFiltersAndPagination();
        this.loadingPrescriptions = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des ordonnances', err);
        this.loadingPrescriptions = false;
      }
    });
  }

  selectPrescription(prescription: PrescriptionResponse): void {
    this.selectedPrescription = prescription;
  }

  deselectPrescription(): void {
    this.selectedPrescription = null;
  }

  archivePrescription(id: number): void {
    if (!confirm('Archiver cette ordonnance ?')) return;
    this.prescriptionService.updateStatus(id, 'ARCHIVEE').subscribe({
      next: () => {
        const archived = this.prescriptions.find(p => p.id === id);
        if (archived) {
          archived.status = 'ARCHIVEE';
          this.archivedPrescriptions.unshift(archived);
          this.prescriptions = this.prescriptions.filter(p => p.id !== id);
        }
        if (this.selectedPrescription?.id === id) {
          this.selectedPrescription = null;
        }
      },
      error: (err) => {
        console.error('Erreur lors de l\'archivage', err);
      }
    });
  }

  applyFiltersAndPagination(): void {
    let list = [...this.prescriptions];

    if (this.prescSearchQuery.trim()) {
      const q = this.prescSearchQuery.trim().toLowerCase();
      list = list.filter(p =>
        p.id.toString().includes(q) ||
        this.getDoctorDetails(p.doctorId).name.toLowerCase().includes(q)
      );
    }

    if (this.prescSortOrder === 'recent') {
      list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    } else {
      list.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }

    this.filteredPrescriptions = list;
    this.prescPage = 0;
    this.updatePage();
  }

  updatePage(): void {
    const start = this.prescPage * this.prescPageSize;
    this.pagedPrescriptions = this.filteredPrescriptions.slice(start, start + this.prescPageSize);
  }

  get prescTotalPages(): number {
    return Math.ceil(this.filteredPrescriptions.length / this.prescPageSize) || 1;
  }

  goToPrescPage(page: number): void {
    if (page < 0 || page >= this.prescTotalPages) return;
    this.prescPage = page;
    this.updatePage();
    this.selectedPrescription = null;
  }

  openArchiveModal(): void {
    this.showArchiveModal = true;
  }

  closeArchiveModal(): void {
    this.showArchiveModal = false;
  }

  getPrescriptionStatusClass(status: string): string {
    switch (status.toUpperCase()) {
      case 'SOUMISE': return 'status-pending';
      case 'EN_PREPARATION': return 'status-pending';
      case 'PREPAREE': return 'status-confirmed';
      case 'RETIREE': return 'status-confirmed';
      case 'DISPENSEE': return 'status-confirmed';
      case 'ANNULEE': return 'status-cancelled';
      case 'ARCHIVEE': return 'status-archived';
      case 'BROUILLON':
      default: return 'status-pending';
    }
  }

  getPrescriptionStatusLabel(status: string): string {
    switch (status.toUpperCase()) {
      case 'BROUILLON': return 'Brouillon';
      case 'SOUMISE': return 'Soumise';
      case 'EN_PREPARATION': return 'En préparation';
      case 'PREPAREE': return 'Préparée';
      case 'RETIREE': return 'Retirée';
      case 'DISPENSEE': return 'Dispensée';
      case 'ANNULEE': return 'Annulée';
      case 'ARCHIVEE': return 'Archivée';
      default: return status;
    }
  }

  canArchive(status: string): boolean {
    return status.toUpperCase() === 'DISPENSEE';
  }

  // Load doctors from backend
  loadDoctors(): void {
    this.loadingDoctors = true;
    this.doctorService.getDoctorsWithProfiles().subscribe({
      next: (data) => {
        this.doctors = data;
        this.filteredDoctors = data;
        
        // Extract unique specialties for filtering
        const specs = data.map(d => d.specialty).filter(s => !!s);
        this.specialties = Array.from(new Set(specs));
        
        // Charger les médecins déjà réservés par ce patient
        this.loadBookedDoctorIds();
        
        this.loadingDoctors = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des medecins', err);
        this.loadingDoctors = false;
      }
    });
  }

  // Charge les IDs des médecins ayant déjà un rendez-vous actif
  loadBookedDoctorIds(): void {
    this.appointmentService.getActiveDoctorIds().subscribe({
      next: (ids) => {
        this.bookedDoctorIds = ids;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des médecins réservés', err);
        this.bookedDoctorIds = [];
      }
    });
  }

  // Vérifie si un médecin a déjà un rendez-vous actif
  isDoctorBooked(doctorId: number): boolean {
    return this.bookedDoctorIds.includes(doctorId);
  }

  // Filter doctors list by specialty and search query
  filterDoctors(): void {
    const query = this.searchQuery.toLowerCase().trim();
    this.filteredDoctors = this.doctors.filter(d => {
      const matchesSpecialty = !this.selectedSpecialty || d.specialty === this.selectedSpecialty;
      const matchesSearch = !query
        || `${d.firstName} ${d.lastName}`.toLowerCase().includes(query)
        || (d.specialty || '').toLowerCase().includes(query)
        || (d.hospital || '').toLowerCase().includes(query)
        || (d.biography || '').toLowerCase().includes(query);
      return matchesSpecialty && matchesSearch;
    });
  }

  // Switch tabs
  switchTab(tab: 'list' | 'book'): void {
    this.activeTab = tab;
    this.successMessage = '';
    this.errorMessage = '';
    if (tab === 'list') {
      this.selectedDoctor = null;
      this.loadAppointments();
    }
  }

  // Choose a doctor for booking
  selectDoctor(doctor: DoctorWithProfile): void {
    this.selectedDoctor = doctor;
    // Set default values for booking
    this.bookingDate = '';
    this.bookingTime = '';
    this.bookingMode = 'PRESENTIEL';
    this.bookingNotes = '';
    this.successMessage = '';
    this.errorMessage = '';
  }

  // Cancel booking form
  cancelBooking(): void {
    this.selectedDoctor = null;
    this.availableSlots = [];
    this.bookingTime = '';
  }

  // Called when date changes to load available slots
  onDateChange(): void {
    this.bookingTime = '';
    this.availableSlots = [];
    if (!this.selectedDoctor || !this.bookingDate) return;
    this.loadingSlots = true;
    this.errorMessage = '';
    this.appointmentService.getAvailableSlots(
      this.selectedDoctor.id,
      this.bookingDate,
      this.selectedDoctor.debutMatin,
      this.selectedDoctor.finMatin,
      this.selectedDoctor.debutApresMidi,
      this.selectedDoctor.finApresMidi
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

  // Book appointment
  bookAppointment(): void {
    if (!this.selectedDoctor) return;
    if (!this.bookingDate || !this.bookingTime) {
      this.errorMessage = 'Veuillez selectionner une date et une heure.';
      return;
    }
    if (!this.bookingTime) {
      this.errorMessage = 'Veuillez sélectionner un créneau horaire.';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Create LocalDateTime ISO string format: YYYY-MM-DDTHH:mm:ss
    const dateTimeStr = `${this.bookingDate}T${this.bookingTime}:00`;

    const request: AppointmentRequest = {
      doctorId: this.selectedDoctor.id,
      dateTime: dateTimeStr,
      mode: this.bookingMode,
      notes: this.bookingNotes
    };

    this.appointmentService.createAppointment(request).subscribe({
      next: () => {
        this.successMessage = `Rendez-vous reserve avec succes chez le Dr. ${this.selectedDoctor?.firstName} ${this.selectedDoctor?.lastName}!`;
        this.submitting = false;
        setTimeout(() => {
          this.switchTab('list');
        }, 3000);
      },
      error: (err) => {
        console.error('Erreur lors de la reservation', err);
        if (err.error && typeof err.error === 'string') {
          this.errorMessage = err.error;
        } else {
          this.errorMessage = 'Une erreur est survenue lors de la reservation du rendez-vous. Veuillez reessayer.';
        }
        this.submitting = false;
      }
    });
  }

  // Helper method to resolve doctor details in appointments list
  getDoctorDetails(doctorId: number): { name: string; specialty: string } {
    const doc = this.doctors.find(d => d.id === doctorId);
    if (doc) {
      return {
        name: `Dr. ${doc.firstName} ${doc.lastName}`,
        specialty: doc.specialty || 'Medecin'
      };
    }
    return { name: `Medecin #${doctorId}`, specialty: 'General' };
  }

  // Formatter for status badge styling
  getStatusClass(status: string): string {
    switch (status.toUpperCase()) {
      case 'CONFIRMED':
        return 'status-confirmed';
      case 'CANCELLED':
        return 'status-cancelled';
      case 'PENDING':
      default:
        return 'status-pending';
    }
  }

  // Formatter for status icon
  getStatusIcon(status: string): string {
    switch (status.toUpperCase()) {
      case 'CONFIRMED':
        return '✅';
      case 'CANCELLED':
        return '❌';
      case 'PENDING':
      default:
        return '⏳';
    }
  }

  // Formatter for status text translation
  getStatusLabel(status: string): string {
    switch (status.toUpperCase()) {
      case 'CONFIRMED':
        return 'Confirmé';
      case 'CANCELLED':
        return 'Annulé';
      case 'PENDING':
      default:
        return 'En attente';
    }
  }

  // Check if appointment is upcoming
  isUpcoming(dateTime: string): boolean {
    return new Date(dateTime) > new Date();
  }

  // Count pending appointments
  getPendingCount(): number {
    return this.appointments.filter(a => a.status.toUpperCase() === 'PENDING').length;
  }
}
