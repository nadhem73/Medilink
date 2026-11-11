import { Component, OnInit, OnDestroy } from '@angular/core';
import { MonitoringService, OverviewDto, ServiceHealthDto, MetricPointDto, TimeRange } from '../core/services/monitoring.service';

@Component({
  selector: 'app-monitoring-overview',
  templateUrl: './monitoring-overview.component.html',
  styleUrls: ['./monitoring-overview.component.scss']
})
export class MonitoringOverviewComponent implements OnInit, OnDestroy {
  overview: OverviewDto | null = null;
  services: ServiceHealthDto[] = [];
  loading = true;
  error = '';
  private sse?: EventSource;

  overviewCards: CardData[] = [];
  expandedService: string | null = null;

  selectedTimeRange: TimeRange = '1h';
  timeRanges: { key: TimeRange; label: string }[] = [
    { key: '30m', label: '30 min' },
    { key: '1h', label: '1 heure' },
    { key: '6h', label: '6 heures' },
    { key: '24h', label: '24 heures' },
    { key: '7d', label: '7 jours' },
    { key: '30d', label: '30 jours' },
  ];

  historyCpu: MetricPointDto[] = [];
  historyRam: MetricPointDto[] = [];
  historyResponse: MetricPointDto[] = [];
  historyDisk: MetricPointDto[] = [];
  historyRequests: MetricPointDto[] = [];
  historyErrors: MetricPointDto[] = [];

  gaugeCpu = 0;
  gaugeRam = 0;
  gaugeDisk = 0;

  constructor(private monitoringService: MonitoringService) {}

  ngOnInit(): void {
    this.loadData();
    this.loadHistory();
    this.connectSse();
  }

  ngOnDestroy(): void {
    this.sse?.close();
  }

  onTimeRangeChange(): void {
    this.loadHistory();
  }

  loadData(): void {
    this.monitoringService.getOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.buildCards(data);
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les métriques.';
        this.loading = false;
      }
    });
    this.monitoringService.getServices().subscribe({
      next: (data) => { this.services = data; },
      error: () => {}
    });
  }

  loadHistory(): void {
    const { from, to } = this.monitoringService.getTimeRangeDates(this.selectedTimeRange);

    this.monitoringService.getMetricsRange(undefined, 'cpu', from, to).subscribe({
      next: (data) => { this.historyCpu = data; this.computeGauges(); this.rebuildCards(); },
      error: () => {}
    });
    this.monitoringService.getMetricsRange(undefined, 'ram', from, to).subscribe({
      next: (data) => { this.historyRam = data; this.computeGauges(); this.rebuildCards(); },
      error: () => {}
    });
    this.monitoringService.getMetricsRange(undefined, 'response_time', from, to).subscribe({
      next: (data) => { this.historyResponse = data; this.rebuildCards(); },
      error: () => {}
    });
    this.monitoringService.getMetricsRange(undefined, 'disk', from, to).subscribe({
      next: (data) => { this.historyDisk = data; this.computeGauges(); this.rebuildCards(); },
      error: () => {}
    });
    this.monitoringService.getMetricsRange(undefined, 'requests', from, to).subscribe({
      next: (data) => { this.historyRequests = data; this.rebuildCards(); },
      error: () => {}
    });
    this.monitoringService.getMetricsRange(undefined, 'errors', from, to).subscribe({
      next: (data) => { this.historyErrors = data; this.rebuildCards(); },
      error: () => {}
    });
  }

  computeGauges(): void {
    if (this.historyCpu.length > 0) {
      this.gaugeCpu = this.historyCpu[this.historyCpu.length - 1].value;
    }
    if (this.historyRam.length > 0) {
      this.gaugeRam = this.historyRam[this.historyRam.length - 1].value;
    }
    if (this.historyDisk.length > 0) {
      this.gaugeDisk = this.historyDisk[this.historyDisk.length - 1].value;
    } else if (this.overview) {
      this.gaugeDisk = this.overview.diskPercent;
    }
  }

  getHistoryPoints(data: MetricPointDto[]): string {
    if (data.length < 2) return '';
    const values = data.map(d => d.value);
    const min = Math.min(...values);
    const max = Math.max(...values);
    const range = max - min || 1;
    const w = 100, h = 40;
    return data.map((d, i) => {
      const x = (i / (data.length - 1)) * w;
      const y = h - ((d.value - min) / range) * (h - 4) - 2;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    }).join(' ');
  }

  getHistoryFill(data: MetricPointDto[]): string {
    if (data.length < 2) return '';
    const pts = this.getHistoryPoints(data);
    const firstX = '0,40';
    const lastX = '100,40';
    return `${firstX} ${pts} ${lastX}`;
  }

  gaugeColor(val: number): string {
    if (val > 90) return '#ef4444';
    if (val > 80) return '#f59e0b';
    if (val > 60) return '#3b82f6';
    return '#22c55e';
  }

  getSparklinePoints(data: number[]): string {
    if (!data || data.length < 2) return '';
    const w = 100, h = 30;
    const min = Math.min(...data), max = Math.max(...data);
    const range = max - min || 1;
    return data.map((v, i) => {
      const x = (i / (data.length - 1)) * w;
      const y = h - ((v - min) / range) * (h - 4) - 2;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    }).join(' ');
  }

  gaugeStrokeDash(val: number): string {
    const circumference = 132;
    const offset = circumference - (val / 100) * circumference;
    return `${offset} ${circumference}`;
  }

  connectSse(): void {
    this.sse = this.monitoringService.createEventSource();
    this.sse.addEventListener('overview', (event) => {
      const data: OverviewDto = JSON.parse(event.data);
      this.overview = data;
      this.buildCards(data);
      this.computeGauges();
    });
    this.sse.addEventListener('services', (event) => {
      this.services = JSON.parse(event.data);
    });
  }

  toggleService(name: string): void {
    this.expandedService = this.expandedService === name ? null : name;
  }

  getServiceStatusClass(status: string): string {
    switch (status) {
      case 'Online': return 'st-healthy';
      case 'Degraded': return 'st-warning';
      default: return 'st-down';
    }
  }

  private getSparklineFromHistory(data: MetricPointDto[], count: number = 7): number[] {
    if (!data || data.length === 0) {
      return [50, 50, 50, 50, 50, 50, 50];
    }
    const step = Math.max(1, Math.floor(data.length / count));
    const sampled: number[] = [];
    for (let i = 0; i < data.length; i += step) {
      sampled.push(Math.round(data[i].value * 10) / 10);
    }
    while (sampled.length < count) sampled.unshift(sampled[0] ?? 0);
    return sampled.slice(-count);
  }

