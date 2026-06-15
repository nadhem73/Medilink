import { Component } from '@angular/core';

@Component({
  selector: 'app-pharmacy-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss']
})
export class MessagesComponent {
  conversations = [
    { contact: 'Dr. Yasmine Ben Salem', role: 'Cardiologue', preview: 'Pouvez-vous confirmer la disponibilite de l Amlodipine ?', time: '09:24', unread: 2 },
    { contact: 'MediPharm Tunisie', role: 'Fournisseur', preview: 'Votre commande CMD-2026-031 est en cours de livraison.', time: 'Hier', unread: 1 },
    { contact: 'Mohamed Khelifi', role: 'Patient', preview: 'Mon traitement est-il pret a etre retire ?', time: 'Lun', unread: 0 },
    { contact: 'Laboratoires Galiens', role: 'Fournisseur', preview: 'Nouvelle remise sur les generiques ce mois-ci.', time: '12 juin', unread: 0 }
  ];
}
