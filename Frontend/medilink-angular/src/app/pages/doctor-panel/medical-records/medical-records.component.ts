import { Component } from '@angular/core';

@Component({
  selector: 'app-doctor-medical-records',
  templateUrl: './medical-records.component.html',
  styleUrls: ['./medical-records.component.scss']
})
export class MedicalRecordsComponent {
  // Dossiers patients consultes par le medecin (cahier des charges, section 6.2)
  patients = [
    { label: 'Mohamed Aloui', value: 'Hypertension - O+' },
    { label: 'Fatma Khelifi', value: 'Diabete type 2 - A+' },
    { label: 'Sami Bouazizi', value: 'Asthme - B+' },
    { label: 'Leila Ben Amor', value: 'Suivi grossesse - AB+' },
    { label: 'Karim Jelassi', value: 'Cholesterol - O-' },
    { label: 'Nour Hamdi', value: 'Migraine chronique - A-' }
  ];

  ongoingCases = [
    { name: 'Mohamed Aloui', detail: 'Bilan cardiovasculaire a re-evaluer', status: 'En cours' },
    { name: 'Fatma Khelifi', detail: 'Adaptation du traitement antidiabetique', status: 'Suivi' }
  ];

  results = [
    { name: 'NFS - Sami Bouazizi', detail: 'Resultats recus, validation requise', status: 'A valider' },
    { name: 'Bilan lipidique - Karim Jelassi', detail: 'Disponible depuis hier', status: 'A valider' },
    { name: 'Glycemie - Fatma Khelifi', detail: 'Valeurs dans la norme', status: 'Valide' }
  ];

  documents = [
    { name: 'Compte rendu consultation Aloui.pdf', meta: 'Redige le 02 juin 2026' },
    { name: 'Radiographie thorax Bouazizi.pdf', meta: 'Clinique El Manar - 20 mai 2026' },
    { name: 'Ordonnance Khelifi.pdf', meta: 'Editee le 08 mai 2026' }
  ];
}
