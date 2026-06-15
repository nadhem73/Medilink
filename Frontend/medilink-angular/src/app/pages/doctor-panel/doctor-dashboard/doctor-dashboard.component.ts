import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-doctor-dashboard',
  templateUrl: './doctor-dashboard.component.html',
  styleUrls: ['./doctor-dashboard.component.scss']
})
export class DoctorDashboardComponent {
  currentUser: any;

  summaryCards = [
    {
      title: 'Patients du jour',
      value: '8',
      detail: '3 nouveaux dossiers',
      accent: 'blue'
    },
    {
      title: 'Consultations a venir',
      value: '5',
      detail: 'Cette semaine',
      accent: 'green'
    },
    {
      title: 'Ordonnances a signer',
      value: '4',
      detail: 'En attente de validation',
      accent: 'orange'
    },
    {
      title: 'Messages patients',
      value: '7',
      detail: 'Nouveaux depuis hier',
      accent: 'purple'
    }
  ];

  quickActions = [
    {
      title: 'Consulter mon agenda',
      description: 'Visualiser et confirmer les rendez-vous de la semaine.',
      route: '/dashboard/doctor/appointments',
      cta: 'Ouvrir l\'agenda'
    },
    {
      title: 'Mes patients',
      description: 'Acceder aux dossiers et antecedents de vos patients.',
      route: '/dashboard/doctor/patients',
      cta: 'Voir mes patients'
    },
    {
      title: 'Rediger une ordonnance',
      description: 'Prescrire un traitement et signer une ordonnance.',
      route: '/dashboard/doctor/prescriptions',
      cta: 'Nouvelle ordonnance'
    }
  ];

  todayAppointments = [
    {
      patient: 'Mohamed Aloui',
      reason: 'Suivi hypertension',
      date: 'Aujourd hui, 09:30',
      location: 'Cabinet - Salle 2',
      status: 'Confirme'
    },
    {
      patient: 'Fatma Khelifi',
      reason: 'Controle diabete',
      date: 'Aujourd hui, 10:15',
      location: 'Cabinet - Salle 2',
      status: 'En attente'
    },
    {
      patient: 'Leila Ben Amor',
      reason: 'Suivi grossesse',
      date: 'Aujourd hui, 11:00',
      location: 'Teleconsultation',
      status: 'En ligne'
    }
  ];

  pendingTasks = [
    {
      name: 'Ordonnance - Mohamed Aloui',
      instruction: 'Renouvellement Amlodipine a signer',
      nextDose: 'A traiter aujourd hui'
    },
    {
      name: 'Resultats NFS - Sami Bouazizi',
      instruction: 'Validation des analyses requise',
      nextDose: 'Recu il y a 3 h'
    },
    {
      name: 'Compte rendu - Fatma Khelifi',
      instruction: 'Consultation du 28 mai a finaliser',
      nextDose: 'En attente'
    }
  ];

  activityTimeline = [
    {
      title: 'Nouveau rendez-vous reserve',
      meta: 'Mohamed Aloui - il y a 2 heures',
      description: 'Un patient a reserve une consultation de controle pour demain matin.'
    },
    {
      title: 'Ordonnance signee',
      meta: 'Fatma Khelifi - hier',
      description: 'Le traitement antidiabetique a ete prolonge pour 30 jours.'
    },
    {
      title: 'Resultats recus',
      meta: 'Laboratoire Pasteur - il y a 2 jours',
      description: 'La NFS de Sami Bouazizi est disponible et attend votre validation.'
    }
  ];

  careTeam = [
    { name: 'Dr. Yasmine Ben Salem', role: 'Cardiologue - collegue' },
    { name: 'Centre de biologie El Menzah', role: 'Laboratoire partenaire' },
    { name: 'Secretariat Clinique El Manar', role: 'Support administratif' }
  ];

  constructor(private authService: AuthService) {
    this.currentUser = this.authService.getCurrentUser();
  }
}
