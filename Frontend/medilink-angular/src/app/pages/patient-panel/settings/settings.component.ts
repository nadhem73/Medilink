import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import * as QRCode from 'qrcode';

@Component({
  selector: 'app-patient-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
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

  telegramChatId: string | null = null;
  telegramLoading = false;
  telegramInput = '';
  telegramLinking = false;
  telegramError = '';
  telegramSuccess = '';
  telegramQrCode: string | null = null;
  checkingTelegram = false;
  telegramContinue = false;

  preferences = [
    { label: 'Notifications par email', enabled: true },
    { label: 'Notifications par SMS', enabled: true },
    { label: 'Notifications push', enabled: false }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.loadUser();
  }

  private loadUser(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.profileCards = [
      { key: 'firstName', label: 'Prenom', value: this.currentUser?.firstName || '' },
      { key: 'lastName', label: 'Nom', value: this.currentUser?.lastName || '' },
      { key: 'email', label: 'Email', value: this.currentUser?.email || 'Non renseigne' },
      { key: 'phone', label: 'Telephone', value: this.currentUser?.phone || 'Non renseigne' },
      { key: 'address', label: 'Adresse', value: this.currentUser?.address || 'Non renseignee' },
      { key: 'birthDate', label: 'Date de naissance', value: this.currentUser?.birthDate || 'Non renseignee' },
      { key: 'gender', label: 'Genre', value: this.currentUser?.gender || 'Non renseigne' }
    ];
    this.loadTelegramStatus();
  }

  get initials(): string {
    const first = this.currentUser?.firstName?.charAt(0) || 'P';
    const last = this.currentUser?.lastName?.charAt(0) || '';
    return (first + last).toUpperCase();
  }

  get fullName(): string {
    return `${this.currentUser?.firstName || 'Patient'} ${this.currentUser?.lastName || ''}`.trim();
  }

  get patientId(): number | null {
    return this.currentUser?.id || null;
  }

  loadTelegramStatus(): void {
    if (!this.patientId) return;
    this.telegramLoading = true;
    this.authService.getTelegramChatId(this.patientId).subscribe({
      next: (res) => {
        this.telegramChatId = res.telegramChatId || null;
        this.telegramLoading = false;
        this.generateTelegramQr();
      },
      error: () => {
        this.telegramLoading = false;
        this.generateTelegramQr();
      }
    });
  }

  generateTelegramQr(): void {
    const botUrl = 'https://t.me/medilink_tunisie_bot';
    QRCode.toDataURL(botUrl, {
      width: 180,
      margin: 1,
      color: { dark: '#1f3b73', light: '#ffffff' }
    }).then(url => {
      this.telegramQrCode = url;
    }).catch(() => {
      this.telegramQrCode = null;
    });
  }

  linkTelegramAccount(): void {
    this.telegramError = '';
    this.telegramSuccess = '';
    if (!this.telegramInput.trim()) {
      this.telegramError = 'Veuillez entrer votre identifiant Telegram.';
      return;
    }
    this.telegramLinking = true;
    this.authService.linkTelegram({
      email: this.currentUser?.email || '',
      telegramChatId: this.telegramInput.trim()
    }).subscribe({
      next: () => {
        this.telegramChatId = this.telegramInput.trim();
        this.telegramInput = '';
        this.telegramLinking = false;
        this.telegramSuccess = 'Compte Telegram lie avec succes !';
        setTimeout(() => this.telegramSuccess = '', 3000);
      },
      error: () => {
        this.telegramLinking = false;
        this.telegramError = 'Erreur lors de la liaison. Verifiez votre email.';
      }
    });
  }

  unlinkTelegramAccount(): void {
    this.telegramInput = '';
    this.telegramLinking = true;
    this.authService.linkTelegram({
      email: this.currentUser?.email || '',
      telegramChatId: ''
    }).subscribe({
      next: () => {
        this.telegramChatId = null;
        this.telegramLinking = false;
        this.telegramSuccess = 'Compte Telegram dissocie.';
        setTimeout(() => this.telegramSuccess = '', 3000);
      },
      error: () => {
        this.telegramLinking = false;
        this.telegramError = 'Erreur lors de la dissociation.';
      }
    });
  }

  openTelegramBot(): void {
    window.open('https://t.me/medilink_tunisie_bot', '_blank');
  }

  checkTelegramConnection(): void {
    if (!this.currentUser?.email) return;
    this.checkingTelegram = true;
    this.telegramError = '';
    this.telegramSuccess = '';
    this.telegramContinue = false;
    this.authService.autoLinkTelegram(this.currentUser.email).subscribe({
      next: (res) => {
        this.checkingTelegram = false;
        if (res.success && res.telegramChatId) {
          this.telegramChatId = res.telegramChatId;
          this.telegramSuccess = 'Compte Telegram lie automatiquement !';
          setTimeout(() => this.telegramSuccess = '', 3000);
        } else if (res.webhookDeleted) {
          this.telegramContinue = true;
          this.telegramError = res.message || 'Envoyez un message au bot puis cliquez sur Continuer.';
        } else {
          this.telegramError = res.message || 'Aucun message detecte.';
        }
      },
      error: () => {
        this.checkingTelegram = false;
        this.telegramError = 'Erreur lors de la verification.';
      }
    });
  }

  continueLinking(): void {
    if (!this.currentUser?.email) return;
    this.checkingTelegram = true;
    this.telegramError = '';
    this.telegramSuccess = '';
    this.authService.completeLinking(this.currentUser.email).subscribe({
      next: (res) => {
        this.checkingTelegram = false;
        this.telegramContinue = false;
        if (res.success && res.telegramChatId) {
          this.telegramChatId = res.telegramChatId;
          this.telegramSuccess = 'Compte Telegram lie automatiquement !';
          setTimeout(() => this.telegramSuccess = '', 3000);
        } else {
          this.telegramError = res.message || 'Aucun message trouve. Envoyez un message puis reessayez.';
        }
      },
      error: () => {
        this.checkingTelegram = false;
        this.telegramContinue = false;
        this.telegramError = 'Erreur lors de la verification.';
      }
    });
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
