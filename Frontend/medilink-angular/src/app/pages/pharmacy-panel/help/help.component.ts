import { Component } from '@angular/core';

@Component({
  selector: 'app-pharmacy-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent {
  faqs = [
    { q: 'Comment receptionner une ordonnance electronique ?', a: 'Les ordonnances envoyees par les medecins apparaissent en temps reel dans la section Ordonnances recues.' },
    { q: 'Comment gerer les seuils d alerte de stock ?', a: 'Dans la section Stock, definissez un seuil par reference ; une alerte est generee automatiquement en cas de rupture imminente.' },
    { q: 'Comment passer une commande fournisseur ?', a: 'Rendez-vous dans la section Commandes, selectionnez un fournisseur et ajoutez les references a reapprovisionner.' },
    { q: 'Comment fonctionne la prevision IA des ruptures ?', a: 'Le module Previsions IA analyse votre consommation pour anticiper les ruptures et suggerer des reapprovisionnements.' }
  ];
}
