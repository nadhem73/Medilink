import { Component } from '@angular/core';

@Component({
  selector: 'app-doctor-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent {
  faqs = [
    { q: 'Comment gerer mon agenda ?', a: 'Rendez-vous dans la section Agenda pour consulter, confirmer ou bloquer vos creneaux de consultation.' },
    { q: 'Comment valider des resultats d\'analyses ?', a: 'Les resultats publies par les laboratoires apparaissent dans la section Resultats d\'analyses, pret a etre valides.' },
    { q: 'Comment rediger une ordonnance ?', a: 'Depuis la fiche patient ou la section Ordonnances, cliquez sur Nouvelle ordonnance pour la rediger et la signer.' },
    { q: 'Comment lancer une teleconsultation ?', a: 'A l\'heure du rendez-vous, ouvrez la section Teleconsultation et cliquez sur Demarrer l\'appel.' }
  ];
}
