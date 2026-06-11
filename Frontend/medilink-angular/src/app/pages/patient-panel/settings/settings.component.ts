import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-patient-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {
  currentUser: any;
  profileCards: { label: string; value: string }[] = [];

  preferences = [
    { label: 'Notifications par email', enabled: true },
    { label: 'Notifications par SMS', enabled: true },
    { label: 'Notifications push', enabled: false },
    { label: 'Authentification a deux facteurs', enabled: false }
  ];

  constructor(private authService: AuthService) {
    this.currentUser = this.authService.getCurrentUser();
    this.profileCards = [
      { label: 'Nom complet', value: `${this.currentUser?.firstName || 'Patient'} ${this.currentUser?.lastName || ''}`.trim() },
      { label: 'Email', value: this.currentUser?.email || 'Non renseigne' },
      { label: 'Telephone', value: this.currentUser?.phone || 'Non renseigne' },
      { label: 'Adresse', value: this.currentUser?.address || 'Non renseignee' },
      { label: 'Date de naissance', value: this.currentUser?.birthDate || 'Non renseignee' },
      { label: 'Genre', value: this.currentUser?.gender || 'Non renseigne' }
    ];
  }

  toggle(pref: { enabled: boolean }): void {
    pref.enabled = !pref.enabled;
  }
}
