import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { Subject, debounceTime, distinctUntilChanged, switchMap } from 'rxjs';
import { PrescriptionService, MedicationDto } from '../../../../core/services/prescription.service';
import { AuthService } from '../../../../core/services/auth.service';
import { DoctorService, Doctor } from '../../../../core/services/doctor.service';

export interface PrescriptionItemEntry {
  medicamentId: number;
  medicamentName: string;
  dosage: string;
  forme: string;
  posologie: string;
  dureeTraitement: number;
  voieAdministration: string;
  instructions: string;
}

@Component({
  selector: 'app-prescription-modal',
  templateUrl: './prescription-modal.component.html',
  styleUrls: ['./prescription-modal.component.scss']
})
export class PrescriptionModalComponent implements OnInit {
  @Input() consultationId!: number;
  @Input() patientId!: number;
  @Input() patientName = '';
  @Input() patientCnam = '';
  @Input() analysisTypes: string[] = [];
  @Input() selectedAnalyses = '';
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();
  @Output() analysesChanged = new EventEmitter<string>();

  activeTab: 'medicaments' | 'analyses' = 'medicaments';

  searchTerm = '';
  searchResults: MedicationDto[] = [];
  selectedMedication: MedicationDto | null = null;
  expandedName: string | null = null;
  items: PrescriptionItemEntry[] = [];
  saving = false;
  error = '';
  searchSubject = new Subject<string>();

  voieOptions = ['Orale', 'Injectable', 'Cutanée', 'Rectale', 'Oculaire', 'Nasale', 'Sublinguale', 'Inhalée'];

  doctorFirstName = '';
  doctorLastName = '';
  doctorSpecialty = '';
  doctorLicenseNumber = '';
  doctorPhone = '';
  doctorEmail = '';
  doctorHospital = '';
  signatureDisplayed = false;
  showStamp = false;
  today = new Date();

  private posologieByVoie: Record<string, string[]> = {
    'Orale': [
      '1 comprimé, 1 fois par jour', '1 comprimé, 2 fois par jour', '1 comprimé, 3 fois par jour',
      '2 comprimés, 1 fois par jour', '2 comprimés, 2 fois par jour', '2 comprimés, 3 fois par jour',
      '1 gélule, 1 fois par jour', '1 gélule, 2 fois par jour', '1 gélule, 3 fois par jour',
      '1 cuillère à soupe, 1 fois par jour', '1 cuillère à soupe, 2 fois par jour', '1 cuillère à soupe, 3 fois par jour',
      '1 sachet, 1 fois par jour', '1 sachet, 2 fois par jour', '1 sachet, 3 fois par jour'
    ],
    'Injectable': [
      '1 ampoule, 1 fois par jour', '1 ampoule, 2 fois par jour', '1 ampoule, 1 fois par semaine',
      '1 injection, 1 fois par jour', 'Perfusion selon prescription'
    ],
    'Cutanée': [
      '1 application locale, 1 fois par jour', '1 application locale, 2 fois par jour', '1 application locale, 3 fois par jour',
      '1 couche mince, 1 fois par jour', '1 couche mince, 2 fois par jour'
    ],
    'Rectale': [
      '1 suppositoire, 1 fois par jour', '1 suppositoire, 2 fois par jour', '1 suppositoire, 1 fois si besoin',
      '1 lavement selon prescription'
    ],
    'Oculaire': [
      '1 goutte, 1 fois par jour', '1 goutte, 2 fois par jour', '1 goutte, 3 fois par jour',
      '1 goutte, 4 fois par jour', '1 goutte toutes les 4 heures'
    ],
    'Nasale': [
      '1 pulvérisation, 1 fois par jour', '1 pulvérisation, 2 fois par jour', '1 pulvérisation, 3 fois par jour',
      '2 pulvérisations, 2 fois par jour', '1 goutte, 2 fois par jour'
    ],
    'Sublinguale': [
      '1 comprimé sublingual, 1 fois par jour', '1 comprimé sublingual, 2 fois par jour',
      '1 comprimé sublingual, 3 fois par jour', '1 comprimé sublingual, en cas de crise'
    ],
    'Inhalée': [
      '1 inhalation, 1 fois par jour', '1 inhalation, 2 fois par jour', '1 inhalation, 3 fois par jour',
      '2 inhalations, 2 fois par jour', '1 bouffée, 2 fois par jour', '2 bouffées, 2 fois par jour'
    ]
  };

