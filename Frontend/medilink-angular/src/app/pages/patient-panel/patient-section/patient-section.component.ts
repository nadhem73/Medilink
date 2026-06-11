import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

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

  appointments = [
    {
      doctor: 'Dr. Yasmine Ben Salem',
      specialty: 'Cardiologie',
      date: 'Lundi 10 juin 2026',
      hour: '09:30',
      mode: 'Presentiel',
      status: 'Confirme'
    },
    {
      doctor: 'Dr. Ines Gharbi',
      specialty: 'Medecine generale',
      date: 'Vendredi 14 juin 2026',
      hour: '11:00',
      mode: 'Teleconsultation',
      status: 'En ligne'
    },
    {
      doctor: 'Dr. Mehdi Trabelsi',
      specialty: 'Dermatologie',
      date: 'Mardi 18 juin 2026',
      hour: '14:15',
      mode: 'Presentiel',
      status: 'A confirmer'
    }
  ];

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
    private authService: AuthService
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
    });
  }
}
