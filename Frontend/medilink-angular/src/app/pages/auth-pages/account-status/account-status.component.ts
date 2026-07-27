import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

type RoleKey = 'patient' | 'doctor' | 'pharmacy';
type StatusKey = 'SUSPENDED' | 'INACTIVE';

interface RoleTheme {
  primary: string;
  accent: string;
  dark: string;
  label: string;
}

/**
 * Page affichée quand la connexion est refusée pour un compte suspendu ou désactivé.
 * - SUSPENDED : compte à rebours jusqu'à la fin de suspension.
 * - INACTIVE  : message informatif (réactivation par l'administration).
 * Les couleurs s'adaptent au rôle de l'utilisateur.
 */
@Component({
  selector: 'app-account-status',
  templateUrl: './account-status.component.html',
  styleUrls: ['./account-status.component.scss']
})
export class AccountStatusComponent implements OnInit, OnDestroy {
  status: StatusKey = 'SUSPENDED';
  role: RoleKey = 'patient';
  suspendUntil: Date | null = null;

  // Compte à rebours
  days = 0;
  hours = 0;
  minutes = 0;
  seconds = 0;
  expired = false;

  private timer: any = null;

  private readonly themes: Record<RoleKey, RoleTheme> = {
    patient:  { primary: '#0066a2', accent: '#00a8b5', dark: '#0f2c4c', label: 'Patient' },
    doctor:   { primary: '#2e8b57', accent: '#3fae6c', dark: '#0f2c4c', label: 'Médecin' },
    pharmacy: { primary: '#6d28d9', accent: '#8b5cf6', dark: '#4c1d95', label: 'Pharmacie' }
  };

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const p = this.route.snapshot.queryParams;

    this.status = p['status'] === 'INACTIVE' ? 'INACTIVE' : 'SUSPENDED';

    const role = p['role'] as RoleKey;
    this.role = ['patient', 'doctor', 'pharmacy'].includes(role) ? role : 'patient';

    if (p['until']) {
      const d = new Date(p['until']);
      if (!isNaN(d.getTime())) {
        this.suspendUntil = d;
      }
    }

    if (this.status === 'SUSPENDED' && this.suspendUntil) {
      this.tick();
      this.timer = setInterval(() => this.tick(), 1000);
    }
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  get theme(): RoleTheme {
    return this.themes[this.role];
  }

  private tick(): void {
    if (!this.suspendUntil) return;

    const diff = this.suspendUntil.getTime() - Date.now();
    if (diff <= 0) {
      this.expired = true;
      this.days = this.hours = this.minutes = this.seconds = 0;
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
      return;
    }

    const totalSec = Math.floor(diff / 1000);
    this.days = Math.floor(totalSec / 86400);
    this.hours = Math.floor((totalSec % 86400) / 3600);
    this.minutes = Math.floor((totalSec % 3600) / 60);
    this.seconds = totalSec % 60;
  }

  pad(n: number): string {
    return n < 10 ? '0' + n : '' + n;
  }

  goToLogin(): void {
    this.router.navigate(['/auth/login']);
  }
}
