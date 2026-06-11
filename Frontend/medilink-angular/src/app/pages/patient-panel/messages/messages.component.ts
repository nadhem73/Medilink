import { Component } from '@angular/core';

@Component({
  selector: 'app-patient-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss']
})
export class MessagesComponent {
  conversations = [
    { contact: 'Dr. Yasmine Ben Salem', role: 'Cardiologue', preview: 'Vos resultats sont rassurants, on se voit lundi.', time: '09:24', unread: 2 },
    { contact: 'Laboratoire Pasteur', role: 'Laboratoire', preview: 'Votre NFS est disponible en telechargement.', time: 'Hier', unread: 1 },
    { contact: 'Dr. Mehdi Trabelsi', role: 'Dermatologue', preview: 'Pensez a appliquer la creme matin et soir.', time: 'Lun', unread: 0 },
    { contact: 'Pharmacie El Manar', role: 'Pharmacie', preview: 'Votre traitement est pret a etre retire.', time: '12 juin', unread: 0 }
  ];
}
