import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-pharmacy-dashboard',
  templateUrl: './pharmacy-dashboard.component.html',
  styleUrls: ['./pharmacy-dashboard.component.scss']
})
export class PharmacyDashboardComponent {
  currentUser: any;

  summaryCards = [
    {
      title: 'Ordonnances recues',
      value: '8',
      detail: 'A traiter aujourd hui',
      accent: 'blue'
    },
    {
      title: 'Medicaments en stock',
      value: '1 240',
      detail: 'References actives',
      accent: 'green'
    },
    {
      title: 'Alertes de stock',
      value: '5',
      detail: 'Seuil critique atteint',
      accent: 'orange'
    },
    {
      title: 'Commandes en cours',
      value: '3',
      detail: 'Aupres des fournisseurs',
      accent: 'purple'
    }
  ];

  quickActions = [
    {
      title: 'Traiter les ordonnances',
      description: 'Receptionner et dispenser les ordonnances electroniques recues en temps reel.',
      route: '/dashboard/pharmacy/prescriptions',
      cta: 'Ouvrir les ordonnances'
    },
    {
      title: 'Gerer le stock',
      description: 'Suivre les niveaux de stock, les seuils d alerte et les peremptions.',
      route: '/dashboard/pharmacy/stock',
      cta: 'Voir le stock'
    },
    {
      title: 'Passer une commande',
      description: 'Reapprovisionner aupres des fournisseurs et suivre les livraisons.',
      route: '/dashboard/pharmacy/orders',
      cta: 'Gerer les commandes'
    }
  ];

  pendingPrescriptions = [
    {
      patient: 'Mohamed Khelifi',
      doctor: 'Dr. Yasmine Ben Salem',
      date: 'Aujourd hui, 08:45',
      items: '3 medicaments',
      status: 'A preparer'
    },
    {
      patient: 'Salma Aouini',
      doctor: 'Dr. Mehdi Trabelsi',
      date: 'Aujourd hui, 09:20',
      items: '1 medicament',
      status: 'En attente'
    },
    {
      patient: 'Karim Bouzid',
      doctor: 'Dr. Ines Gharbi',
      date: 'Aujourd hui, 10:05',
      items: '2 medicaments',
      status: 'Verification stock'
    }
  ];

  stockAlerts = [
    {
      name: 'Amoxicilline 1 g',
      instruction: 'Stock : 12 boites - seuil : 30',
      nextDose: 'Rupture imminente'
    },
    {
      name: 'Paracetamol 1 g',
      instruction: 'Stock : 8 boites - seuil : 25',
      nextDose: 'A commander'
    },
    {
      name: 'Insuline Lantus',
      instruction: 'Stock : 4 stylos - seuil : 15',
      nextDose: 'Critique'
    }
  ];

  activityTimeline = [
    {
      title: 'Ordonnance dispensee',
      meta: 'Mohamed Khelifi - il y a 30 min',
      description: 'Trois medicaments delivres et historises dans le registre de dispensation.'
    },
    {
      title: 'Commande receptionnee',
      meta: 'Fournisseur MediPharm - il y a 2 heures',
      description: 'Reapprovisionnement de 40 references ajoute au stock central.'
    },
    {
      title: 'Alerte de rupture',
      meta: 'Module IA Pharmacie - hier',
      description: 'Une rupture a ete anticipee sur l Insuline Lantus, commande suggeree.'
    }
  ];

  suppliers = [
    { name: 'MediPharm Tunisie', role: 'Fournisseur principal' },
    { name: 'Laboratoires Galiens', role: 'Generiques et antibiotiques' },
    { name: 'Distri-Sante Lac 2', role: 'Materiel et parapharmacie' }
  ];

  constructor(private authService: AuthService) {
    this.currentUser = this.authService.getCurrentUser();
  }
}