  private commonPosologie = ['Selon prescription médicale'];

  get posologieOptions(): string[] {
    return [...(this.posologieByVoie[this.currentItem.voieAdministration] || []), ...this.commonPosologie];
  }

  instructionsOptions = [
    'Avant les repas', 'Après les repas', 'Au cours des repas', 'À jeun',
    'Le soir avant le coucher', 'Le matin à jeun', '30 minutes avant le repas',
    '1 heure après le repas', 'Au besoin', 'En cas de douleur',
    'Ne pas dépasser la dose prescrite', 'Avec un verre d\'eau',
    'Éviter l\'alcool', 'Éviter l\'exposition au soleil', 'Rester au repos'
  ];

  posologieCustom = false;
  instructionsCustom = false;

  currentItem: PrescriptionItemEntry = {
    medicamentId: 0, medicamentName: '', dosage: '', forme: '',
    posologie: '', dureeTraitement: 7, voieAdministration: 'Orale', instructions: ''
  };

  get hasAnalyses(): boolean {
    return this.selectedAnalyses.length > 0;
  }

  get analysesList(): string[] {
    return this.selectedAnalyses
      ? this.selectedAnalyses.split(',').map(s => s.trim()).filter(s => s)
      : [];
  }

  hasAnalysis(type: string): boolean {
    return this.analysesList.includes(type);
  }

  toggleAnalysis(type: string, checked: boolean): void {
    let list = this.analysesList;
    if (checked) {
      if (!list.includes(type)) list = [...list, type];
    } else {
      list = list.filter(s => s !== type);
    }
    this.selectedAnalyses = list.join(', ');
    this.analysesChanged.emit(this.selectedAnalyses);
  }

  setActiveTab(tab: 'medicaments' | 'analyses'): void {
    this.activeTab = tab;
  }

  get hasInstructions(): boolean {
    return this.items.some(i => i.instructions?.trim().length > 0);
  }

  get itemsWithInstructions(): PrescriptionItemEntry[] {
    return this.items.filter(i => i.instructions?.trim().length > 0);
  }

  get uniqueNames(): string[] {
    return [...new Set(this.searchResults.map(m => m.name))];
  }

