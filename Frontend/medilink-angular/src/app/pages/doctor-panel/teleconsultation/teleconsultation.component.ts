import { Component } from '@angular/core';

@Component({
  selector: 'app-doctor-teleconsultation',
  templateUrl: './teleconsultation.component.html',
  styleUrls: ['./teleconsultation.component.scss']
})
export class TeleconsultationComponent {
  sessions = [
    { patient: 'Mohamed Aloui', reason: 'Suivi hypertension', date: 'Vendredi 14 juin', hour: '11:00', status: 'A venir' },
    { patient: 'Leila Ben Amor', reason: 'Controle grossesse', date: 'Mardi 18 juin', hour: '16:30', status: 'Planifie' }
  ];

  history = [
    { patient: 'Sami Bouazizi', reason: 'Renouvellement traitement asthme', date: '02 juin 2026', status: 'Terminee' },
    { patient: 'Fatma Khelifi', reason: 'Suivi diabete', date: '21 mai 2026', status: 'Terminee' }
  ];

  features = [
    'Appels video HD securises avec vos patients.',
    'Partage securise de documents pendant la consultation.',
    'Redaction d\'ordonnance et de compte rendu en direct.'
  ];
}
