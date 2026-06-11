import { Component } from '@angular/core';

@Component({
  selector: 'app-patient-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent {
  // Types de notifications (cahier des charges, section 13)
  notifications = [
    { title: 'Rappel de rendez-vous', detail: 'Consultation avec Dr. Ben Salem demain a 09:30.', channel: 'Email, SMS, Push', time: 'Il y a 1 h', unread: true },
    { title: 'Analyses disponibles', detail: 'Vos resultats de NFS ont ete publies par le laboratoire.', channel: 'Email, Push', time: 'Il y a 3 h', unread: true },
    { title: 'Renouvellement d\'ordonnance', detail: 'Votre traitement Amlodipine arrive a echeance dans 4 jours.', channel: 'Email, Push', time: 'Hier', unread: false },
    { title: 'Paiement confirme', detail: 'Votre consultation du 02 juin a bien ete reglee.', channel: 'Push', time: 'Il y a 2 j', unread: false }
  ];
}