private rebuildCards(): void {
    if (this.overview) {
      this.buildCards(this.overview);
    }
  }

  private buildCards(data: OverviewDto): void {
    this.overviewCards = [
      {
        icon: 'server', label: 'Services actifs', value: `${data.activeServices}/${data.totalServices}`,
        color: '#22c55e', variation: data.variation.activeServices,
        sparkline: this.getSparklineFromHistory(this.historyCpu)
      },
      {
        icon: 'activity', label: 'Disponibilité', value: `${data.uptimePercent}%`,
        color: '#3b82f6', variation: data.variation.uptimePercent,
        sparkline: this.getSparklineFromHistory(this.historyRam)
      },
      {
        icon: 'clock', label: 'Temps réponse moyen', value: `${data.avgResponseTimeMs}ms`,
        color: '#f59e0b', variation: data.variation.avgResponseTimeMs,
        sparkline: this.getSparklineFromHistory(this.historyResponse)
      },
      {
        icon: 'activity', label: 'Requêtes aujourd\'hui', value: data.totalRequestsToday.toLocaleString(),
        color: '#8b5cf6', variation: data.variation.totalRequestsToday,
        sparkline: this.getSparklineFromHistory(this.historyRequests)
      },
      {
        icon: 'alert-circle', label: 'Erreurs aujourd\'hui', value: data.errorsToday.toLocaleString(),
        color: '#ef4444', variation: data.variation.errorsToday,
        sparkline: this.getSparklineFromHistory(this.historyErrors)
      },
      {
        icon: 'users', label: 'Utilisateurs connectés', value: data.connectedUsers.toLocaleString(),
        color: '#06b6d4', variation: 0,
        sparkline: this.getSparklineFromHistory(this.historyDisk)
      },
      {
        icon: 'file-text', label: 'Documents OCR', value: data.documentsOcrProcessed.toLocaleString(),
        color: '#10b981', variation: 0,
        sparkline: this.getSparklineFromHistory(this.historyDisk)
      },
      {
        icon: 'bell', label: 'Notifications envoyées', value: data.notificationsSent.toLocaleString(),
        color: '#f97316', variation: 0,
        sparkline: this.getSparklineFromHistory(this.historyCpu)
      }
    ];
  }

  private readonly SPARKLINE_COUNT = 7;
}

interface CardData {
  icon: string;
  label: string;
  value: string;
  color: string;
  variation: number;
  sparkline: number[];
}
