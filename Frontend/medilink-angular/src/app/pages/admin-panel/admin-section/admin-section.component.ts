import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService, AdminUserDto } from '../../../core/services/auth.service';

type AdminSectionKey = 'users' | 'monitoring' | 'security' | 'logs' | 'analytics';

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

  // Monitoring systeme en temps reel (sante des microservices)
  monitoring = [
    { name: 'Auth service', metric: 'Disponibilite 99.98 %', sub: 'Latence 64 ms', status: 'Operationnel' },
    { name: 'API Gateway', metric: 'Latence moyenne 82 ms', sub: '1 240 req/min', status: 'Operationnel' },
    { name: 'Service notifications', metric: 'File d attente : 14 messages', sub: 'Debit reduit', status: 'Degrade' },
    { name: 'Pharmacy service', metric: 'Disponibilite 99.91 %', sub: 'CPU 38 %', status: 'Operationnel' },
    { name: 'Base de donnees', metric: 'Connexions 142/200', sub: 'Stockage 61 %', status: 'Operationnel' }
  ];

  monitoringKpis = [
    { label: 'Services en ligne', value: '11 / 12' },
    { label: 'Disponibilite globale', value: '99.94 %' },
    { label: 'Requetes / min', value: '4 820' },
    { label: 'Temps de reponse moyen', value: '78 ms' }
  ];

  // Securite & politiques d'acces
  accessPolicies = [
    { name: 'Authentification a deux facteurs', detail: 'Obligatoire pour les comptes professionnels et admin', status: 'Active' },
    { name: 'Expiration des sessions', detail: 'Deconnexion automatique apres 30 min d inactivite', status: 'Active' },
    { name: 'Politique de mots de passe', detail: 'Minimum 12 caracteres, rotation tous les 90 jours', status: 'Active' },
    { name: 'Liste de blocage IP', detail: '3 adresses actuellement bloquees', status: 'Surveillance' }
  ];

  securityIncidents = [
    { title: 'Connexions echouees repetees', meta: 'IP 41.226.x.x', level: 'Eleve' },
    { title: 'Acces refuse a une ressource admin', meta: 'Compte medecin', level: 'Moyen' },
    { title: 'Nouvel appareil detecte', meta: 'Compte administrateur', level: 'Info' }
  ];

  // Logs d'activite
  logs = [
    { action: 'Connexion administrateur', source: 'IP 196.203.x.x', date: "Aujourd hui 09:12", result: 'Succes', note: 'Session ouverte depuis Tunis.' },
    { action: 'Echecs de connexion repetes', source: 'IP 41.226.x.x', date: "Aujourd hui 03:41", result: 'Bloque', note: 'Adresse mise en liste de surveillance.' },
    { action: 'Validation compte medecin', source: 'Module admin', date: 'Hier 16:20', result: 'Succes', note: 'Dr. Leila Mansour approuvee.' },
    { action: 'Modification politique d acces', source: 'Module securite', date: 'Hier 11:05', result: 'Succes', note: '2FA rendu obligatoire pour les pros.' }
  ];

  // Rapports analytics
  analytics = [
    { label: 'Utilisateurs totaux', value: '12 480' },
    { label: 'Medecins actifs', value: '1 240' },
    { label: 'Pharmacies', value: '320' },
    { label: 'Laboratoires', value: '96' },
    { label: 'Rendez-vous ce mois', value: '8 932' },
    { label: 'Teleconsultations', value: '2 145' }
  ];

  analyticsReports = [
    { title: 'Rapport mensuel d activite', detail: 'Synthese des inscriptions, rendez-vous et revenus.' },
    { title: 'Croissance des comptes pro', detail: 'Evolution des medecins, pharmacies et laboratoires.' },
    { title: 'Taux d adoption teleconsultation', detail: 'Part des consultations realisees a distance.' }
  ];

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.section = data['section'] as AdminSectionKey;
      this.title = data['title'] as string;

      if (this.section === 'users') {
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
