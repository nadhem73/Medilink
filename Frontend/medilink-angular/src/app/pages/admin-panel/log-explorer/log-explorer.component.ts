import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MonitoringService, LogEntryDto, LogPageDto } from '../core/services/monitoring.service';

@Component({
  selector: 'app-log-explorer',
  templateUrl: './log-explorer.component.html',
  styleUrls: ['./log-explorer.component.scss']
})
export class LogExplorerComponent implements OnInit, OnDestroy {
  logs: LogEntryDto[] = [];
  totalCount = 0;
  page = 0;
  pageSize = 20;
  totalPages = 0;

  filterService = '';
  filterLevel = '';
  filterSearch = '';
  filterDateFrom = '';
  filterDateTo = '';

  pages: number[] = [];

  loading = false;
  error = '';

  expandedId: string | null = null;

  services: string[] = ['auth-service', 'prescription-service', 'pharmacy-service', 'bilan-service', 'ai-service', 'api-gateway'];
  levels: string[] = ['INFO', 'WARNING', 'ERROR', 'CRITICAL'];

  private searchSubject = new Subject<string>();
  private searchSub?: Subscription;

  constructor(private monitoringService: MonitoringService) {}

  ngOnInit(): void {
    this.searchSub = this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => this.loadLogs());
    this.loadLogs();
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
  }

  onSearchChange(value: string): void {
    this.filterSearch = value;
    this.searchSubject.next(value);
  }

  loadLogs(): void {
    this.loading = true;
    this.error = '';
    this.expandedId = null;

    this.monitoringService.getLogs({
      service: this.filterService || undefined,
      level: this.filterLevel || undefined,
      search: this.filterSearch || undefined,
      dateFrom: this.filterDateFrom || undefined,
      dateTo: this.filterDateTo || undefined,
      page: this.page,
      pageSize: this.pageSize
    }).subscribe({
      next: (data: LogPageDto) => {
        this.logs = data.logs;
        this.totalCount = data.totalCount;
        this.page = data.page;
        this.pageSize = data.pageSize;
        this.totalPages = data.totalPages;
        this.buildPages();
        this.loading = false;
      },
      error: (err) => {
        console.error('Log loading error', err);
        this.error = 'Impossible de charger les logs.';
        this.loading = false;
      }
    });
  }

  goToPage(p: number): void {
    if (p < 0 || p >= this.totalPages || p === this.page) return;
    this.page = p;
    this.loadLogs();
  }

  toggleExpand(id: string): void {
    this.expandedId = this.expandedId === id ? null : id;
  }

  exportCsv(): void {
    const header = 'Date,Heure,Service,Niveau,Message,Utilisateur,IP,TraceId';
    const rows = this.logs.map(l =>
      `"${l.date}","${l.time}","${l.service}","${l.level}","${l.message.replace(/"/g, '""')}","${l.user}","${l.ipAddress}","${l.traceId}"`
    );
    const csv = [header, ...rows].join('\r\n');
    const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `logs-${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  resetFilters(): void {
    this.filterService = '';
    this.filterLevel = '';
    this.filterSearch = '';
    this.filterDateFrom = '';
    this.filterDateTo = '';
    this.page = 0;
    this.loadLogs();
  }

  getLevelClass(level: string): string {
    switch (level) {
      case 'INFO': return 'level-info';
      case 'WARNING': return 'level-warning';
      case 'ERROR': return 'level-error';
      case 'CRITICAL': return 'level-critical';
      default: return '';
    }
  }

  private buildPages(): void {
    const p: number[] = [];
    const maxVisible = 7;
    const half = Math.floor(maxVisible / 2);
    let start = Math.max(0, this.page - half);
    const end = Math.min(this.totalPages, start + maxVisible);
    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }
    for (let i = start; i < end; i++) p.push(i);
    this.pages = p;
  }
}
