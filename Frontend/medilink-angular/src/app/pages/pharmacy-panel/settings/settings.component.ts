import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-pharmacy-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {
  currentUser: any;
  profileCards: { label: string; value: string }[] = [];

  preferences = [
    { label: 'Alertes de rupture par email', enabled: true },
    { label: 'Alertes de rupture par SMS', enabled: true },
    { label: 'Notifications nouvelles ordonnances', enabled: true },
    { label: 'Authentification a deux facteurs', enabled: false }
  ];

  constructor(private authService: AuthService) {
    this.currentUser = this.authService.getCurrentUser();
    this.profileCards = [
      { label: 'Nom de la pharmacie', value: `${this.currentUser?.firstName || 'Pharmacie'} ${this.currentUser?.lastName || ''}`.trim() },
      { label: 'Email', value: this.currentUser?.email || 'Non renseigne' },
      { label: 'Telephone', value: this.currentUser?.phone || 'Non renseigne' },
      { label: 'Adresse', value: this.currentUser?.address || 'Non renseignee' },
      { label: 'Numero d agrement', value: this.currentUser?.licenseNumber || 'Non renseigne' },
      { label: 'Pharmacien responsable', value: this.currentUser?.gender || 'Non renseigne' }
    ];
  }

  toggle(pref: { enabled: boolean }): void {
    pref.enabled = !pref.enabled;
  }
}
