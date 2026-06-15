import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

type AdminSectionKey = 'approvals' | 'users' | 'monitoring' | 'security' | 'logs' | 'analytics';

@Component({
  selector: 'app-admin-section',
  templateUrl: './admin-section.component.html',
  styleUrls: ['./admin-section.component.scss']
})
export class AdminSectionComponent implements OnInit {
  section: AdminSectionKey = 'approvals';
  title = '';

  // Validation des comptes medicaux (medecins, pharmacies, laboratoires)
  approvals = [
    { name: 'Dr. Sami Khelifi', type: 'Medecin - Cardiologie', date: '11 juin 2026', status: 'En attente' },
    { name: 'Pharmacie El Manar', type: 'Pharmacie - Tunis', date: '10 juin 2026', status: 'En attente' },
    { name: 'Laboratoire Sfax Bio', type: 'Laboratoire - Sfax', date: '09 juin 2026', status: 'A verifier' },
    { name: 'Dr. Leila Mansour', type: 'Medecin - Pediatrie', date: '08 juin 2026', status: 'En attente' }
  ];

  // Gestion des utilisateurs de la plateforme
  users = [
    { name: 'Ahmed Bouazizi', role: 'Patient', email: 'ahmed.b@mail.tn', status: 'Actif' },
    { name: 'Dr. Yasmine Ben Salem', role: 'Medecin', email: 'y.bensalem@mail.tn', status: 'Actif' },
    { name: 'Pharmacie Centrale', role: 'Pharmacie', email: 'contact@pharma.tn', status: 'Actif' },
    { name: 'Mohamed Trabelsi', role: 'Patient', email: 'm.trabelsi@mail.tn', status: 'Suspendu' },
    { name: 'Laboratoire Pasteur', role: 'Laboratoire', email: 'lab@pasteur.tn', status: 'Actif' }
  ];

  // Monitoring systeme en temps reel (sante des microservices)
  monitoring = [
    { name: 'Auth service', metric: 'Disponibilite 99.98 %', sub: 'Latence 64 ms', status: 'Operationnel' },
    { name: 'API Gateway', metric: 'Latence moyenne 82 ms', sub: '1 240 req/min', status: 'Operationnel' },
    { name: 'Service notifications', metric: 'File d attente : 14 messages', sub: 'Debit reduit', status: 'Degrade' },
    { name: 'Pharmacy service', metric: 'Disponibilite 99.91 %', sub: 'CPU 38 %', status: 'Operationnel' },
    { name: 'Base de donnees', metric: 'Connexions 142/200', sub: 'Stockage 61 %', status: 'Operationnel' }
  ];

  monitoringKpis = [
    { label: 'Services en ligne', value: '11 / 12' },
    { label: 'Disponibilite globale', value: '99.94 %' },
    { label: 'Requetes / min', value: '4 820' },
    { label: 'Temps de reponse moyen', value: '78 ms' }
  ];

  // Securite & politiques d'acces
  accessPolicies = [
    { name: 'Authentification a deux facteurs', detail: 'Obligatoire pour les comptes professionnels et admin', status: 'Active' },
    { name: 'Expiration des sessions', detail: 'Deconnexion automatique apres 30 min d inactivite', status: 'Active' },
    { name: 'Politique de mots de passe', detail: 'Minimum 12 caracteres, rotation tous les 90 jours', status: 'Active' },
    { name: 'Liste de blocage IP', detail: '3 adresses actuellement bloquees', status: 'Surveillance' }
  ];

  securityIncidents = [
    { title: 'Connexions echouees repetees', meta: 'IP 41.226.x.x', level: 'Eleve' },
    { title: 'Acces refuse a une ressource admin', meta: 'Compte medecin', level: 'Moyen' },
    { title: 'Nouvel appareil detecte', meta: 'Compte administrateur', level: 'Info' }
  ];

  // Logs d'activite
  logs = [
    { action: 'Connexion administrateur', source: 'IP 196.203.x.x', date: "Aujourd hui 09:12", result: 'Succes', note: 'Session ouverte depuis Tunis.' },
    { action: 'Echecs de connexion repetes', source: 'IP 41.226.x.x', date: "Aujourd hui 03:41", result: 'Bloque', note: 'Adresse mise en liste de surveillance.' },
    { action: 'Validation compte medecin', source: 'Module admin', date: 'Hier 16:20', result: 'Succes', note: 'Dr. Leila Mansour approuvee.' },
    { action: 'Modification politique d acces', source: 'Module securite', date: 'Hier 11:05', result: 'Succes', note: '2FA rendu obligatoire pour les pros.' }
  ];

  // Rapports analytics
  analytics = [
    { label: 'Utilisateurs totaux', value: '12 480' },
    { label: 'Medecins actifs', value: '1 240' },
    { label: 'Pharmacies', value: '320' },
    { label: 'Laboratoires', value: '96' },
    { label: 'Rendez-vous ce mois', value: '8 932' },
    { label: 'Teleconsultations', value: '2 145' }
  ];

  analyticsReports = [
    { title: 'Rapport mensuel d activite', detail: 'Synthese des inscriptions, rendez-vous et revenus.' },
    { title: 'Croissance des comptes pro', detail: 'Evolution des medecins, pharmacies et laboratoires.' },
    { title: 'Taux d adoption teleconsultation', detail: 'Part des consultations realisees a distance.' }
  ];

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.section = data['section'] as AdminSectionKey;
      this.title = data['title'] as string;
    });
  }
}
