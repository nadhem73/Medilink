import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PrescriptionService } from '../../../core/services/prescription.service';
import { AlertsComponent } from './alerts.component';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('AlertsComponent', () => {
  let component: AlertsComponent;
  let fixture: ComponentFixture<AlertsComponent>;
  let prescriptionServiceSpy: jasmine.SpyObj<PrescriptionService>;

  const mockRuptureAlerts = [
    { medicamentId: 1, medicamentName: 'DOLIPRANE', stockTotal: 5, status: 'Critique' },
    { medicamentId: 2, medicamentName: 'AMOXICILLINE', stockTotal: 0, status: 'Rupture' },
    { medicamentId: 3, medicamentName: 'ASPIRINE', stockTotal: 12, status: 'Faible' },
  ];

  const mockPerimesAlerts = [
    { id: 10, medicamentId: 1, medicamentName: 'DOLIPRANE', numeroLot: 'LOT-2401-2701', quantiteEnStock: 100, dateExpiration: new Date(Date.now() + 5 * 86400000).toISOString(), emplacement: 'A12' },
    { id: 11, medicamentId: 2, medicamentName: 'AMOXICILLINE', numeroLot: 'LOT-2402-2702', quantiteEnStock: 50, dateExpiration: new Date(Date.now() + 20 * 86400000).toISOString(), emplacement: 'B12' },
    { id: 12, medicamentId: 3, medicamentName: 'VITAMINE C', numeroLot: 'LOT-2403-2703', quantiteEnStock: 200, dateExpiration: new Date(Date.now() + 10 * 86400000).toISOString(), emplacement: 'C12' },
  ];

  const today = new Date();

  beforeEach(async () => {
    prescriptionServiceSpy = jasmine.createSpyObj('PrescriptionService', [
      'getAlertsRupture',
      'getAlertsPerimes'
    ]);

    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [AlertsComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: PrescriptionService, useValue: prescriptionServiceSpy }
      ]
    }).compileComponents();
  });

  function setupAlerts() {
    prescriptionServiceSpy.getAlertsRupture.and.returnValue(of(mockRuptureAlerts as any));
    prescriptionServiceSpy.getAlertsPerimes.and.returnValue(of(mockPerimesAlerts as any));

    fixture = TestBed.createComponent(AlertsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', () => {
    setupAlerts();
    expect(component).toBeTruthy();
  });

  it('should load alerts on init', () => {
    setupAlerts();
    expect(prescriptionServiceSpy.getAlertsRupture).toHaveBeenCalledWith(20);
    expect(prescriptionServiceSpy.getAlertsPerimes).toHaveBeenCalledWith(30);
    expect(component.ruptureAlerts.length).toBe(3);
    expect(component.perimesAlerts.length).toBe(3);
    expect(component.loading).toBeFalse();
  });

  it('should handle rupture alerts error gracefully', () => {
    prescriptionServiceSpy.getAlertsRupture.and.returnValue(throwError(() => new Error('error')));
    prescriptionServiceSpy.getAlertsPerimes.and.returnValue(of(mockPerimesAlerts as any));

    fixture = TestBed.createComponent(AlertsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.ruptureAlerts).toEqual([]);
    expect(component.loading).toBeFalse();
  });

  it('should handle perimes alerts error gracefully', () => {
    prescriptionServiceSpy.getAlertsRupture.and.returnValue(of(mockRuptureAlerts as any));
    prescriptionServiceSpy.getAlertsPerimes.and.returnValue(throwError(() => new Error('error')));

    fixture = TestBed.createComponent(AlertsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.perimesAlerts).toEqual([]);
    expect(component.loading).toBeFalse();
  });

  it('should handle both alerts error gracefully', () => {
    prescriptionServiceSpy.getAlertsRupture.and.returnValue(throwError(() => new Error('error')));
    prescriptionServiceSpy.getAlertsPerimes.and.returnValue(throwError(() => new Error('error')));

    fixture = TestBed.createComponent(AlertsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.ruptureAlerts).toEqual([]);
    expect(component.perimesAlerts).toEqual([]);
    expect(component.loading).toBeFalse();
  });

  it('should count rupture and perimes alerts', () => {
    setupAlerts();
    expect(component.ruptureCount).toBe(3);
    expect(component.perimesCount).toBe(3);
  });

  it('should switch tab and reset filters', () => {
    setupAlerts();
    component.searchQuery = 'test';
    component.sortBy = 'stock-asc';
    component.page = 2;

    component.switchTab('perimes');

    expect(component.activeTab).toBe('perimes');
    expect(component.sortBy).toBe('name');
    expect(component.searchQuery).toBe('');
    expect(component.page).toBe(0);
  });

  it('should return filtered and sorted rupture items', () => {
    setupAlerts();
    const items = component.currentItems;
    expect(items.length).toBe(3);
    // Default sort by name A-Z
    expect(items[0].medicamentName).toBe('AMOXICILLINE');
    expect(items[1].medicamentName).toBe('ASPIRINE');
    expect(items[2].medicamentName).toBe('DOLIPRANE');
  });

  it('should filter rupture items by search query', () => {
    setupAlerts();
    component.searchQuery = 'doli';
    expect(component.currentItems.length).toBe(1);
    expect(component.currentItems[0].medicamentName).toBe('DOLIPRANE');
  });

  it('should sort rupture items by stock ascending', () => {
    setupAlerts();
    component.sortBy = 'stock-asc';
    const items = component.currentItems;
    expect(items[0].stockTotal).toBe(0);
    expect(items[1].stockTotal).toBe(5);
    expect(items[2].stockTotal).toBe(12);
  });

  it('should sort rupture items by stock descending', () => {
    setupAlerts();
    component.sortBy = 'stock-desc';
    const items = component.currentItems;
    expect(items[0].stockTotal).toBe(12);
    expect(items[1].stockTotal).toBe(5);
    expect(items[2].stockTotal).toBe(0);
  });

  it('should return perimes items sorted by name A-Z', () => {
    setupAlerts();
    component.switchTab('perimes');
    const items = component.currentItems;
    expect(items.length).toBe(3);
    expect(items[0].medicamentName).toBe('AMOXICILLINE');
  });

  it('should sort perimes items by expiry date', () => {
    setupAlerts();
    component.switchTab('perimes');
    component.sortBy = 'expiry';
    const items = component.currentItems;
    // First item should be the one expiring soonest
    expect(items[0].medicamentName).toBe('DOLIPRANE');
  });

  it('should sort perimes items by quantity descending', () => {
    setupAlerts();
    component.switchTab('perimes');
    component.sortBy = 'qty';
    const items = component.currentItems;
    expect(items[0].quantiteEnStock).toBe(200);
    expect(items[2].quantiteEnStock).toBe(50);
  });

  it('should paginate items', () => {
    setupAlerts();
    // With 3 items at pageSize 10, should show all on 1 page
    expect(component.totalPages).toBe(1);
    expect(component.currentItems.length).toBe(3);

    // Add 15 more rupture alerts to force multi-page
    for (let i = 0; i < 15; i++) {
      component.ruptureAlerts.push({ medicamentId: 100 + i, medicamentName: 'MED-' + i, stockTotal: 0, status: 'Rupture' });
    }
    expect(component.totalPages).toBe(2);
    expect(component.currentItems.length).toBe(10);

    component.goTo(1);
    expect(component.page).toBe(1);
    expect(component.currentItems.length).toBe(8);
  });

  it('should not go below page 0', () => {
    setupAlerts();
    for (let i = 0; i < 15; i++) {
      component.ruptureAlerts.push({ medicamentId: 100 + i, medicamentName: 'MED-' + i, stockTotal: 0, status: 'Rupture' });
    }
    component.goTo(-1);
    expect(component.page).toBe(0);
  });

  it('should not go beyond last page', () => {
    setupAlerts();
    for (let i = 0; i < 15; i++) {
      component.ruptureAlerts.push({ medicamentId: 100 + i, medicamentName: 'MED-' + i, stockTotal: 0, status: 'Rupture' });
    }
    component.goTo(99);
    expect(component.page).toBe(component.totalPages - 1);
  });

  it('should reset page on search or sort change', () => {
    setupAlerts();
    component.page = 2;
    component.onSearchOrSortChange();
    expect(component.page).toBe(0);
  });

  it('should compute joursRestants correctly', () => {
    setupAlerts();
    const future = new Date(today.getTime() + 7 * 86400000).toISOString();
    const days = component.joursRestants(future);
    expect(days).toBe(7);

    const past = new Date(today.getTime() - 5 * 86400000).toISOString();
    const pastDays = component.joursRestants(past);
    expect(pastDays).toBeLessThan(0);
  });

  it('should return 0 joursRestants for null date', () => {
    setupAlerts();
    expect(component.joursRestants('')).toBe(0);
  });

  it('should provide correct sort options for rupture tab', () => {
    setupAlerts();
    const options = component.sortOptions;
    expect(options.length).toBe(3);
    expect(options[0].value).toBe('name');
    expect(options[1].value).toBe('stock-asc');
    expect(options[2].value).toBe('stock-desc');
  });

  it('should provide correct sort options for perimes tab', () => {
    setupAlerts();
    component.switchTab('perimes');
    const options = component.sortOptions;
    expect(options.length).toBe(3);
    expect(options[0].value).toBe('name');
    expect(options[1].value).toBe('expiry');
    expect(options[2].value).toBe('qty');
  });

  it('should use trackById with medicamentId', () => {
    setupAlerts();
    const item = { medicamentId: 99 };
    expect(component.trackById(0, item)).toBe(99);
  });

  it('should use trackById with id for perimes', () => {
    setupAlerts();
    const item = { id: 99 };
    expect(component.trackById(0, item)).toBe(99);
  });

  it('should show filtered empty state for rupture search with no results', () => {
    setupAlerts();
    component.searchQuery = 'zzzzz';
    expect(component.totalItems).toBe(0);
    expect(component.currentItems.length).toBe(0);
  });

  it('should show empty state for perimes tab with no results', () => {
    setupAlerts();
    component.switchTab('perimes');
    component.searchQuery = 'zzzzz';
    expect(component.totalItems).toBe(0);
  });
});
