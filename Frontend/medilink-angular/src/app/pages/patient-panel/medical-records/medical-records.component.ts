import { Component } from '@angular/core';

@Component({
  selector: 'app-patient-medical-records',
  templateUrl: './medical-records.component.html',
  styleUrls: ['./medical-records.component.scss']
})
export class MedicalRecordsComponent {
  // Donnees medicales centralisees (cahier des charges, section 6.2)
  vitals = [
    { label: 'Groupe sanguin', value: 'O+' },
    { label: 'Taille', value: '1,74 m' },
    { label: 'Poids', value: '72 kg' },
    { label: 'Allergies', value: 'Penicilline' },
    { label: 'Maladies chroniques', value: 'Hypertension' },
    { label: 'Medecin traitant', value: 'Dr. Yasmine Ben Salem' }
  ];

  treatments = [
    { name: 'Amlodipine 5 mg', detail: 'Traitement cardiovasculaire en cours', status: 'Actif' },
    { name: 'Vitamine D', detail: 'Complement, 1 capsule / soir', status: 'Suivi' }
  ];

  vaccinations = [
    { name: 'COVID-19 (rappel)', detail: 'Administre le 12 mars 2025', status: 'A jour' },
    { name: 'Tetanos', detail: 'Prochain rappel en 2027', status: 'A jour' },
    { name: 'Grippe saisonniere', detail: 'Recommande avant novembre', status: 'A prevoir' }
  ];

  documents = [
    { name: 'Compte rendu cardiologie.pdf', meta: 'Dr. Ben Salem - 02 juin 2026' },
    { name: 'Radiographie thorax.pdf', meta: 'Clinique El Manar - 20 mai 2026' },
    { name: 'Bilan sanguin complet.pdf', meta: 'Laboratoire Pasteur - 08 mai 2026' }
  ];
}
