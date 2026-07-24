import { Injectable, NgZone } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OverviewDto {
  totalServices: number;
  activeServices: number;
  offlineServices: number;
  uptimePercent: number;
  avgResponseTimeMs: number;
  totalRequestsToday: number;
  errorsToday: number;
  connectedUsers: number;
  diskPercent: number;
  documentsOcrProcessed: number;
  notificationsSent: number;
  variation: {
    activeServices: number;
    uptimePercent: number;
    avgResponseTimeMs: number;
    totalRequestsToday: number;
    errorsToday: number;
  };
}

export interface ServiceHealthDto {
  name: string;
  status: string;
  cpuPercent: number;
  ramPercent: number;
  responseTimeMs: number;
  requestsCount: number;
  errorsCount: number;
  uptimeHours: number;
  lastRestart: string;
  version: string;
}

export interface LogEntryDto {
  id: string;
  date: string;
  time: string;
  service: string;
  level: string;
  message: string;
  user: string;
  ipAddress: string;
  traceId: string;
}

export interface LogPageDto {
  logs: LogEntryDto[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface SecurityOverviewDto {
  loginAttempts: number;
  failedLogins: number;
  lockedAccounts: number;
  expiredJwt: number;
  activeSessions: number;
  expiredSessions: number;
  suspiciousActivities: number;
  topSuspiciousIps: { ip: string; attempts: number; lastAttempt: string; location: string }[];
}

export interface AlertDto {
  id: string;
  title: string;
  description: string;
  priority: string;
  date: string;
  status: string;
  service: string;
}

export interface HistoryPointDto {
  date: string;
  value: number;
}

export interface IncidentDto {
  date: string;
  service: string;
  description: string;
  severity: string;
  resolutionTimeHours: number;
}

export interface AnalyticsDto {
  uptime30Days: HistoryPointDto[];
  cpuEvolution: HistoryPointDto[];
  ramEvolution: HistoryPointDto[];
  storageEvolution: HistoryPointDto[];
  incidents: IncidentDto[];
  avgResolutionTimeHours: number;
}

export interface MetricPointDto {
  timestamp: string;
  value: number;
  serviceName: string;
}

export interface MetricSummaryDto {
  avgCpu: number;
  avgRam: number;
  avgDisk: number;
  avgResponseTimeMs: number;
  totalRequests: number;
  totalErrors: number;
}

export type TimeRange = '30m' | '1h' | '6h' | '24h' | '7d' | '30d' | 'custom';

@Injectable({ providedIn: 'root' })
export class MonitoringService {
  private readonly baseUrl = '/api/monitoring';

  constructor(private http: HttpClient, private zone: NgZone) {}

  getOverview(): Observable<OverviewDto> {
    return this.http.get<OverviewDto>(`${this.baseUrl}/overview`);
  }

  getServices(): Observable<ServiceHealthDto[]> {
    return this.http.get<ServiceHealthDto[]>(`${this.baseUrl}/services`);
  }

  getLogs(params?: {
    service?: string; level?: string; search?: string;
    dateFrom?: string; dateTo?: string; page?: number; pageSize?: number;
  }): Observable<LogPageDto> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== '') httpParams = httpParams.set(k, v);
      });
    }
    return this.http.get<LogPageDto>(`${this.baseUrl}/logs`, { params: httpParams });
  }

  getSecurity(): Observable<SecurityOverviewDto> {
    return this.http.get<SecurityOverviewDto>(`${this.baseUrl}/security`);
  }

  getAlerts(priority?: string, status?: string): Observable<AlertDto[]> {
    let params = new HttpParams();
    if (priority) params = params.set('priority', priority);
    if (status) params = params.set('status', status);
    return this.http.get<AlertDto[]>(`${this.baseUrl}/alerts`, { params });
  }

  acknowledgeAlert(id: string): Observable<AlertDto> {
    return this.http.post<AlertDto>(`${this.baseUrl}/alerts/${id}/acknowledge`, {});
  }

  getHistory(): Observable<AnalyticsDto> {
    return this.http.get<AnalyticsDto>(`${this.baseUrl}/history`);
  }

  getMetricsRange(service: string | undefined, metric: string, from: string, to: string): Observable<MetricPointDto[]> {
    let params = new HttpParams().set('metric', metric).set('from', from).set('to', to);
    if (service) params = params.set('service', service);
    return this.http.get<MetricPointDto[]>(`${this.baseUrl}/metrics/range`, { params });
  }

  getMetricsSummary(from: string, to: string): Observable<MetricSummaryDto> {
    let params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<MetricSummaryDto>(`${this.baseUrl}/metrics/summary`, { params });
  }

  exportMetricsCsv(from: string, to: string): Observable<Blob> {
    let params = new HttpParams().set('from', from).set('to', to);
    return this.http.get(`${this.baseUrl}/metrics/export`, {
      params,
      responseType: 'blob'
    });
  }

  getMetricsLatest(): Observable<OverviewDto> {
    return this.http.get<OverviewDto>(`${this.baseUrl}/metrics/latest`);
  }

  createEventSource(): EventSource {
    return new EventSource(`${this.baseUrl}/stream`);
  }

  getTimeRangeDates(range: TimeRange, customFrom?: string, customTo?: string): { from: string; to: string } {
    const now = new Date();
    const to = customTo || now.toISOString();
    let from: Date;
    switch (range) {
      case '30m': from = new Date(now.getTime() - 30 * 60000); break;
      case '1h': from = new Date(now.getTime() - 60 * 60000); break;
      case '6h': from = new Date(now.getTime() - 6 * 3600000); break;
      case '24h': from = new Date(now.getTime() - 24 * 3600000); break;
      case '7d': from = new Date(now.getTime() - 7 * 86400000); break;
      case '30d': from = new Date(now.getTime() - 30 * 86400000); break;
      case 'custom': from = customFrom ? new Date(customFrom) : new Date(now.getTime() - 3600000); break;
      default: from = new Date(now.getTime() - 3600000);
    }
    return { from: from.toISOString(), to };
  }
}
