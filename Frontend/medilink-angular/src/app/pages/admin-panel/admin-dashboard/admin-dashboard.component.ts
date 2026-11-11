import { Component, OnDestroy, OnInit } from '@angular/core';
import { forkJoin, of, Subscription, timer } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import {
  AlertDto,
  MonitoringService,
  OverviewDto,
  PlatformStatsDto,
  ServiceHealthDto
} from '../core/services/monitoring.service';

interface SummaryCard { title: string; value: string; detail: string; accent: string; }
interface HealthRow { name: string; instruction: string; nextDose: string; }
interface TimelineItem { title: string; meta: string; description: string; }

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  currentUser: any;
  loading = true;
  loadError = false;

  platformState = 'Chargement...';
  platformStateDetail = '';

  summaryCards: SummaryCard[] = [];
  systemHealth: HealthRow[] = [];
  activityTimeline: TimelineItem[] = [];

  quickActions = [
    {
      title: 'Gerer les utilisateurs',
      description: 'Rechercher, suspendre ou reactiver les comptes de la plateforme.',
      route: '/dashboard/admin/users',
      cta: 'Voir les utilisateurs'
    },
    {
      title: 'Consulter les rapports analytics',
      description: 'Analyser la croissance, les rendez-vous et l activite globale.',
      route: '/dashboard/admin/analytics',
      cta: 'Voir les rapports'
    }
  ];

  team = [
    { name: 'Super administrateur', role: 'Acces complet plateforme' },
    { name: 'Equipe support', role: 'Assistance utilisateurs' },
    { name: 'Equipe securite', role: 'Audit et conformite' }
  ];

  private sub?: Subscription;

  constructor(private authService: AuthService, private monitoring: MonitoringService) {
    this.currentUser = this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    // Rafraîchissement toutes les 30 s
    this.sub = timer(0, 30000).pipe(
      switchMap(() => forkJoin({
        overview: this.monitoring.getOverview().pipe(catchError(() => of(null))),
        services: this.monitoring.getServices().pipe(catchError(() => of([] as ServiceHealthDto[]))),
        platform: this.monitoring.getPlatformStats().pipe(catchError(() => of(null))),
        alerts: this.monitoring.getAlerts().pipe(catchError(() => of([] as AlertDto[])))
      }))
    ).subscribe(({ overview, services, platform, alerts }) => {
      this.loading = false;
      this.loadError = !overview && !platform;
      this.buildCards(overview, platform);
      this.buildHealth(overview, services);
      this.buildTimeline(alerts);
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  private buildCards(o: OverviewDto | null, p: PlatformStatsDto | null): void {
    this.summaryCards = [
      {
        title: 'Utilisateurs actifs',
        value: p ? this.fmt(p.activeUsers) : '—',
        detail: p ? `+${p.newUsersThisWeek} cette semaine · ${o?.connectedUsers ?? 0} connecte(s)` : 'Donnees indisponibles',
        accent: 'gold'
      },
      {
        title: 'Comptes pro a valider',
        value: p ? this.fmt(p.pendingProAccounts) : '—',
        detail: p ? `${p.doctors} medecins · ${p.pharmacies} pharmacies inscrits` : 'Donnees indisponibles',
        accent: 'red'
      },
      {
        title: 'Disponibilite systeme',
        value: o && o.totalServices ? `${o.uptimePercent} %` : '—',
        detail: o && o.totalServices ? `${o.activeServices} / ${o.totalServices} services en ligne` : 'Monitoring indisponible',
        accent: 'blue'
      },
      {
        title: 'Incidents securite',
        value: p ? this.fmt(p.suspiciousIps24h) : '—',
        detail: p ? `IP suspecte(s) · ${p.failedLogins24h} echec(s) de connexion en 24 h` : 'Donnees indisponibles',
        accent: 'green'
      }
    ];
  }

  private buildHealth(o: OverviewDto | null, services: ServiceHealthDto[]): void {
    if (!o || !o.totalServices) {
      this.platformState = 'Indisponible';
      this.platformStateDetail = 'Le service de monitoring ne repond pas.';
    } else if (o.offlineServices > 0) {
      this.platformState = 'Degrade';
      this.platformStateDetail = `${o.offlineServices} service(s) hors ligne sur ${o.totalServices}.`;
    } else if (o.activeServices < o.totalServices) {
      this.platformState = 'Degrade';
      this.platformStateDetail = `${o.totalServices - o.activeServices} service(s) en etat degrade.`;
    } else {
      this.platformState = 'Operationnel';
      this.platformStateDetail = `Les ${o.totalServices} services repondent normalement.`;
    }

    this.systemHealth = services.map(s => ({
      name: s.name,
      instruction: s.status === 'Offline'
        ? 'Aucune reponse au health check'
        : `${Math.round(s.responseTimeMs)} ms · CPU ${s.cpuPercent.toFixed(1)} % · ${this.fmt(s.requestsCount)} requetes`,
      nextDose: s.status === 'Online' ? 'Operationnel' : s.status === 'Degraded' ? 'Degrade' : 'Hors ligne'
    }));
  }

  private buildTimeline(alerts: AlertDto[]): void {
    this.activityTimeline = alerts.slice(0, 5).map(a => ({
      title: a.title,
      meta: `${a.service} - ${this.relative(a.date)} - ${this.statusLabel(a.status)}`,
      description: a.description
    }));
  }

  private statusLabel(status: string): string {
    switch (status) {
      case 'OPEN': return 'ouverte';
      case 'ACKNOWLEDGED': return 'acquittee';
      case 'RESOLVED': return 'resolue';
      default: return status.toLowerCase();
    }
  }

  private relative(iso: string): string {
    const diff = Date.now() - new Date(iso).getTime();
    if (isNaN(diff)) return iso;
    const min = Math.round(diff / 60000);
    if (min < 1) return 'a l instant';
    if (min < 60) return `il y a ${min} min`;
    const h = Math.round(min / 60);
    if (h < 24) return `il y a ${h} h`;
    return `il y a ${Math.round(h / 24)} j`;
  }

  private fmt(n: number): string {
    return (n ?? 0).toLocaleString('fr-FR');
  }
}
