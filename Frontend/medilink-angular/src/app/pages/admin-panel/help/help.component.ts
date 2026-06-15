import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent {
  faqs = [
    { q: 'Comment valider un compte professionnel ?', a: 'Dans la section Validation comptes pro, ouvrez la demande, verifiez les documents puis cliquez sur Approuver ou Refuser.' },
    { q: 'Comment suspendre un utilisateur ?', a: 'Depuis la Gestion utilisateurs, recherchez le compte et changez son statut en Suspendu.' },
    { q: 'Ou consulter les logs de securite ?', a: 'La section Logs & securite affiche le journal d activite, les connexions et les incidents.' },
    { q: 'Comment exporter les statistiques ?', a: 'La section Statistiques globales propose un export des rapports d activite de la plateforme.' }
  ];
}
