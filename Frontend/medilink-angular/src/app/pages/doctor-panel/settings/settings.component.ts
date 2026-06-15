import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-doctor-settings',
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
      { label: 'Nom complet', value: `Dr. ${this.currentUser?.firstName || ''} ${this.currentUser?.lastName || ''}`.trim() },
      { label: 'Email', value: this.currentUser?.email || 'Non renseigne' },
      { label: 'Telephone', value: this.currentUser?.phone || 'Non renseigne' },
      { label: 'Specialite', value: this.currentUser?.specialty || 'Non renseignee' },
      { label: 'Numero d\'ordre', value: this.currentUser?.licenseNumber || 'Non renseigne' },
      { label: 'Etablissement', value: this.currentUser?.facility || 'Non renseigne' }
    ];
  }

  toggle(pref: { enabled: boolean }): void {
    pref.enabled = !pref.enabled;
  }
}
