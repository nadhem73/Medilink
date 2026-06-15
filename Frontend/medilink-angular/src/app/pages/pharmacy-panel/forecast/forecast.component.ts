import { Component } from '@angular/core';

@Component({
  selector: 'app-pharmacy-forecast',
  templateUrl: './forecast.component.html',
  styleUrls: ['./forecast.component.scss']
})
export class ForecastComponent {
  // IA Pharmacie (cahier des charges, section 7.5)
  predictions = [
    { name: 'Insuline Lantus', detail: 'Rupture prevue dans 3 jours selon la consommation', status: 'A commander' },
    { name: 'Amoxicilline 1 g', detail: 'Rupture prevue dans 6 jours', status: 'A surveiller' }
  ];

  trends = [
    { name: 'Antibiotiques', detail: 'Consommation +18 % ce mois (saison)', status: 'En hausse' },
    { name: 'Antihistaminiques', detail: 'Pic saisonnier attendu en juillet', status: 'A anticiper' },
    { name: 'Vitamines', detail: 'Consommation stable', status: 'Stable' }
  ];

  features = [
    'Prevision des ruptures de stock par analyse predictive.',
    'Analyse des tendances de consommation des medicaments.',
    'Suggestions de reapprovisionnement automatiques.'
  ];
}
