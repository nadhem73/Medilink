import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService, AdminUserDto } from '../../../core/services/auth.service';

type AdminSectionKey = 'users';

@Component({
  selector: 'app-admin-section',
  templateUrl: './admin-section.component.html',
  styleUrls: ['./admin-section.component.scss']
})
export class AdminSectionComponent implements OnInit {
  section: AdminSectionKey = 'users';
  title = '';

  // Gestion des utilisateurs — chargés depuis le backend
  users: AdminUserDto[] = [];
  loadingUsers = false;
  usersError = '';

  // Filtre role pour la section users
  userFilter: 'ALL' | 'DOCTOR' | 'PHARMACY' | 'PATIENT' = 'ALL';

  // Recherche et tri
  userSearch = '';
  userSortBy: 'name' | 'status' | 'date' = 'name';
  userSortDir: 'asc' | 'desc' = 'asc';

  // Suspension modal
  showSuspendModal = false;
  suspendTarget: AdminUserDto | null = null;
  suspendDuration: number = 7;
  suspendUnit: 'DAYS' | 'HOURS' = 'DAYS';
  suspending = false;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.section = data['section'] as AdminSectionKey;
      this.title = data['title'] as string;

      if (this.section === 'users') {
        const filter = data['filter'] as string;
        if (filter && filter !== 'ALL') {
          this.userFilter = filter as 'DOCTOR' | 'PHARMACY' | 'PATIENT';
        }
        this.loadUsers();
      }
    });
  }

  loadUsers(): void {
    this.loadingUsers = true;
    this.usersError = '';
    this.authService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loadingUsers = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        this.usersError = 'Impossible de charger les utilisateurs.';
        this.loadingUsers = false;
      }
    });
  }

  get filteredUsers(): AdminUserDto[] {
    let list = this.userFilter === 'ALL'
      ? this.users
      : this.users.filter(u => u.role === this.userFilter);

    // Recherche (nom, email, telephone, specialite, numero de licence)
    const term = this.userSearch.trim().toLowerCase();
    if (term) {
      list = list.filter(u => {
        const haystack = [
          this.getUserDisplayName(u),
          u.email,
          u.phone,
          u.specialty,
          u.licenseNumber
        ].filter(Boolean).join(' ').toLowerCase();
        return haystack.includes(term);
      });
    }

    // Tri
    const dir = this.userSortDir === 'asc' ? 1 : -1;
    return [...list].sort((a, b) => {
      let cmp = 0;
      switch (this.userSortBy) {
        case 'name':
          cmp = this.getUserDisplayName(a).localeCompare(this.getUserDisplayName(b));
          break;
        case 'status':
          cmp = a.status.localeCompare(b.status);
          break;
        case 'date':
          cmp = new Date(a.createdAt || 0).getTime() - new Date(b.createdAt || 0).getTime();
          break;
      }
      return cmp * dir;
    });
  }

  toggleSortDir(): void {
    this.userSortDir = this.userSortDir === 'asc' ? 'desc' : 'asc';
  }

  getUserDisplayName(user: AdminUserDto): string {
    if (user.role === 'PHARMACY' && user.pharmacyName) {
      return user.pharmacyName;
    }
    const prefix = user.role === 'DOCTOR' ? 'Dr. ' : '';
    return `${prefix}${user.firstName} ${user.lastName}`;
  }

  getUserRoleLabel(role: string): string {
    switch (role) {
      case 'DOCTOR': return 'Medecin';
      case 'PHARMACY': return 'Pharmacie';
      case 'PATIENT': return 'Patient';
      case 'ADMIN': return 'Admin';
      default: return role;
    }
  }

  getUserStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'Actif';
      case 'INACTIVE': return 'Inactif';
      case 'SUSPENDED': return 'Suspendu';
      case 'PENDING': return 'En attente';
      default: return status;
    }
  }

  getUserStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'st-active';
      case 'INACTIVE': return 'st-inactive';
      case 'SUSPENDED': return 'st-suspended';
      case 'PENDING': return 'st-pending';
      default: return '';
    }
  }

  activateUser(user: AdminUserDto): void {
    this.authService.updateUserStatus(user.id, { status: 'ACTIVE' }).subscribe({
      next: () => {
        user.status = 'ACTIVE';
        user.suspendUntil = undefined;
      },
      error: (err) => console.error('Erreur activation', err)
    });
  }

  deactivateUser(user: AdminUserDto): void {
    this.authService.updateUserStatus(user.id, { status: 'INACTIVE' }).subscribe({
      next: () => {
        user.status = 'INACTIVE';
        user.suspendUntil = undefined;
      },
      error: (err) => console.error('Erreur desactivation', err)
    });
  }

  openSuspendModal(user: AdminUserDto): void {
    this.suspendTarget = user;
    this.suspendDuration = 7;
    this.suspendUnit = 'DAYS';
    this.showSuspendModal = true;
  }

  closeSuspendModal(): void {
    this.showSuspendModal = false;
    this.suspendTarget = null;
    this.suspending = false;
  }

  confirmSuspend(): void {
    if (!this.suspendTarget) return;

    const now = new Date();
    const millis = this.suspendUnit === 'DAYS'
      ? this.suspendDuration * 24 * 60 * 60 * 1000
      : this.suspendDuration * 60 * 60 * 1000;
    const suspendUntil = new Date(now.getTime() + millis).toISOString();

    this.suspending = true;
    this.authService.updateUserStatus(this.suspendTarget.id, {
      status: 'SUSPENDED',
      suspendUntil
    }).subscribe({
      next: () => {
        if (this.suspendTarget) {
          this.suspendTarget.status = 'SUSPENDED';
          this.suspendTarget.suspendUntil = suspendUntil;
        }
        this.closeSuspendModal();
      },
      error: (err) => {
        console.error('Erreur suspension', err);
        this.suspending = false;
      }
    });
  }

  getSuspendLabel(user: AdminUserDto): string {
    if (!user.suspendUntil) return 'Suspendu';
    const until = new Date(user.suspendUntil);
    const now = new Date();
    if (until <= now) return 'Suspendu (expiré)';
    const days = Math.ceil((until.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    if (days >= 1) return `Suspendu (${days}j restant)`;
    const hours = Math.ceil((until.getTime() - now.getTime()) / (1000 * 60 * 60));
    return `Suspendu (${hours}h restant)`;
  }
}
