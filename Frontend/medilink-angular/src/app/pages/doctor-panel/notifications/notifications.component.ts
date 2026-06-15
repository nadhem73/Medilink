import { Component } from '@angular/core';

@Component({
  selector: 'app-doctor-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent {
  // Types de notifications (cahier des charges, section 13)
  notifications = [
    { title: 'Nouveau rendez-vous', detail: 'Mohamed Aloui a reserve une consultation demain a 09:30.', channel: 'Email, SMS, Push', time: 'Il y a 1 h', unread: true },
    { title: 'Resultats a valider', detail: 'La NFS de Sami Bouazizi a ete publiee par le laboratoire.', channel: 'Email, Push', time: 'Il y a 3 h', unread: true },
    { title: 'Ordonnance a signer', detail: 'Le renouvellement d\'Amlodipine pour M. Aloui attend votre signature.', channel: 'Email, Push', time: 'Hier', unread: false },
    { title: 'Paiement recu', detail: 'La consultation du 02 juin a bien ete reglee par la patiente.', channel: 'Push', time: 'Il y a 2 j', unread: false }
  ];
}
