import { Component, OnInit } from '@angular/core';
import { PrescriptionService } from '../../../core/services/prescription.service';

interface StockItem {
  id: number;
  name: string;
  dosage: string;
  forme: string;
  presentation: string;
  price: number;
  remboursement: number;
  dci: string;
  type: string;
  prescriptionRequired: boolean;
  imageUrl: string;
  stockTotal: number;
  nbLots: number;
  voieAdministration: string[];
  emplacements: string[];
}

/** Médicament regroupé par nom commercial populaire (ex: DOLIPRANE), avec ses présentations. */
interface GroupedMed {
  name: string;
  dci: string;
  type: string;
  imageUrl: string;
  prescriptionRequired: boolean;
  variants: StockItem[];
  stockTotal: number;
  lotsTotal: number;
  priceMin: number | null;
  priceMax: number | null;
  formes: string[];
}

@Component({
  selector: 'app-pharmacy-stock',
  templateUrl: './pharmacy-stock.component.html',
  styleUrls: ['./pharmacy-stock.component.scss']
})
export class PharmacyStockComponent implements OnInit {
  allItems: StockItem[] = [];
  groups: GroupedMed[] = [];
  private maxGroupStock = 1;

  loading = false;
  searchQuery = '';
  currentPage = 0;
  totalElements = 0;        // nombre total de présentations (lignes)
  pageSize = 24;

  selectedType = '';
  selectedStockFilter = '';
  expandedName: string | null = null;

  selectedGroup: GroupedMed | null = null;

  showAddForm = false;
  addTab: 'existing' | 'new' = 'existing';

  // Onglet « Nouveau médicament »
  addSaving = false;
  addError = '';
  addForm: any = {};
  addNumeroTouched = false;
  addImgError = false;

  // Valeurs distinctes du catalogue (listes déroulantes du formulaire d'ajout)
  doseOptions: string[] = [];
  formeOptions: string[] = [];
  presentationOptions: string[] = [];
  dciOptions: string[] = [];

  // Onglet « Médicament existant » (ajout d'un lot)
  lotSearch = '';
  lotResults: StockItem[] = [];
  selectedMed: StockItem | null = null;
  lotForm: any = {};
  lotSaving = false;
  lotError = '';
  lotNumeroTouched = false;

  loadingEmplacements = new Set<number>();

  groupImgErrors = new Set<string>();

  // Classes thérapeutiques réelles du référentiel (valeurs exactes en base, triées A→Z).
  readonly types = ['',
    'Analgésiques (antidouleurs)', 'Anesthésiques', 'Antiacides', 'Antibiotiques', 'Anticancéreux',
    'Anticoagulants', 'Anticonvulsivants', 'Antidiabétiques', 'Antidiarrhéiques', 'Antidépresseurs',
    'Antifongiques', 'Antihistaminiques', 'Antihypertenseurs', 'Anti-inflammatoires',
    'Antinauséeux (antiémétiques)', 'Antiparasitaires', 'Antiplaquettaires', 'Antipsychotiques',
    'Antispasmodiques', 'Antiviraux', 'Anxiolytiques (calmants)', 'Autres', 'Bronchodilatateurs',
    'Corticoïdes', 'Décongestionnants', 'Diurétiques', 'Expectorants', 'Hormones', 'Immunosuppresseurs',
    'Laxatifs', 'Sédatifs / Hypnotiques', 'Vitamines et compléments alimentaires'];

  readonly stockFilters = ['', 'rupture', 'critique', 'faible', 'suffisant'];

