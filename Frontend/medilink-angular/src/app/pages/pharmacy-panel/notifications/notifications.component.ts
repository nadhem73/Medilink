import { Component } from '@angular/core';

@Component({
  selector: 'app-pharmacy-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent {
  // Types de notifications (cahier des charges, section 13)
  notifications = [
    { title: 'Nouvelle ordonnance recue', detail: 'Ordonnance electronique de Dr. Ben Salem pour Mohamed Khelifi.', channel: 'Push, Email', time: 'Il y a 20 min', unread: true },
    { title: 'Alerte de rupture de stock', detail: 'L Insuline Lantus a atteint le seuil critique (4 stylos).', channel: 'Push, SMS', time: 'Il y a 2 h', unread: true },
    { title: 'Commande livree', detail: 'La commande CMD-2026-030 a ete receptionnee et ajoutee au stock.', channel: 'Email', time: 'Hier', unread: false },
    { title: 'Suggestion IA de reapprovisionnement', detail: 'Reapprovisionnement recommande pour l Amoxicilline 1 g.', channel: 'Push', time: 'Il y a 2 j', unread: false }
  ];
}