  getVariantsForName(name: string): MedicationDto[] {
    const seen = new Set<string>();
    return this.searchResults.filter(m => {
      if (m.name !== name) return false;
      const key = `${m.dosage}|${m.forme}`;
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
  }

  toggleExpand(name: string): void {
    this.expandedName = this.expandedName === name ? null : name;
  }

  get filteredVoieOptions(): string[] {
    if (this.selectedMedication?.voieAdministration?.length) {
      return this.selectedMedication.voieAdministration;
    }
    return this.voieOptions;
  }

  constructor(
    private prescriptionService: PrescriptionService,
    private authService: AuthService,
    private doctorService: DoctorService
  ) {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        if (term.trim().length < 2) return [];
        return this.prescriptionService.searchMedicaments(term);
      })
    ).subscribe({
      next: (res: any) => { this.searchResults = res?.content || []; },
      error: () => { this.searchResults = []; }
    });
  }

  ngOnInit(): void {
    this.loadDoctorInfo();
  }

  private loadDoctorInfo(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    this.doctorFirstName = currentUser.firstName || '';
    this.doctorLastName = currentUser.lastName || '';
    this.doctorPhone = currentUser.phone || '';
    this.doctorEmail = currentUser.email || '';

    this.doctorService.getAllDoctors().subscribe({
      next: (doctors) => {
        const doc = doctors.find(d => d.id === currentUser.id);
        if (doc) {
          this.doctorSpecialty = doc.specialty || '';
          this.doctorLicenseNumber = doc.licenseNumber || '';
          this.doctorHospital = doc.hospital || '';
        }
      },
      error: () => {}
    });
  }

  get signatureChars(): { char: string; delay: string }[] {
    const name = `${this.doctorFirstName} ${this.doctorLastName}`.trim() || 'Dr';
    return name.split('').map((char, i) => ({
      char,
      delay: `${i * 0.08}s`
    }));
  }

  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.selectedMedication = null;
    this.expandedName = null;
    this.searchSubject.next(term);
  }

  selectMedication(m: MedicationDto): void {
    this.selectedMedication = m;
    const defaultVoie = m.voieAdministration?.length ? m.voieAdministration[0] : 'Orale';
    this.currentItem = {
      medicamentId: m.id,
      medicamentName: m.name,
      dosage: m.dosage || '',
      forme: m.forme || '',
      posologie: '',
      dureeTraitement: 7,
      voieAdministration: defaultVoie,
      instructions: ''
    };
    this.posologieCustom = false;
    this.instructionsCustom = false;
    this.searchTerm = '';
    this.searchResults = [];
  }

  onPosologieChange(value: string): void {
    if (value === '__custom__') {
      this.posologieCustom = true;
      this.currentItem.posologie = '';
    } else {
      this.posologieCustom = false;
      this.currentItem.posologie = value;
    }
  }

  onVoieChange(): void {
    this.currentItem.posologie = '';
    this.posologieCustom = false;
  }

  onInstructionsChange(value: string): void {
    if (value === '__custom__') {
      this.instructionsCustom = true;
      this.currentItem.instructions = '';
    } else {
      this.instructionsCustom = false;
      this.currentItem.instructions = value;
    }
  }

  addItem(): void {
    if (!this.currentItem.medicamentId || !this.currentItem.posologie.trim()) {
      this.error = 'Veuillez remplir la posologie';
      return;
    }
    this.items.push({ ...this.currentItem });
    this.selectedMedication = null;
    this.posologieCustom = false;
    this.instructionsCustom = false;
    this.currentItem = {
      medicamentId: 0, medicamentName: '', dosage: '', forme: '',
      posologie: '', dureeTraitement: 7, voieAdministration: 'Orale', instructions: ''
    };
    this.error = '';
  }

  removeItem(index: number): void {
    this.items.splice(index, 1);
  }

  getStockClass(stock: number): string {
    if (stock <= 0) return 'stock-none';
    if (stock < 20) return 'stock-low';
    return 'stock-ok';
  }

  getStockLabel(stock: number): string {
    if (stock <= 0) return 'Rupture';
    if (stock < 20) return 'Faible';
    return 'Disponible';
  }

  savePrescription(): void {
    if (this.items.length === 0) {
      this.error = 'Ajoutez au moins un médicament à l\'ordonnance';
      return;
    }
    this.saving = true;
    this.error = '';
    this.showStamp = true;

    setTimeout(() => {
      const request = {
        consultationId: this.consultationId,
        patientId: this.patientId,
        notes: '',
        items: this.items.map(i => ({
          medicamentId: i.medicamentId,
          medicamentName: i.medicamentName,
          dosage: i.dosage,
          forme: i.forme,
          posologie: i.posologie,
          dureeTraitement: i.dureeTraitement,
          voieAdministration: i.voieAdministration,
          instructions: i.instructions
        }))
      };
      this.prescriptionService.createPrescription(request).subscribe({
        next: () => {
          this.saving = false;
          this.saved.emit();
        },
        error: (err) => {
          this.saving = false;
          this.showStamp = false;
          this.error = err.error?.error || 'Erreur lors de la création de l\'ordonnance';
        }
      });
    }, 1600);
  }

  onClose(): void {
    this.close.emit();
  }

  trackByIndex(index: number): number {
    return index;
  }
}
