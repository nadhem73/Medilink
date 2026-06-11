import { Component } from '@angular/core';
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
  currentUser: any = null;
  avatarError = false;

  // Liens du panneau patient (conformes au cahier des charges, section 6.2)
  links: SidebarLink[] = [
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

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.avatarError = false;
    });
  }

  get displayName(): string {
    const firstName = (this.currentUser?.firstName || '').trim();
    const lastName = (this.currentUser?.lastName || '').trim();
    const name = `${firstName} ${lastName}`.trim();
    if (name) {
      return name;
    }
    const email = (this.currentUser?.email || '').trim();
    return email ? email.split('@')[0] : 'Patient';
  }

  get avatarInitial(): string {
    const source = (this.currentUser?.firstName || this.currentUser?.lastName || this.currentUser?.email || '').trim();
    return source ? source.charAt(0).toUpperCase() : 'P';
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
}
