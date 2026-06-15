import { Component, HostListener, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  isAuthenticated = false;
  currentUser: any = null;
  isUserMenuOpen = false;
  avatarError = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isAuthenticated = !!user;
      this.avatarError = false;
    });
  }

  @HostListener('document:click')
  closeUserMenu(): void {
    this.isUserMenuOpen = false;
  }

  toggleUserMenu(event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  onAvatarError(): void {
    this.avatarError = true;
  }

  get displayName(): string {
    const firstName = (this.currentUser?.firstName || '').trim();
    const lastName = (this.currentUser?.lastName || '').trim();
    const name = `${firstName} ${lastName}`.trim();
    if (name) {
      return name;
    }
    const email = (this.currentUser?.email || '').trim();
    return email ? email.split('@')[0] : 'Utilisateur';
  }

  get avatarInitial(): string {
    const source = (this.currentUser?.firstName || this.currentUser?.lastName || this.currentUser?.email || '').trim();
    return source ? source.charAt(0).toUpperCase() : 'U';
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

  get panelRoute(): string {
    const roles: string[] = this.currentUser?.roles || [];
    const normalized = roles.map(r => String(r).toUpperCase());

    if (normalized.includes('ROLE_ADMIN') || normalized.includes('ADMIN')) return '/dashboard/admin';
    if (normalized.includes('ROLE_DOCTOR') || normalized.includes('DOCTOR')) return '/dashboard/doctor';
    if (normalized.some(r => r.includes('PHARMAC'))) return '/dashboard/pharmacy';
    if (normalized.includes('ROLE_LABORATORY') || normalized.includes('LABORATORY')) return '/dashboard/laboratory';
    if (normalized.includes('ROLE_AMBULANCE') || normalized.includes('AMBULANCE')) return '/dashboard/ambulance';
    return '/dashboard/patient';
  }

  get panelLabel(): string {
    const roles: string[] = this.currentUser?.roles || [];
    const normalized = roles.map(r => String(r).toUpperCase());

    if (normalized.includes('ROLE_ADMIN') || normalized.includes('ADMIN')) return 'Admin panel';
    if (normalized.includes('ROLE_DOCTOR') || normalized.includes('DOCTOR')) return 'Doctor panel';
    if (normalized.some(r => r.includes('PHARMAC'))) return 'Pharmacy panel';
    if (normalized.includes('ROLE_LABORATORY') || normalized.includes('LABORATORY')) return 'Laboratory panel';
    if (normalized.includes('ROLE_AMBULANCE') || normalized.includes('AMBULANCE')) return 'Ambulance panel';
    return 'Patient panel';
  }

  goToPanel(): void {
    this.isUserMenuOpen = false;
    this.router.navigateByUrl(this.panelRoute);
  }

  goToSettings(): void {
    this.isUserMenuOpen = false;
    this.router.navigateByUrl(this.panelRoute);
  }

  logout(): void {
    this.authService.logout();
    this.isUserMenuOpen = false;
    this.router.navigate(['/']);
  }
}