  // Couleur d'accent par classe (alignée sur les valeurs réelles en base).
  private readonly CLASS_COLORS: Record<string, string> = {
    'Analgésiques (antidouleurs)': '#E85D4E',
    'Anesthésiques': '#64748B',
    'Antiacides': '#0D9488',
    'Antibiotiques': '#1B8A5E',
    'Anticancéreux': '#BE123C',
    'Anticoagulants': '#BE123C',
    'Anticonvulsivants': '#6D28D9',
    'Antidiabétiques': '#0D9488',
    'Antidiarrhéiques': '#B45309',
    'Antidépresseurs': '#7C3AED',
    'Antifongiques': '#1B8A5E',
    'Antihistaminiques': '#8B5CF6',
    'Antihypertenseurs': '#256BB0',
    'Anti-inflammatoires': '#D97706',
    'Antinauséeux (antiémétiques)': '#0891B2',
    'Antiparasitaires': '#65A30D',
    'Antiplaquettaires': '#DB2777',
    'Antipsychotiques': '#7C3AED',
    'Antispasmodiques': '#D97706',
    'Antiviraux': '#059669',
    'Anxiolytiques (calmants)': '#0891B2',
    'Autres': '#6F7D8C',
    'Bronchodilatateurs': '#0D9488',
    'Corticoïdes': '#C026D3',
    'Décongestionnants': '#0891B2',
    'Diurétiques': '#256BB0',
    'Expectorants': '#0EA5E9',
    'Hormones': '#DB2777',
    'Immunosuppresseurs': '#9333EA',
    'Laxatifs': '#B45309',
    'Sédatifs / Hypnotiques': '#4F46E5',
    'Vitamines et compléments alimentaires': '#D97706',
  };

  private readonly DEFAULT_COLOR = '#6D28D9';

