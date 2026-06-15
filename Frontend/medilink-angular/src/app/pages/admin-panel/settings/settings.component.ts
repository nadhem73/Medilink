import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {
  currentUser: any;
  profileCards: { label: string; value: string }[] = [];

  preferences = [
    { label: 'Alertes de securite par email', enabled: true },
    { label: 'Rapport hebdomadaire d activite', enabled: true },
    { label: 'Notifications de nouvelles inscriptions', enabled: false },
    { label: 'Authentification a deux facteurs (obligatoire)', enabled: true }
  ];

  constructor(private authService: AuthService) {
    this.currentUser = this.authService.getCurrentUser();
    this.profileCards = [
      { label: 'Nom complet', value: `${this.currentUser?.firstName || 'Administrateur'} ${this.currentUser?.lastName || ''}`.trim() },
      { label: 'Email', value: this.currentUser?.email || 'admin@medilink.tn' },
      { label: 'Role', value: 'Super administrateur' },
      { label: 'Telephone', value: this.currentUser?.phone || 'Non renseigne' },
      { label: 'Derniere connexion', value: "Aujourd hui 09:12" },
      { label: 'Niveau d acces', value: 'Complet' }
    ];
  }

  toggle(pref: { enabled: boolean }): void {
    pref.enabled = !pref.enabled;
  }
}
