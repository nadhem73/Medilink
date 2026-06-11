import { Component } from '@angular/core';

@Component({
  selector: 'app-patient-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent {
  faqs = [
    { q: 'Comment prendre un rendez-vous ?', a: 'Rendez-vous dans la section Rendez-vous, choisissez un specialiste puis un creneau disponible.' },
    { q: 'Comment acceder a mes resultats d\'analyses ?', a: 'Les resultats publies par le laboratoire apparaissent dans la section Resultats d\'analyses.' },
    { q: 'Comment fonctionne la teleconsultation ?', a: 'A l\'heure du rendez-vous, ouvrez la section Teleconsultation et cliquez sur Demarrer l\'appel.' },
    { q: 'Comment payer une consultation ?', a: 'Les paiements se font en ligne par carte bancaire ou paiement mobile depuis la section Facturation.' }
  ];
}
