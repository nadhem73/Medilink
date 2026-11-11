import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PrescriptionService } from '../../../core/services/prescription.service';
import { PharmacyStockComponent } from './pharmacy-stock.component';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('PharmacyStockComponent', () => {
  let component: PharmacyStockComponent;
  let fixture: ComponentFixture<PharmacyStockComponent>;
  let prescriptionServiceSpy: jasmine.SpyObj<PrescriptionService>;

  const mockMedicaments = {
    content: [
      { id: 1, name: 'DOLIPRANE', dosage: '500mg', forme: 'Comprimé', presentation: 'Boîte 8', price: 3.250, remboursement: 2.500, dci: 'Paracétamol', type: 'Analgésiques (antidouleurs)', prescriptionRequired: false, imageUrl: '', stockTotal: 120, nbLots: 3, voieAdministration: ['Orale'], emplacements: [] },
      { id: 2, name: 'DOLIPRANE', dosage: '1000mg', forme: 'Comprimé effervescent', presentation: 'Boîte 8', price: 4.800, remboursement: 3.500, dci: 'Paracétamol', type: 'Analgésiques (antidouleurs)', prescriptionRequired: false, imageUrl: '', stockTotal: 60, nbLots: 2, voieAdministration: ['Orale'], emplacements: [] },
      { id: 3, name: 'AMOXICILLINE', dosage: '250mg', forme: 'Gélule', presentation: 'Boîte 12', price: 5.500, remboursement: 4.000, dci: 'Amoxicilline', type: 'Antibiotiques', prescriptionRequired: true, imageUrl: '', stockTotal: 30, nbLots: 1, voieAdministration: ['Orale'], emplacements: [] },
      { id: 4, name: 'AMOXICILLINE', dosage: '500mg', forme: 'Gélule', presentation: 'Boîte 12', price: 8.000, remboursement: 6.000, dci: 'Amoxicilline', type: 'Antibiotiques', prescriptionRequired: true, imageUrl: '', stockTotal: 0, nbLots: 0, voieAdministration: ['Orale'], emplacements: [] },
    ],
    totalElements: 4
  };

  beforeEach(async () => {
    prescriptionServiceSpy = jasmine.createSpyObj('PrescriptionService', [
      'getAllMedicaments',
      'getStockLots',
      'createMedicament',
      'createStock'
    ]);

    prescriptionServiceSpy.getAllMedicaments.and.returnValue(of(mockMedicaments as any));
    prescriptionServiceSpy.getStockLots.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [PharmacyStockComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: PrescriptionService, useValue: prescriptionServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PharmacyStockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load medicaments on init', () => {
    expect(prescriptionServiceSpy.getAllMedicaments).toHaveBeenCalledWith(0, 5000);
    expect(component.allItems.length).toBe(4);
    expect(component.totalElements).toBe(4);
  });

  it('should build groups by name', () => {
    expect(component.groups.length).toBe(2);
    const doliprane = component.groups.find(g => g.name === 'DOLIPRANE');
    expect(doliprane).toBeTruthy();
    expect(doliprane!.variants.length).toBe(2);
    expect(doliprane!.stockTotal).toBe(180);
    expect(doliprane!.lotsTotal).toBe(5);
  });

  it('should build option lists from catalogue', () => {
    expect(component.doseOptions).toContain('1000mg');
    expect(component.doseOptions).toContain('500mg');
    expect(component.formeOptions).toContain('Comprimé');
    expect(component.formeOptions).toContain('Comprimé effervescent');
    expect(component.dciOptions).toContain('Paracétamol');
    expect(component.dciOptions).toContain('Amoxicilline');
  });

  it('should filter groups by search query', () => {
    component.searchQuery = 'amoxi';
    expect(component.filteredGroups.length).toBe(1);
    expect(component.filteredGroups[0].name).toBe('AMOXICILLINE');
  });

  it('should filter groups by DCI', () => {
    component.searchQuery = 'Paracétamol';
    expect(component.filteredGroups.length).toBe(1);
    expect(component.filteredGroups[0].name).toBe('DOLIPRANE');
  });

  it('should filter groups by type', () => {
    component.selectedType = 'Antibiotiques';
    expect(component.filteredGroups.length).toBe(1);
    expect(component.filteredGroups[0].name).toBe('AMOXICILLINE');
  });

  it('should filter groups by stock level `faible`', () => {
    component.selectedStockFilter = 'faible';
    expect(component.filteredGroups.length).toBe(1);
    expect(component.filteredGroups[0].name).toBe('AMOXICILLINE');
  });

  it('should filter groups by stock level `rupture`', () => {
    component.selectedStockFilter = 'rupture';
    expect(component.filteredGroups.length).toBe(0);
  });

  it('should return correct stock status', () => {
    expect(component.getStockStatus(0)).toBe('rupture');
    expect(component.getStockStatus(5)).toBe('critique');
    expect(component.getStockStatus(30)).toBe('faible');
    expect(component.getStockStatus(180)).toBe('suffisant');
  });

  it('should clear filters and reset page', () => {
    component.searchQuery = 'test';
    component.selectedType = 'Antibiotiques';
    component.selectedStockFilter = 'rupture';
    component.currentPage = 2;
    component.expandedName = 'DOLIPRANE';
    component.selectedGroup = component.groups[0];

    component.clearFilters();

    expect(component.searchQuery).toBe('');
    expect(component.selectedType).toBe('');
    expect(component.selectedStockFilter).toBe('');
    expect(component.currentPage).toBe(0);
    expect(component.expandedName).toBeNull();
    expect(component.selectedGroup).toBeNull();
  });

  it('should detect active filters', () => {
    expect(component.hasActiveFilters).toBeFalse();
    component.searchQuery = 'test';
    expect(component.hasActiveFilters).toBeTrue();
  });

  it('should handle pagination', () => {
    const totalPages = component.totalPages;
    expect(totalPages).toBeGreaterThanOrEqual(1);
    expect(component.currentPage).toBe(0);

    component.nextPage();
    if (totalPages > 1) {
      expect(component.currentPage).toBe(1);
    }

    component.prevPage();
    expect(component.currentPage).toBe(0);
  });

  it('should not go below page 0', () => {
    component.currentPage = 0;
    component.prevPage();
    expect(component.currentPage).toBe(0);
  });

  it('should open modal and set selected group', () => {
    const group = component.groups[0];
    component.openModal(group);
    expect(component.selectedGroup).toBe(group);
  });

  it('should close modal and reset overflow', () => {
    component.selectedGroup = component.groups[0];
    component.closeModal();
    expect(component.selectedGroup).toBeNull();
  });

  it('should open add modal with correct defaults', () => {
    component.openAddModal();
    expect(component.showAddForm).toBeTrue();
    expect(component.addTab).toBe('existing');
    expect(component.addForm).toEqual({});
    expect(component.lotSearch).toBe('');
    expect(component.selectedMed).toBeNull();
  });

  it('should close add modal', () => {
    component.showAddForm = true;
    component.addError = 'error';
    component.lotError = 'lot error';
    component.closeAddModal();
    expect(component.showAddForm).toBeFalse();
    expect(component.addError).toBe('');
    expect(component.lotError).toBe('');
  });

  it('should search lots by name', () => {
    component.lotSearch = 'dol';
    component.onLotSearch();
    expect(component.lotResults.length).toBe(2);
    expect(component.lotResults.every(m => m.name === 'DOLIPRANE')).toBeTrue();
  });

  it('should search lots by DCI', () => {
    component.lotSearch = 'amoxi';
    component.onLotSearch();
    expect(component.lotResults.length).toBe(2);
  });

  it('should return empty lot results when no match', () => {
    component.lotSearch = 'zzzzz';
    component.onLotSearch();
    expect(component.lotResults.length).toBe(0);
  });

  it('should clear lot results on empty query', () => {
    component.lotSearch = '';
    component.onLotSearch();
    expect(component.lotResults.length).toBe(0);
  });

  it('should select med for lot and load emplacements', () => {
    const med = mockMedicaments.content[0] as any;
    component.selectMedForLot(med);
    expect(component.selectedMed).toBe(med);
    expect(component.lotResults).toEqual([]);
    expect(prescriptionServiceSpy.getStockLots).toHaveBeenCalledWith(1);
  });

  it('should clear selected med', () => {
    component.selectedMed = mockMedicaments.content[0] as any;
    component.lotForm = { quantite: 10 };
    component.clearSelectedMed();
    expect(component.selectedMed).toBeNull();
    expect(component.lotForm).toEqual({});
  });

  it('should generate lot number on date change', () => {
    component.lotNumeroTouched = false;
    component.lotForm.dateFabrication = '2026-07-01';
    component.onLotDateChange();
    expect(component.lotForm.numeroLot).toBeTruthy();
    expect(component.lotForm.numeroLot).toMatch(/^\d{2}\d{2}-[A-Z]\d{3}$/);
  });

  it('should not overwrite manually entered lot number', () => {
    component.lotNumeroTouched = true;
    component.lotForm.numeroLot = 'MANUAL-001';
    component.lotForm.dateFabrication = '2026-07-01';
    component.onLotDateChange();
    expect(component.lotForm.numeroLot).toBe('MANUAL-001');
  });

  it('should submit lot and reload', () => {
    const med = mockMedicaments.content[0] as any;
    component.selectedMed = med;
    component.lotForm = { quantite: 50, numeroLot: 'LOT-2607-2907', emplacement: 'A12' };
    prescriptionServiceSpy.createStock.and.returnValue(of({}));
    prescriptionServiceSpy.getAllMedicaments.and.returnValue(of(mockMedicaments as any));

    component.submitLot();

    expect(prescriptionServiceSpy.createStock).toHaveBeenCalledWith({
      medicamentId: 1,
      numeroLot: 'LOT-2607-2907',
      quantiteEnStock: 50,
      dateFabrication: null,
      dateExpiration: null,
      emplacement: 'A12'
    });
  });

  it('should set error on submit lot without selected med', () => {
    component.selectedMed = null;
    component.submitLot();
    expect(component.lotError).toBe('Sélectionnez d abord un médicament.');
  });

  it('should set error on submit lot with invalid quantity', () => {
    component.selectedMed = mockMedicaments.content[0] as any;
    component.lotForm = { quantite: 0 };
    component.submitLot();
    expect(component.lotError).toBe('La quantité doit être supérieure à 0.');
  });

  it('should set error when exp < fab date', () => {
    component.selectedMed = mockMedicaments.content[0] as any;
    component.lotForm = { quantite: 10, dateFabrication: '2026-07-01', dateExpiration: '2026-06-01' };
    component.submitLot();
    expect(component.lotError).toBe('La date d expiration doit être postérieure à la fabrication.');
  });

  it('should handle createStock error on submit lot', () => {
    const med = mockMedicaments.content[0] as any;
    component.selectedMed = med;
    component.lotForm = { quantite: 50, numeroLot: 'LOT-2607-2907' };
    prescriptionServiceSpy.createStock.and.returnValue(throwError(() => ({
      error: { error: 'Erreur serveur' }
    })));

    component.submitLot();

    expect(component.lotError).toBe('Erreur serveur');
  });

  it('should submit new medicament with stock and reload', () => {
    component.addForm = { name: 'NOVAMOX', dosage: '500mg', forme: 'Comprimé', quantite: 100, price: 10.000 };
    prescriptionServiceSpy.createMedicament.and.returnValue(of({ id: 99 }));
    prescriptionServiceSpy.createStock.and.returnValue(of({}));
    prescriptionServiceSpy.getAllMedicaments.and.returnValue(of(mockMedicaments as any));

    component.submitAdd();

    expect(prescriptionServiceSpy.createMedicament).toHaveBeenCalled();
    expect(prescriptionServiceSpy.createStock).toHaveBeenCalled();
  });

  it('should set error on createMedicament failure', () => {
    component.addForm = { name: 'TEST' };
    prescriptionServiceSpy.createMedicament.and.returnValue(throwError(() => ({
      error: { error: 'Erreur création' }
    })));

    component.submitAdd();

    expect(component.addError).toBe('Erreur création');
  });

  it('should set error if medicament name is missing', () => {
    component.addForm = { name: '' };
    component.submitAdd();
    expect(component.addError).toBe('Le nom commercial est obligatoire.');
  });

  it('should return correct stock status', () => {
    expect(component.getStockStatus(0)).toBe('rupture');
    expect(component.getStockStatus(5)).toBe('critique');
    expect(component.getStockStatus(30)).toBe('faible');
    expect(component.getStockStatus(180)).toBe('suffisant');
  });

  it('should return correct stock label', () => {
    expect(component.getStockBarColor('rupture')).toBe('#C93545');
    expect(component.getStockBarColor('critique')).toBe('#D48A00');
    expect(component.getStockBarColor('suffisant')).toBe('#1B8A5E');
  });

  it('should format price correctly', () => {
    expect(component.formatPrice(3.250)).toBe('3.250 DT');
    expect(component.formatPrice(null)).toBe('—');
  });

  it('should compute price range', () => {
    const amoxi = component.groups.find(g => g.name === 'AMOXICILLINE')!;
    const doliprane = component.groups.find(g => g.name === 'DOLIPRANE')!;
    expect(component.priceRange(amoxi)).toBe('5.500 – 8.000 DT');
    expect(component.priceRange(doliprane)).toBe('3.250 – 4.800 DT');
  });

  it('should call onFilterChange and reset state', () => {
    component.currentPage = 3;
    component.expandedName = 'DOLIPRANE';
    component.selectedGroup = component.groups[0];
    component.onFilterChange();
    expect(component.currentPage).toBe(0);
    expect(component.expandedName).toBeNull();
    expect(component.selectedGroup).toBeNull();
  });

  it('should load emplacements for a variant', () => {
    const variant = mockMedicaments.content[0] as any;
    variant.emplacements = [];
    prescriptionServiceSpy.getStockLots.and.returnValue(of([
      { emplacement: 'A12' }, { emplacement: 'A12' }, { emplacement: 'B05' }
    ]));

    component.loadEmplacements(variant);

    expect(prescriptionServiceSpy.getStockLots).toHaveBeenCalledWith(1);
    expect(variant.emplacements).toEqual(['A12', 'B05']);
  });

  it('should not reload emplacements if already loaded', () => {
    const variant = { id: 1, emplacements: ['A12'] };
    component.loadEmplacements(variant as any);
    expect(prescriptionServiceSpy.getStockLots).not.toHaveBeenCalled();
  });

  it('should handle overlay click to close modal', () => {
    component.selectedGroup = component.groups[0];
    const event = { target: { classList: { contains: (cls: string) => cls === 'sc-modal-overlay' } } } as any;
    component.onOverlayClick(event);
    expect(component.selectedGroup).toBeNull();
  });

  it('should not close modal on inner click', () => {
    component.selectedGroup = component.groups[0];
    const event = { target: { classList: { contains: (cls: string) => false } } } as any;
    component.onOverlayClick(event);
    expect(component.selectedGroup).toBeTruthy();
  });

  it('should generate gradient from name', () => {
    const grad = component.headerGradient('DOLIPRANE');
    expect(grad).toContain('linear-gradient');
    expect(grad).toContain('hsl(');
  });

  it('should assign class color for known types', () => {
    expect(component.classColor('Analgésiques (antidouleurs)')).toBe('#E85D4E');
    expect(component.classColor('Antibiotiques')).toBe('#1B8A5E');
    expect(component.classColor('UNKNOWN_TYPE')).toBe('#6D28D9');
  });

  it('should compute bar width based on max stock', () => {
    const doliprane = component.groups.find(g => g.name === 'DOLIPRANE')!;
    const amoxi = component.groups.find(g => g.name === 'AMOXICILLINE')!;
    // DOLIPRANE has 180 (max), AMOXICILLINE has 30 → barWidth near (30/180)*100 ≈ 17
    const width = component.barWidth(amoxi);
    expect(width).toBeGreaterThanOrEqual(2);
    expect(width).toBeLessThanOrEqual(100);
  });

  it('should toggle page and reset expanded/selected state on nextPage', () => {
    // Force multiple pages by reducing pageSize below group count
    component.pageSize = 1;
    component.currentPage = 0;
    component.expandedName = 'DOLIPRANE';
    component.selectedGroup = component.groups[0];
    component.nextPage();
    expect(component.expandedName).toBeNull();
    expect(component.selectedGroup).toBeNull();
    expect(component.currentPage).toBe(1);
  });

  it('should not go beyond last page', () => {
    component.currentPage = component.totalPages - 1;
    component.nextPage();
    expect(component.currentPage).toBe(component.totalPages - 1);
  });
});
