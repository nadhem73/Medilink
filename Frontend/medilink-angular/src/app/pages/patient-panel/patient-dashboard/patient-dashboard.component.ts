import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-patient-dashboard',
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.scss']
})
export class PatientDashboardComponent {
  currentUser: any;

  summaryCards = [
    {
      title: 'Prochains rendez-vous',
      value: '3',
      detail: 'Cette semaine',
      accent: 'blue'
    },
    {
      title: 'Ordonnances actives',
      value: '5',
      detail: '2 a renouveler',
      accent: 'green'
    },
    {
      title: 'Analyses en attente',
      value: '2',
      detail: 'Laboratoire Charles Nicolle',
      accent: 'orange'
    },
    {
      title: 'Messages medicaux',
      value: '7',
      detail: 'Nouveaux depuis hier',
      accent: 'purple'
    }
  ];

  quickActions = [
    {
      title: 'Reserver un rendez-vous',
      description: 'Choisir un specialiste et confirmer un nouveau creneau.',
      route: '/dashboard/patient/appointments',
      cta: 'Voir mes rendez-vous'
    },
    {
      title: 'Suivre mes ordonnances',
      description: 'Verifier les traitements en cours et les renouvellements.',
      route: '/dashboard/patient/prescriptions',
      cta: 'Ouvrir les ordonnances'
    },
    {
      title: 'Consulter mes analyses',
      description: 'Acceder rapidement aux derniers resultats de laboratoire.',
      route: '/dashboard/patient/labs',
      cta: 'Voir les analyses'
    }
  ];

  upcomingAppointments = [
    {
      doctor: 'Dr. Yasmine Ben Salem',
      specialty: 'Cardiologie',
      date: 'Lundi 10 juin, 09:30',
      location: 'Clinique El Manar',
      status: 'Confirme'
    },
    {
      doctor: 'Dr. Mehdi Trabelsi',
      specialty: 'Dermatologie',
      date: 'Mercredi 12 juin, 14:15',
      location: 'Cabinet Lac 2',
      status: 'A preparer'
    },
    {
      doctor: 'Dr. Ines Gharbi',
      specialty: 'Medecine generale',
      date: 'Vendredi 14 juin, 11:00',
      location: 'Teleconsultation',
      status: 'En ligne'
    }
  ];

  medicationReminders = [
    {
      name: 'Amlodipine 5 mg',
      instruction: '1 comprime apres le petit-dejeuner',
      nextDose: 'Aujourd hui a 13:00'
    },
    {
      name: 'Vitamine D',
      instruction: '1 capsule chaque soir',
      nextDose: 'Ce soir a 20:00'
    },
    {
      name: 'Omeprazole 20 mg',
      instruction: 'Avant le repas du matin',
      nextDose: 'Demain a 08:00'
    }
  ];

  careTimeline = [
    {
      title: 'Analyse sanguine deposee',
      meta: 'Laboratoire central - il y a 2 heures',
      description: 'Le laboratoire a bien recu votre echantillon. Les resultats seront publies sous 24 h.'
    },
    {
      title: 'Ordonnance renouvelee',
      meta: 'Dr. Yasmine Ben Salem - hier',
      description: 'Votre traitement cardiovasculaire a ete prolonge pour 30 jours.'
    },
    {
      title: 'Question envoyee au medecin',
      meta: 'Messagerie securisee - il y a 2 jours',
      description: 'Vous avez partage un symptome post-consultation et le dossier attend une reponse.'
    }
  ];

  careTeam = [
    { name: 'Dr. Yasmine Ben Salem', role: 'Cardiologue referente' },
    { name: 'Dr. Mehdi Trabelsi', role: 'Dermatologue' },
    { name: 'Centre de biologie El Menzah', role: 'Laboratoire partenaire' }
  ];

  constructor(private authService: AuthService) {
    this.currentUser = this.authService.getCurrentUser();
  }
}

