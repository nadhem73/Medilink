import { Component, OnInit } from '@angular/core';
import { PrescriptionService } from '../../../core/services/prescription.service';

@Component({
  selector: 'app-pharmacy-alerts',
  templateUrl: './alerts.component.html',
  styleUrls: ['./alerts.component.scss']
})
export class AlertsComponent implements OnInit {

  activeTab: 'rupture' | 'perimes' = 'rupture';
  ruptureAlerts: any[] = [];
  perimesAlerts: any[] = [];
  loading = true;

  searchQuery = '';
  sortBy = 'name';
  page = 0;
  readonly pageSize = 10;

  constructor(private prescriptionService: PrescriptionService) {}

  ngOnInit(): void {
    this.loadAlerts();
  }

  private loadAlerts(): void {
    this.loading = true;
    this.prescriptionService.getAlertsRupture(20).subscribe({
      next: (r) => {
        this.ruptureAlerts = r;
        this.prescriptionService.getAlertsPerimes(30).subscribe({
          next: (p) => { this.perimesAlerts = p; this.loading = false; },
          error: () => { this.perimesAlerts = []; this.loading = false; }
        });
      },
      error: () => { this.ruptureAlerts = []; this.loading = false; }
    });
  }

  get ruptureCount(): number {
    return this.ruptureAlerts.length;
  }

  get perimesCount(): number {
    return this.perimesAlerts.length;
  }

  private getFilteredSortedList(): any[] {
    const isRupture = this.activeTab === 'rupture';
    let list = isRupture ? [...this.ruptureAlerts] : [...this.perimesAlerts];
    const q = this.searchQuery.trim().toLowerCase();
    if (q) list = list.filter(r => (r.medicamentName || '').toLowerCase().includes(q));
    if (isRupture) {
      switch (this.sortBy) {
        case 'stock-asc': list.sort((a, b) => a.stockTotal - b.stockTotal); break;
        case 'stock-desc': list.sort((a, b) => b.stockTotal - a.stockTotal); break;
        default: list.sort((a, b) => (a.medicamentName || '').localeCompare(b.medicamentName || '', 'fr', { numeric: true }));
      }
    } else {
      switch (this.sortBy) {
        case 'expiry': list.sort((a, b) => new Date(a.dateExpiration).getTime() - new Date(b.dateExpiration).getTime()); break;
        case 'qty': list.sort((a, b) => b.quantiteEnStock - a.quantiteEnStock); break;
        default: list.sort((a, b) => (a.medicamentName || '').localeCompare(b.medicamentName || '', 'fr', { numeric: true }));
      }
    }
    return list;
  }

  get currentItems(): any[] {
    const full = this.getFilteredSortedList();
    const start = this.page * this.pageSize;
    return full.slice(start, start + this.pageSize);
  }

  get totalItems(): number {
    return this.getFilteredSortedList().length;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.totalItems / this.pageSize));
  }

  goTo(p: number): void {
    this.page = Math.max(0, Math.min(p, this.totalPages - 1));
  }

  onSearchOrSortChange(): void {
    this.page = 0;
  }

  switchTab(tab: 'rupture' | 'perimes'): void {
    this.activeTab = tab;
    this.sortBy = 'name';
    this.searchQuery = '';
    this.page = 0;
  }

  get sortOptions(): { value: string; label: string }[] {
    return this.activeTab === 'rupture'
      ? [{ value: 'name', label: 'Nom A–Z' }, { value: 'stock-asc', label: 'Stock croissant' }, { value: 'stock-desc', label: 'Stock décroissant' }]
      : [{ value: 'name', label: 'Nom A–Z' }, { value: 'expiry', label: 'Expiration proche' }, { value: 'qty', label: 'Quantité' }];
  }

  joursRestants(dateExpiration: string): number {
    if (!dateExpiration) return 0;
    const today = new Date();
    const exp = new Date(dateExpiration);
    return Math.ceil((exp.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }

  trackById(_i: number, item: any): any {
    return item.medicamentId || item.id;
  }
}