  /** Groupes filtrés (recherche + classe + niveau de stock) sur TOUT le catalogue. */
  get filteredGroups(): GroupedMed[] {
    let list = this.groups;
    if (this.selectedType) list = list.filter(g => g.type === this.selectedType);
    if (this.selectedStockFilter) list = list.filter(g => this.getStockStatus(g.stockTotal) === this.selectedStockFilter);
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.trim().toLowerCase();
      list = list.filter(g =>
        g.name.toLowerCase().includes(q) ||
        g.dci?.toLowerCase().includes(q) ||
        g.type?.toLowerCase().includes(q)
      );
    }
    return list;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredGroups.length / this.pageSize));
  }

  get pagedGroups(): GroupedMed[] {
    const start = this.currentPage * this.pageSize;
    return this.filteredGroups.slice(start, start + this.pageSize);
  }

  constructor(
    private prescriptionService: PrescriptionService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  /** Charge l'intégralité du référentiel en une requête puis regroupe par nom. */
  loadAll(): void {
    this.loading = true;
    this.prescriptionService.getAllMedicaments(0, 5000).subscribe({
      next: (res) => {
        this.allItems = (res.content || []).map((item: any) => ({ ...item, emplacements: [] }));
        this.totalElements = res.totalElements;
        this.buildOptionLists(this.allItems);
        this.groups = this.buildGroups(this.allItems);
        this.maxGroupStock = Math.max(...this.groups.map(g => g.stockTotal), 1);
        this.currentPage = 0;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  /** Construit les listes de valeurs distinctes du catalogue pour les listes déroulantes. */
  private buildOptionLists(items: StockItem[]): void {
    const uniq = (vals: (string | undefined)[]) =>
      [...new Set(vals.map(v => (v || '').trim()).filter(Boolean))]
        .sort((a, b) => a.localeCompare(b, 'fr', { numeric: true }));
    this.doseOptions = uniq(items.map(i => i.dosage));
    this.formeOptions = uniq(items.map(i => i.forme));
    this.presentationOptions = uniq(items.map(i => i.presentation));
    this.dciOptions = uniq(items.map(i => i.dci));
  }

  /** Regroupe les présentations par nom commercial (DOLIPRANE, DAFALGAN, …). */
  private buildGroups(items: StockItem[]): GroupedMed[] {
    const map = new Map<string, StockItem[]>();
    for (const it of items) {
      const arr = map.get(it.name);
      if (arr) arr.push(it); else map.set(it.name, [it]);
    }
    const groups: GroupedMed[] = [];
    map.forEach((variants, name) => {
      const prices = variants.map(v => v.price).filter(p => p !== null && p !== undefined) as number[];
      const withImg = variants.find(v => v.imageUrl);
      const formes = [...new Set(variants.map(v => v.forme).filter(Boolean))];
      groups.push({
        name,
        dci: variants[0].dci,
        type: variants[0].type,
        imageUrl: withImg ? withImg.imageUrl : '',
        prescriptionRequired: variants.some(v => v.prescriptionRequired),
        variants: variants.sort((a, b) => (a.dosage || '').localeCompare(b.dosage || '', 'fr', { numeric: true })),
        stockTotal: variants.reduce((s, v) => s + (v.stockTotal || 0), 0),
        lotsTotal: variants.reduce((s, v) => s + (v.nbLots || 0), 0),
        priceMin: prices.length ? Math.min(...prices) : null,
        priceMax: prices.length ? Math.max(...prices) : null,
        formes,
      });
    });
    return groups.sort((a, b) => a.name.localeCompare(b.name, 'fr'));
  }

  openModal(group: GroupedMed): void {
    this.selectedGroup = group;
    document.body.style.overflow = 'hidden';
    group.variants.forEach(v => this.loadEmplacements(v));
  }

  closeModal(): void {
    this.selectedGroup = null;
    document.body.style.overflow = '';
  }

  openAddModal(): void {
    this.addTab = 'existing';
    this.addForm = {};
    this.addError = '';
    this.addNumeroTouched = false;
    this.addImgError = false;
    this.lotSearch = '';
    this.lotResults = [];
    this.selectedMed = null;
    this.lotForm = {};
    this.lotError = '';
    this.lotNumeroTouched = false;
    this.showAddForm = true;
    document.body.style.overflow = 'hidden';
  }

  closeAddModal(): void {
    this.showAddForm = false;
    this.addError = '';
    this.lotError = '';
    document.body.style.overflow = '';
  }

  // ── Onglet « Médicament existant » ──────────────────────────────
  onLotSearch(): void {
    const q = this.lotSearch.trim().toLowerCase();
    if (!q) { this.lotResults = []; return; }
    this.lotResults = this.allItems.filter(m =>
      m.name.toLowerCase().includes(q) ||
      (m.dci && m.dci.toLowerCase().includes(q)) ||
      (m.dosage && m.dosage.toLowerCase().includes(q))
    ).slice(0, 8);
  }

  selectMedForLot(m: StockItem): void {
    this.selectedMed = m;
    this.lotResults = [];
    this.lotSearch = '';
    this.lotError = '';
    this.lotForm = {};
    this.lotNumeroTouched = false;

    // Emplacement auto : reprend l'emplacement habituel des lots existants du médicament.
    this.prescriptionService.getStockLots(m.id).subscribe({
      next: (lots) => {
        const emp = this.mostFrequentEmplacement(lots);
        if (emp && !this.lotForm.emplacement) this.lotForm.emplacement = emp;
      },
      error: () => {}
    });
  }

  clearSelectedMed(): void {
    this.selectedMed = null;
    this.lotForm = {};
    this.lotNumeroTouched = false;
  }

  /** Régénère le numéro de lot depuis la date de fabrication, sauf s'il a été saisi manuellement. */
  onLotDateChange(): void {
    if (this.lotNumeroTouched) return;
    if (this.lotForm.dateFabrication) {
      this.lotForm.numeroLot = this.generateLot(this.lotForm.dateFabrication);
    }
  }

  /** Emplacement le plus fréquent parmi les lots existants (rangement habituel). */
  private mostFrequentEmplacement(lots: any[]): string | null {
    const counts = new Map<string, number>();
    for (const l of lots || []) {
      const e = (l.emplacement || '').trim();
      if (e) counts.set(e, (counts.get(e) || 0) + 1);
    }
    let best: string | null = null;
    let max = 0;
    counts.forEach((c, e) => { if (c > max) { max = c; best = e; } });
    return best;
  }

  submitLot(): void {
    if (!this.selectedMed) { this.lotError = 'Sélectionnez d abord un médicament.'; return; }
    const qte = Number(this.lotForm.quantite);
    if (!qte || qte <= 0) { this.lotError = 'La quantité doit être supérieure à 0.'; return; }
    if (this.lotForm.dateFabrication && this.lotForm.dateExpiration &&
        this.lotForm.dateExpiration < this.lotForm.dateFabrication) {
      this.lotError = 'La date d expiration doit être postérieure à la fabrication.'; return;
    }
    this.lotError = '';
    this.lotSaving = true;

    this.prescriptionService.createStock({
      medicamentId: this.selectedMed.id,
      numeroLot: (this.lotForm.numeroLot && this.lotForm.numeroLot.trim())
        || this.generateLot(this.lotForm.dateFabrication),
      quantiteEnStock: qte,
      dateFabrication: this.lotForm.dateFabrication || null,
      dateExpiration: this.lotForm.dateExpiration || null,
      emplacement: this.lotForm.emplacement || null,
    }).subscribe({
      next: () => {
        this.lotSaving = false;
        this.closeAddModal();
        this.loadAll();
      },
      error: (err: any) => {
        this.lotSaving = false;
        this.lotError = err.error?.error || 'Erreur lors de l ajout du lot.';
      }
    });
  }

  /** Génère un numéro de lot AAMM-Xnnn à partir de la date de fabrication (ou aujourd hui). */
  private generateLot(dateFab?: string): string {
    const d = dateFab ? new Date(dateFab) : new Date();
    const yy = String(d.getFullYear() % 100).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const letters = 'ABCDEFGHJKLMNPRTVWXYZ';
    const l = letters[Math.floor(Math.random() * letters.length)];
    const n = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
    return `${yy}${mm}-${l}${n}`;
  }

  submitAdd(): void {
    if (!this.addForm.name) { this.addError = 'Le nom commercial est obligatoire.'; return; }
    this.addError = '';
    this.addSaving = true;

    this.prescriptionService.createMedicament({
      name: this.addForm.name,
      dosage: this.addForm.dosage || null,
      forme: this.addForm.forme || null,
      presentation: this.addForm.presentation || null,
      price: this.addForm.price ? Number(this.addForm.price) : null,
      remboursement: this.addForm.remboursement ? Number(this.addForm.remboursement) : null,
      dci: this.addForm.dci || null,
      type: this.addForm.type || null,
      prescriptionRequired: !!this.addForm.prescriptionRequired,
      imageUrl: (this.addForm.imageUrl && this.addForm.imageUrl.trim()) || null,
    }).subscribe({
      next: (medicament: any) => {
        const qte = this.addForm.quantite ? Number(this.addForm.quantite) : 0;
        if (qte > 0) {
          this.prescriptionService.createStock({
            medicamentId: medicament.id,
            numeroLot: (this.addForm.numeroLot && this.addForm.numeroLot.trim())
              || this.generateLot(this.addForm.dateFabrication),
            quantiteEnStock: qte,
            dateFabrication: this.addForm.dateFabrication || null,
            dateExpiration: this.addForm.dateExpiration || null,
            emplacement: this.addForm.emplacement || null,
          }).subscribe({
            next: () => {
              this.addSaving = false;
              this.showAddForm = false;
              document.body.style.overflow = '';
              this.loadAll();
            },
            error: (err: any) => {
              this.addSaving = false;
              this.addError = err.error?.error || 'Erreur lors de la creation du stock.';
            }
          });
        } else {
          this.addSaving = false;
          this.showAddForm = false;
          document.body.style.overflow = '';
          this.loadAll();
        }
      },
      error: (err: any) => {
        this.addSaving = false;
        this.addError = err.error?.error || 'Erreur lors de la creation du medicament.';
      }
    });
  }

  /** Onglet nouveau médicament : régénère le numéro de lot depuis la date de fabrication. */
  onAddDateChange(): void {
    if (this.addNumeroTouched) return;
    if (this.addForm.dateFabrication) {
      this.addForm.numeroLot = this.generateLot(this.addForm.dateFabrication);
    }
  }

  onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('sc-modal-overlay')) {
      this.closeModal();
    }
  }

  loadEmplacements(variant: StockItem): void {
    if (variant.emplacements.length > 0 || this.loadingEmplacements.has(variant.id)) return;
    this.loadingEmplacements.add(variant.id);
    this.prescriptionService.getStockLots(variant.id).subscribe({
      next: (lots) => {
        const locs = [...new Set(lots.map((l: any) => l.emplacement).filter(Boolean))];
        variant.emplacements = locs;
        this.loadingEmplacements.delete(variant.id);
      },
      error: () => this.loadingEmplacements.delete(variant.id)
    });
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.expandedName = null;
    this.selectedGroup = null;
  }

  getStockStatus(stock: number): string {
    if (stock === 0) return 'rupture';
    if (stock <= 10) return 'critique';
    if (stock <= 50) return 'faible';
    return 'suffisant';
  }

  getStockLabel(status: string): string {
    switch (status) {
      case 'rupture': return 'Rupture';
      case 'critique': return 'Critique';
      case 'faible': return 'Faible';
      case 'suffisant': return 'OK';
      default: return '';
    }
  }

  getStockBarColor(status: string): string {
    switch (status) {
      case 'rupture': return '#C93545';
      case 'critique': return '#D48A00';
      case 'faible': return '#D48A00';
      case 'suffisant': return '#1B8A5E';
      default: return '#D0D5DD';
    }
  }

  classColor(type: string): string {
    return this.CLASS_COLORS[type || ''] || this.DEFAULT_COLOR;
  }

  barWidth(g: GroupedMed): string {
    return Math.max(Math.round((g.stockTotal / this.maxGroupStock) * 100), 2).toString();
  }

  formatPrice(price: number | null | undefined): string {
    if (price === null || price === undefined) return '—';
    return `${price.toFixed(3)} DT`;
  }

  /** Fourchette de prix des variantes (ex: "1.955 – 27.100 DT"). */
  priceRange(g: GroupedMed): string {
    if (g.priceMin === null) return '—';
    if (g.priceMax === null || g.priceMax === g.priceMin) return this.formatPrice(g.priceMin);
    return `${g.priceMin.toFixed(3)} – ${g.priceMax.toFixed(3)} DT`;
  }

  headerGradient(name: string): string {
    const hue = this.hashCode(name) % 360;
    const sat = 55 + (this.hashCode(name + 's') % 20);
    const light = 40 + (this.hashCode(name + 'l') % 15);
    const hue2 = (hue + 30 + (this.hashCode(name + 'h') % 20)) % 360;
    return `linear-gradient(135deg, hsl(${hue}, ${sat}%, ${light}%), hsl(${hue2}, ${sat + 5}%, ${light - 8}%))`;
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedType = '';
    this.selectedStockFilter = '';
    this.currentPage = 0;
    this.expandedName = null;
    this.selectedGroup = null;
  }

  get hasActiveFilters(): boolean {
    return !!(this.searchQuery || this.selectedType || this.selectedStockFilter);
  }

  onGroupImgError(g: GroupedMed): void {
    this.groupImgErrors.add(g.name);
  }

  prevPage(): void {
    if (this.currentPage > 0) { this.currentPage--; this.expandedName = null; this.selectedGroup = null; }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) { this.currentPage++; this.expandedName = null; this.selectedGroup = null; }
  }

  private hashCode(str: string): number {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) - hash) + str.charCodeAt(i);
      hash = hash & hash;
    }
    return Math.abs(hash);
  }
}
