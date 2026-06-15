import { Component, HostBinding } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface SidebarLink {
  label: string;
  route: string;
  icon: string;
  badge?: number;
  exact?: boolean;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  @HostBinding('class.modal-active') get isModalActive(): boolean {
    return this.showVerificationModal;
  }

  currentUser: any = null;
  avatarError = false;

  // Liens du panneau patient (conformes au cahier des charges, section 6.2)
  patientLinks: SidebarLink[] = [
    { label: 'Tableau de bord', route: '/dashboard/patient', icon: 'dashboard', exact: true },
    { label: 'Rendez-vous', route: '/dashboard/patient/appointments', icon: 'calendar' },
    { label: 'Dossiers medicaux', route: '/dashboard/patient/medical-records', icon: 'folder' },
    { label: 'Ordonnances', route: '/dashboard/patient/prescriptions', icon: 'pill' },
    { label: "Resultats d'analyses", route: '/dashboard/patient/labs', icon: 'flask' },
    { label: 'Teleconsultation', route: '/dashboard/patient/teleconsultation', icon: 'video' },
    { label: 'Messages', route: '/dashboard/patient/messages', icon: 'message', badge: 3 },
    { label: 'Notifications', route: '/dashboard/patient/notifications', icon: 'bell', badge: 5 },
    { label: 'Facturation', route: '/dashboard/patient/billing', icon: 'card' },
    { label: 'Parametres', route: '/dashboard/patient/settings', icon: 'settings' },
    { label: "Centre d'aide", route: '/dashboard/patient/help', icon: 'help' }
  ];

  // Liens du panneau medecin (fonctionnalites du medecin)
  doctorLinks: SidebarLink[] = [
    { label: 'Tableau de bord', route: '/dashboard/doctor', icon: 'dashboard', exact: true },
    { label: 'Mes patients', route: '/dashboard/doctor/patients', icon: 'users' },
    { label: 'Agenda', route: '/dashboard/doctor/appointments', icon: 'calendar' },
    { label: 'Consultations', route: '/dashboard/doctor/consultations', icon: 'stethoscope' },
    { label: 'Dossiers medicaux', route: '/dashboard/doctor/medical-records', icon: 'folder' },
    { label: 'Ordonnances', route: '/dashboard/doctor/prescriptions', icon: 'pill' },
    { label: 'Teleconsultation', route: '/dashboard/doctor/teleconsultation', icon: 'video' },
    { label: "Resultats d'analyses", route: '/dashboard/doctor/labs', icon: 'flask' },
    { label: 'Messages', route: '/dashboard/doctor/messages', icon: 'message', badge: 3 },
    { label: 'Notifications', route: '/dashboard/doctor/notifications', icon: 'bell', badge: 5 },
    { label: 'Parametres', route: '/dashboard/doctor/settings', icon: 'settings' },
    { label: "Centre d'aide", route: '/dashboard/doctor/help', icon: 'help' }
  ];

  // Liens du panneau pharmacie (cahier des charges, module 6.4)
  pharmacyLinks: SidebarLink[] = [
    { label: 'Tableau de bord', route: '/dashboard/pharmacy', icon: 'dashboard', exact: true },
    { label: 'Ordonnances recues', route: '/dashboard/pharmacy/prescriptions', icon: 'pill' },
    { label: 'Stock medicaments', route: '/dashboard/pharmacy/stock', icon: 'box' },
    { label: 'Commandes', route: '/dashboard/pharmacy/orders', icon: 'cart' },
    { label: 'Ventes / Dispensation', route: '/dashboard/pharmacy/sales', icon: 'card' },
    { label: 'Alertes de stock', route: '/dashboard/pharmacy/alerts', icon: 'alert', badge: 5 },
    { label: 'Previsions IA', route: '/dashboard/pharmacy/forecast', icon: 'chart' },
    { label: 'Messages', route: '/dashboard/pharmacy/messages', icon: 'message', badge: 3 },
    { label: 'Notifications', route: '/dashboard/pharmacy/notifications', icon: 'bell', badge: 5 },
    { label: 'Parametres', route: '/dashboard/pharmacy/settings', icon: 'settings' },
    { label: "Centre d'aide", route: '/dashboard/pharmacy/help', icon: 'help' }
  ];

  // Liens du panneau administrateur (supervision technique de la plateforme)
  adminLinks: SidebarLink[] = [
    { label: 'Tableau de bord', route: '/dashboard/admin', icon: 'dashboard', exact: true },
    { label: 'Validation comptes medicaux', route: '/dashboard/admin/approvals', icon: 'shield', badge: 4 },
    { label: 'Gestion utilisateurs', route: '/dashboard/admin/users', icon: 'users' },
    { label: 'Monitoring systeme', route: '/dashboard/admin/monitoring', icon: 'activity' },
    { label: 'Securite & acces', route: '/dashboard/admin/security', icon: 'lock' },
    { label: "Logs d'activite", route: '/dashboard/admin/logs', icon: 'file' },
    { label: 'Rapports analytics', route: '/dashboard/admin/analytics', icon: 'chart' },
    { label: 'Notifications', route: '/dashboard/admin/notifications', icon: 'bell', badge: 5 },
    { label: 'Parametres', route: '/dashboard/admin/settings', icon: 'settings' },
    { label: "Centre d'aide", route: '/dashboard/admin/help', icon: 'help' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.avatarError = false;
    });
  }

  get isDoctor(): boolean {
    return this.router.url.startsWith('/dashboard/doctor');
  }

  get isPharmacy(): boolean {
    return this.router.url.startsWith('/dashboard/pharmacy');
  }

  get isAdmin(): boolean {
    return this.router.url.startsWith('/dashboard/admin');
  }

  get links(): SidebarLink[] {
    if (this.isAdmin) {
      return this.adminLinks;
    }
    if (this.isPharmacy) {
      return this.pharmacyLinks;
    }
    return this.isDoctor ? this.doctorLinks : this.patientLinks;
  }

  get roleLabel(): string {
    if (this.isAdmin) {
      return 'Administrateur';
    }
    if (this.isPharmacy) {
      return 'Pharmacie';
    }
    return this.isDoctor ? 'Medecin' : 'Patient';
  }

  get displayName(): string {
    const firstName = (this.currentUser?.firstName || '').trim();
    const lastName = (this.currentUser?.lastName || '').trim();
    const name = `${firstName} ${lastName}`.trim();
    if (name) {
      return this.isDoctor ? `Dr ${name}` : name;
    }
    const email = (this.currentUser?.email || '').trim();
    const fallback = email ? email.split('@')[0] : this.roleLabel;
    return this.isDoctor && email ? `Dr ${fallback}` : fallback;
  }

  get avatarInitial(): string {
    const source = (this.currentUser?.firstName || this.currentUser?.lastName || this.currentUser?.email || '').trim();
    if (source) {
      return source.charAt(0).toUpperCase();
    }
    if (this.isAdmin) {
      return 'A';
    }
    if (this.isPharmacy) {
      return 'Ph';
    }
    return this.isDoctor ? 'D' : 'P';
  }

  get avatarUrl(): string | null {
    const url =
      this.currentUser?.imageUrl ||
      this.currentUser?.avatarUrl ||
      this.currentUser?.photoUrl ||
      this.currentUser?.photo ||
      null;
    return typeof url === 'string' && url.trim().length > 0 ? url : null;
  }

  get isVerified(): boolean {
    return !!(this.currentUser?.isEmailVerified ?? this.currentUser?.emailVerified);
  }

  onAvatarError(): void {
    this.avatarError = true;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  // OTP Verification Modal Logic
  showVerificationModal = false;
  otpCode = '';
  otpLoading = false;
  otpError = '';
  otpSuccess = '';
  resendCountdown = 0;
  resendIntervalId: any = null;

  triggerVerification(): void {
    this.otpError = '';
    this.otpSuccess = '';
    this.otpCode = '';
    this.showVerificationModal = true;
    this.otpLoading = true;
    
    this.authService.requestEmailVerification().subscribe({
      next: () => {
        this.otpLoading = false;
        this.startResendCountdown();
      },
      error: (err) => {
        this.otpLoading = false;
        this.otpError = "Impossible d'envoyer le code de vérification. Veuillez réessayer.";
        console.error(err);
      }
    });
  }

  verifyOtp(): void {
    if (!this.otpCode || this.otpCode.length !== 6) {
      this.otpError = 'Veuillez saisir un code à 6 chiffres.';
      return;
    }

    this.otpLoading = true;
    this.otpError = '';
    this.otpSuccess = '';

    this.authService.verifyEmailOtp(this.otpCode).subscribe({
      next: () => {
        this.otpLoading = false;
        this.otpSuccess = 'Votre adresse e-mail a été vérifiée avec succès !';
        setTimeout(() => {
          this.closeVerificationModal();
        }, 2000);
      },
      error: (err) => {
        this.otpLoading = false;
        this.otpError = err.error?.message || 'Code de vérification incorrect ou expiré.';
        console.error(err);
      }
    });
  }

  resendOtp(): void {
    if (this.resendCountdown > 0) return;
    this.otpError = '';
    this.otpSuccess = '';
    this.otpLoading = true;

    this.authService.requestEmailVerification().subscribe({
      next: () => {
        this.otpLoading = false;
        this.otpSuccess = 'Un nouveau code OTP a été envoyé par e-mail.';
        this.startResendCountdown();
      },
      error: (err) => {
        this.otpLoading = false;
        this.otpError = "Impossible de renvoyer le code. Veuillez réessayer.";
        console.error(err);
      }
    });
  }

  closeVerificationModal(): void {
    this.showVerificationModal = false;
    this.otpCode = '';
    this.otpError = '';
    this.otpSuccess = '';
    if (this.resendIntervalId) {
      clearInterval(this.resendIntervalId);
      this.resendIntervalId = null;
    }
    this.resendCountdown = 0;
  }

  startResendCountdown(): void {
    if (this.resendIntervalId) {
      clearInterval(this.resendIntervalId);
    }
    this.resendCountdown = 60;
    this.resendIntervalId = setInterval(() => {
      if (this.resendCountdown > 0) {
        this.resendCountdown--;
      } else {
        clearInterval(this.resendIntervalId);
        this.resendIntervalId = null;
      }
    }, 1000);
  }
}
