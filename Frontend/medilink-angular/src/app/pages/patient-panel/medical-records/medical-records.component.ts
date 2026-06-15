import { Component, OnInit } from '@angular/core';
import { PatientService, MedicalRecord } from '../../../core/services/patient.service';

@Component({
  selector: 'app-patient-medical-records',
  templateUrl: './medical-records.component.html',
  styleUrls: ['./medical-records.component.scss']
})
export class MedicalRecordsComponent implements OnInit {
  loading = true;

  // Informations sanitaires (alimentees par le dossier medical du patient).
  vitals: { label: string; value: string }[] = [];

  // Traitements en cours (derives du dossier medical, sinon donnees de demo).
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

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.patientService.getMyMedicalRecord().subscribe({
      next: (record) => {
        this.applyRecord(record);
        this.loading = false;
      },
      error: () => {
        // En cas d'erreur (service indisponible), on garde un affichage par defaut.
        this.vitals = this.fallbackVitals();
        this.loading = false;
      }
    });
  }

  private applyRecord(record: MedicalRecord): void {
    const vitals: { label: string; value: string }[] = [
      { label: 'Groupe sanguin', value: record.bloodGroup || 'Non renseigne' },
      { label: 'Taille', value: record.height ? `${record.height} cm` : 'Non renseigne' },
      { label: 'Poids', value: record.weight ? `${record.weight} kg` : 'Non renseigne' },
      { label: 'Allergies', value: record.allergies || 'Aucune' },
      { label: 'Maladies chroniques', value: record.chronicDiseases || 'Aucune' },
      { label: 'Contact urgence', value: this.emergencyContact(record) }
    ];
    this.vitals = vitals;

    if (record.currentTreatments && record.currentTreatments.trim()) {
      this.treatments = record.currentTreatments
        .split(/[,\n;]/)
        .map(t => t.trim())
        .filter(Boolean)
        .map(t => ({ name: t, detail: 'Traitement declare a l\'inscription', status: 'En cours' }));
    }
  }

  private emergencyContact(record: MedicalRecord): string {
    if (record.emergencyContactName || record.emergencyContactPhone) {
      return `${record.emergencyContactName || ''} ${record.emergencyContactPhone || ''}`.trim();
    }
    return 'Non renseigne';
  }

  private fallbackVitals(): { label: string; value: string }[] {
    return [
      { label: 'Groupe sanguin', value: 'Non disponible' },
      { label: 'Taille', value: 'Non disponible' },
      { label: 'Poids', value: 'Non disponible' },
      { label: 'Allergies', value: 'Non disponible' },
      { label: 'Maladies chroniques', value: 'Non disponible' },
      { label: 'Contact urgence', value: 'Non disponible' }
    ];
  }
}
