import { Component } from '@angular/core';

@Component({
  selector: 'app-doctor-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss']
})
export class MessagesComponent {
  conversations = [
    { contact: 'Mohamed Aloui', role: 'Patient', preview: 'Docteur, mes maux de tete ont diminue, merci.', time: '09:24', unread: 2 },
    { contact: 'Laboratoire Pasteur', role: 'Laboratoire', preview: 'Les resultats de M. Bouazizi sont disponibles.', time: 'Hier', unread: 1 },
    { contact: 'Fatma Khelifi', role: 'Patiente', preview: 'Je voulais confirmer le rendez-vous de jeudi.', time: 'Lun', unread: 0 },
    { contact: 'Secretariat - Clinique El Manar', role: 'Administration', preview: 'Votre planning de la semaine est mis a jour.', time: '12 juin', unread: 0 }
  ];
}
