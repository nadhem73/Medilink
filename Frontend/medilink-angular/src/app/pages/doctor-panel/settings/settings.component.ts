import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-doctor-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {
  currentUser: any;
  profileCards: { key: string; label: string; value: string }[] = [];
  twoFactorEnabled = false;
  editing = false;
  saving = false;
  editForm: Record<string, string> = {};

  showPasswordForm = false;
  passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  savingPassword = false;
  passwordError = '';

  preferences = [
    { label: 'Notifications par email', enabled: true },
    { label: 'Notifications par SMS', enabled: true },
    { label: 'Notifications push', enabled: false }
  ];

  constructor(private authService: AuthService) {
    this.loadUser();
  }

  private loadUser(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.profileCards = [
      { key: 'firstName', label: 'Prenom', value: this.currentUser?.firstName || '' },
      { key: 'lastName', label: 'Nom', value: this.currentUser?.lastName || '' },
      { key: 'email', label: 'Email', value: this.currentUser?.email || 'Non renseigne' },
      { key: 'phone', label: 'Telephone', value: this.currentUser?.phone || 'Non renseigne' },
      { key: 'specialty', label: 'Specialite', value: this.currentUser?.specialty || 'Non renseignee' },
      { key: 'licenseNumber', label: "No d'ordre", value: this.currentUser?.licenseNumber || 'Non renseigne' },
      { key: 'facility', label: 'Etablissement', value: this.currentUser?.facility || 'Non renseigne' }
    ];
  }

  get initials(): string {
    const first = this.currentUser?.firstName?.charAt(0) || 'D';
    const last = this.currentUser?.lastName?.charAt(0) || 'R';
    return (first + last).toUpperCase();
  }

  get fullName(): string {
    const name = `${this.currentUser?.firstName || 'Docteur'} ${this.currentUser?.lastName || ''}`.trim();
    return name.startsWith('Dr.') ? name : `Dr. ${name}`;
  }

  startEditing(): void {
    this.editForm = {};
    for (const item of this.profileCards) {
      this.editForm[item.key] = this.currentUser?.[item.key] || '';
    }
    this.editing = true;
  }

  cancelEditing(): void {
    this.editing = false;
    this.editForm = {};
  }

  saveProfile(): void {
    this.saving = true;
    this.authService.updateProfile(this.editForm).subscribe({
      next: (user) => {
        this.currentUser = user;
        this.loadUser();
        this.editing = false;
        this.saving = false;
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  togglePasswordForm(): void {
    this.showPasswordForm = !this.showPasswordForm;
    if (!this.showPasswordForm) {
      this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
      this.passwordError = '';
    }
  }

  savePassword(): void {
    this.passwordError = '';
    if (!this.passwordForm.currentPassword || !this.passwordForm.newPassword || !this.passwordForm.confirmPassword) {
      this.passwordError = 'Tous les champs sont requis.';
      return;
    }
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.passwordError = 'Les mots de passe ne correspondent pas.';
      return;
    }
    if (this.passwordForm.newPassword.length < 6) {
      this.passwordError = 'Le mot de passe doit contenir au moins 6 caracteres.';
      return;
    }
    this.savingPassword = true;
    this.authService.updateProfile({ password: this.passwordForm.newPassword } as any).subscribe({
      next: () => {
        this.savingPassword = false;
        this.showPasswordForm = false;
        this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
      },
      error: () => {
        this.savingPassword = false;
        this.passwordError = 'Erreur lors du changement de mot de passe.';
      }
    });
  }

  toggle(pref: { enabled: boolean }): void {
    pref.enabled = !pref.enabled;
  }
}
