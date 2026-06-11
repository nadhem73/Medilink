import { Component } from '@angular/core';

@Component({
  selector: 'app-patient-teleconsultation',
  templateUrl: './teleconsultation.component.html',
  styleUrls: ['./teleconsultation.component.scss']
})
export class TeleconsultationComponent {
  sessions = [
    { doctor: 'Dr. Ines Gharbi', specialty: 'Medecine generale', date: 'Vendredi 14 juin', hour: '11:00', status: 'A venir' },
    { doctor: 'Dr. Yasmine Ben Salem', specialty: 'Cardiologie', date: 'Mardi 18 juin', hour: '16:30', status: 'Planifie' }
  ];

  history = [
    { doctor: 'Dr. Mehdi Trabelsi', specialty: 'Dermatologie', date: '02 juin 2026', status: 'Terminee' },
    { doctor: 'Dr. Ines Gharbi', specialty: 'Medecine generale', date: '21 mai 2026', status: 'Terminee' }
  ];

  features = [
    'Appels video HD securises entre patient et medecin.',
    'Partage securise de documents pendant la consultation.',
    'Messagerie instantanee chiffree avec le praticien.'
  ];
}
