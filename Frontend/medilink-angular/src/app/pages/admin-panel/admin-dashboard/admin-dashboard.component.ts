import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent {
  currentUser: any;

  summaryCards = [
    {
      title: 'Utilisateurs actifs',
      value: '12 480',
      detail: '+312 cette semaine',
      accent: 'gold'
    },
    {
      title: 'Comptes pro a valider',
      value: '4',
      detail: 'Medecins, pharmacies, labos',
      accent: 'red'
    },
    {
      title: 'Disponibilite systeme',
      value: '99.94 %',
      detail: '11 / 12 services en ligne',
      accent: 'blue'
    },
    {
      title: 'Incidents securite',
      value: '2',
      detail: 'Derniere 24 h',
      accent: 'green'
    }
  ];

  quickActions = [
    {
      title: 'Valider les comptes professionnels',
      description: 'Approuver ou refuser les inscriptions de medecins, pharmacies et laboratoires.',
      route: '/dashboard/admin/approvals',
      cta: 'Ouvrir la validation'
    },
    {
      title: 'Gerer les utilisateurs',
      description: 'Rechercher, suspendre ou reactiver les comptes de la plateforme.',
      route: '/dashboard/admin/users',
      cta: 'Voir les utilisateurs'
    },
    {
      title: 'Consulter les rapports analytics',
      description: 'Analyser la croissance, les rendez-vous et l activite globale.',
      route: '/dashboard/admin/analytics',
      cta: 'Voir les rapports'
    }
  ];

  pendingApprovals = [
    {
      name: 'Dr. Sami Khelifi',
      type: 'Medecin - Cardiologie',
      submitted: 'Demande il y a 2 heures',
      status: 'En attente'
    },
    {
      name: 'Pharmacie El Manar',
      type: 'Pharmacie - Tunis',
      submitted: 'Demande hier',
      status: 'En attente'
    },
    {
      name: 'Laboratoire Pasteur',
      type: 'Laboratoire - Sfax',
      submitted: 'Demande il y a 2 jours',
      status: 'A verifier'
    }
  ];

  systemHealth = [
    {
      name: 'Auth service',
      instruction: 'Disponibilite 99.98 %',
      nextDose: 'Operationnel'
    },
    {
      name: 'API Gateway',
      instruction: 'Latence moyenne 82 ms',
      nextDose: 'Operationnel'
    },
    {
      name: 'Service notifications',
      instruction: 'File d attente : 14 messages',
      nextDose: 'Degrade'
    }
  ];

  activityTimeline = [
    {
      title: 'Nouveau compte medecin valide',
      meta: 'Administrateur - il y a 1 heure',
      description: 'Le compte du Dr. Leila Mansour a ete approuve et active sur la plateforme.'
    },
    {
      title: 'Tentative de connexion suspecte bloquee',
      meta: 'Module securite - il y a 3 heures',
      description: 'Plusieurs echecs depuis une meme adresse IP. Le compte concerne a ete protege.'
    },
    {
      title: 'Mise a jour de la politique de confidentialite',
      meta: 'Systeme - hier',
      description: 'La nouvelle version a ete publiee et notifiee a l ensemble des utilisateurs.'
    }
  ];

  team = [
    { name: 'Super administrateur', role: 'Acces complet plateforme' },
    { name: 'Equipe support', role: 'Assistance utilisateurs' },
    { name: 'Equipe securite', role: 'Audit et conformite' }
  ];

  constructor(private authService: AuthService) {
    this.currentUser = this.authService.getCurrentUser();
  }
}
