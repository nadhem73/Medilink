import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { MonitoringService, SecurityOverviewDto, AlertDto } from '../core/services/monitoring.service';

@Component({
  selector: 'app-security-dashboard',
  templateUrl: './security-dashboard.component.html',
  styleUrls: ['./security-dashboard.component.scss']
})
export class SecurityDashboardComponent implements OnInit, OnDestroy {
  security: SecurityOverviewDto | null = null;
  alerts: AlertDto[] = [];
  filteredAlerts: AlertDto[] = [];
  alertFilter: string = 'ALL';
  loading: boolean = true;
  error: string | null = null;
  ackLoading: Set<string> = new Set();

  private destroy$ = new Subject<void>();

  stats: { label: string; value: number; accent: string }[] = [];

  constructor(private monitoring: MonitoringService) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadData(): void {
    this.loading = true;
    this.error = null;
    forkJoin({
      security: this.monitoring.getSecurity(),
      alerts: this.monitoring.getAlerts()
    }).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.loading = false)
    ).subscribe({
      next: ({ security, alerts }) => {
        this.security = security;
        this.alerts = alerts;
        this.applyFilter();
        this.buildStats(security);
      },
      error: () => this.error = 'Impossible de charger les donnees de securite.'
    });
  }

  private buildStats(s: SecurityOverviewDto): void {
    this.stats = [
      { label: 'Tentatives de connexion', value: s.loginAttempts, accent: 'gold' },
      { label: 'Echecs authentification', value: s.failedLogins, accent: 'red' },
      { label: 'Comptes verrouilles', value: s.lockedAccounts, accent: 'red' },
      { label: 'Sessions actives', value: s.activeSessions, accent: 'green' },
      { label: 'Activites suspectes', value: s.suspiciousActivities, accent: 'orange' }
    ];
  }

  setFilter(status: string): void {
    this.alertFilter = status;
    this.applyFilter();
  }

  private applyFilter(): void {
    if (this.alertFilter === 'ALL') {
      this.filteredAlerts = [...this.alerts];
    } else {
      this.filteredAlerts = this.alerts.filter(a => a.status === this.alertFilter);
    }
  }

  acknowledge(id: string): void {
    if (this.ackLoading.has(id)) return;
    this.ackLoading.add(id);
    this.monitoring.acknowledgeAlert(id).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.ackLoading.delete(id))
    ).subscribe({
      next: (updated) => {
        const idx = this.alerts.findIndex(a => a.id === id);
        if (idx !== -1) this.alerts[idx] = updated;
        this.applyFilter();
      }
    });
  }

  getAttemptLevel(attempts: number): string {
    if (attempts >= 50) return 'critical';
    if (attempts >= 20) return 'high';
    if (attempts >= 10) return 'medium';
    return 'low';
  }

  priorityClass(p: string): string {
    switch (p) {
      case 'CRITICAL': return 'pr-crit';
      case 'HIGH': return 'pr-high';
      case 'MEDIUM': return 'pr-med';
      default: return 'pr-low';
    }
  }
}
