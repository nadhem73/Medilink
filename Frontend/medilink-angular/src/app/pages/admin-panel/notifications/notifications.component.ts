import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent {
  notifications = [
    { title: 'Nouvelle demande de validation', detail: 'Dr. Sami Khelifi attend l approbation de son compte.', time: 'Il y a 2 heures', unread: true },
    { title: 'Alerte securite', detail: 'Tentatives de connexion repetees depuis une IP suspecte.', time: 'Il y a 3 heures', unread: true },
    { title: 'Service notifications degrade', detail: 'La file d attente depasse le seuil normal.', time: "Aujourd hui 08:10", unread: true },
    { title: 'Rapport hebdomadaire disponible', detail: 'Le rapport d activite de la semaine est pret.', time: 'Hier', unread: false },
    { title: 'Compte medecin valide', detail: 'Dr. Leila Mansour a ete approuvee.', time: 'Il y a 2 jours', unread: false }
  ];
}
