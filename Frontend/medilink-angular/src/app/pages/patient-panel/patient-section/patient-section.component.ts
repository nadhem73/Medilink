import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService, DoctorWithProfile } from '../../../core/services/doctor.service';
import { AppointmentService, AppointmentDto, AppointmentRequest } from '../../../core/services/appointment.service';

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

  // Booking form model
  bookingDate: string = '';
  bookingTime: string = '';
  bookingMode: string = 'PRESENTIEL';
  bookingNotes: string = '';

  // Notifications
  successMessage: string = '';
  errorMessage: string = '';

  // Pre-existing mock data for other tabs
  prescriptions = [
    {
      medication: 'Amlodipine 5 mg',
      dosage: '1 comprime / jour',
      prescriber: 'Dr. Yasmine Ben Salem',
      renewal: 'Renouvellement dans 4 jours',
      status: 'Actif'
    },
    {
      medication: 'Vitamine D',
      dosage: '1 capsule / soir',
      prescriber: 'Dr. Ines Gharbi',
      renewal: 'Valable jusqu au 28 juin',
      status: 'Suivi'
    },
    {
      medication: 'Omeprazole 20 mg',
      dosage: 'Avant le petit-dejeuner',
      prescriber: 'Dr. Mehdi Trabelsi',
      renewal: 'Ordonnance recente',
      status: 'Nouveau'
    }
  ];

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
    private appointmentService: AppointmentService
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
      }
    });
  }

  // Load appointments from backend
  loadAppointments(): void {
    this.loadingAppointments = true;
    this.appointmentService.getMyAppointments().subscribe({
      next: (data) => {
        this.appointments = data;
        this.loadingAppointments = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des rendez-vous', err);
        this.loadingAppointments = false;
      }
    });
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

  // Filter doctors list
  onSpecialtyChange(): void {
    if (!this.selectedSpecialty) {
      this.filteredDoctors = this.doctors;
    } else {
      this.filteredDoctors = this.doctors.filter(
        d => d.specialty === this.selectedSpecialty
      );
    }
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
  }

  // Book appointment
  bookAppointment(): void {
    if (!this.selectedDoctor) return;
    if (!this.bookingDate || !this.bookingTime) {
      this.errorMessage = 'Veuillez selectionner une date et une heure.';
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
        this.errorMessage = 'Une erreur est survenue lors de la reservation du rendez-vous. Veuillez reessayer.';
        this.submitting = false;
      }
    });
  }

  // Cancel an existing appointment
  cancelAppointment(id: number): void {
    if (confirm('Etes-vous sur de vouloir annuler ce rendez-vous ?')) {
      this.appointmentService.cancelAppointment(id).subscribe({
        next: () => {
          this.loadAppointments();
        },
        error: (err) => {
          console.error('Erreur lors de lannulation', err);
          alert('Impossible d\'annuler ce rendez-vous.');
        }
      });
    }
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
