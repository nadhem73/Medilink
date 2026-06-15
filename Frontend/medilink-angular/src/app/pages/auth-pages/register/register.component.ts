import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

/** Une étape de la conversation avec l'assistant IA (présentation uniquement). */
interface WizardStep {
  key: string;
  prompt: string;        // ce que dit l'assistant
  controls: string[];    // contrôles qui doivent être valides pour avancer ([] = optionnel)
}

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  // =========================================================================
  // ⚙️  LOGIQUE MÉTIER — NE PAS MODIFIER
  //     (formulaire, validation, connexion backend, soumission)
  // =========================================================================
  registerForm!: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  bloodGroups = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

  /** True pendant la séquence de scan final — intensifie l'hologramme 3D. */
  get scanning(): boolean {
    return !!this.successMessage || this.loading;
  }

  genders = [
    { value: 'MALE', label: 'Homme' },
    { value: 'FEMALE', label: 'Femme' },
    { value: 'OTHER', label: 'Autre' }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      // Common fields
      firstName:    ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      lastName:     ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email:        ['', [Validators.required, Validators.email]],
      phone:        ['', [Validators.required, Validators.maxLength(20)]],
      birthDate:    ['', [Validators.required]],
      gender:       ['', [Validators.required]],
      address:      ['', [Validators.required, Validators.maxLength(255)]],
      cin:          ['', [Validators.required, Validators.maxLength(20)]],

      // Patient-specific fields
      bloodGroup:             [''],
      height:                 [null],
      weight:                 [null],
      allergies:              [''],
      chronicDiseases:        [''],
      currentTreatments:      [''],
      emergencyContactName:   ['', [Validators.required]],
      emergencyContactPhone:  ['', [Validators.required]],
      insuranceCompany:       [''],
      insuranceNumber:        [''],

      // Security
      password:        ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
      acceptTerms:     [false, [Validators.requiredTrue]],

      // Fixed role
      role: ['PATIENT']
    }, { validators: this.passwordMatchValidator });

    // Démarre la première réplique de l'assistant
    this.scheduleTyping();
  }

  // Shortcut getter for template
  get f() {
    return this.registerForm.controls;
  }

  // Password strength
  get passwordStrength(): number {
    const pwd = this.f['password'].value as string;
    if (!pwd) return 0;
    let score = 0;
    if (pwd.length >= 8)  score += 25;
    if (pwd.length >= 12) score += 15;
    if (/[A-Z]/.test(pwd)) score += 20;
    if (/[0-9]/.test(pwd)) score += 20;
    if (/[^A-Za-z0-9]/.test(pwd)) score += 20;
    return Math.min(score, 100);
  }

  get passwordStrengthClass(): string {
    const s = this.passwordStrength;
    if (s < 40) return 'weak';
    if (s < 70) return 'medium';
    return 'strong';
  }

  get passwordStrengthLabel(): string {
    const s = this.passwordStrength;
    if (s < 40) return 'Faible';
    if (s < 70) return 'Moyen';
    return 'Fort';
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.invalid || this.loading) {
      // Mark all touched to show errors
      Object.keys(this.registerForm.controls).forEach(key => {
        this.registerForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { confirmPassword, acceptTerms, ...registerData } = this.registerForm.value;
    const email = registerData.email;
    const password = registerData.password;

    // Étape 1 : Inscription
    this.authService.register(registerData).subscribe({
      next: () => {
        this.successMessage = 'Compte créé avec succès ! Connexion automatique en cours...';

        // Étape 2 : Connexion automatique après inscription
        this.authService.login({ email, password }).subscribe({
          next: () => {
            this.loading = false;
            this.successMessage = 'Bienvenue ! Redirection vers votre espace patient...';

            // Redirection vers le panel patient après 1.5 secondes
            setTimeout(() => {
              this.router.navigate(['/dashboard/patient']);
            }, 1500);
          },
          error: (loginError) => {
            // Si la connexion automatique échoue, rediriger vers la page de connexion
            this.loading = false;
            this.successMessage = 'Compte créé ! Veuillez vous connecter.';
            setTimeout(() => {
              this.router.navigate(['/auth/login'], {
                queryParams: { registered: 'true', email: email }
              });
            }, 2000);
          }
        });
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error?.error?.message || 'Une erreur est survenue. Veuillez réessayer.';
      }
    });
  }

  // =========================================================================
  // ✨  COUCHE PRÉSENTATION — expérience conversationnelle "Assistant IA"
  //     (aucune logique métier : pilote uniquement l'affichage / les animations)
  // =========================================================================
  currentStep = 0;
  assistantTyping = false;
  private typingTimer: any;

  // ---- Décor d'arrière-plan médical (présentation pure, calculé une fois) ----
  readonly bgBokeh = Array.from({ length: 12 }, (_, i) => i);
  readonly dnaStrandA = RegisterComponent.dnaPoints(1);
  readonly dnaStrandB = RegisterComponent.dnaPoints(-1);
  readonly dnaRungs = RegisterComponent.dnaRungList();

  steps: WizardStep[] = [
    { key: 'intro',     prompt: "Bonjour, bienvenue chez MediLink Tunisia. Je suis votre assistant santé. Je vais vous guider pour créer votre profil médical.", controls: [] },
    { key: 'name',      prompt: "Pour commencer, quel est votre nom complet ?", controls: ['firstName', 'lastName'] },
    { key: 'email',     prompt: "Enchanté. Quelle est votre adresse email ?", controls: ['email'] },
    { key: 'phone',     prompt: "Parfait. Et votre numéro de téléphone ?", controls: ['phone'] },
    { key: 'birthDate', prompt: "Quelle est votre date de naissance ?", controls: ['birthDate'] },
    { key: 'cin',       prompt: "Quel est votre numéro CIN ? Il vous servira à vous connecter.", controls: ['cin'] },
    { key: 'address',   prompt: "Où résidez-vous actuellement ?", controls: ['address'] },
    { key: 'gender',    prompt: "Quel est votre genre ? Je vais initialiser votre hologramme médical.", controls: ['gender'] },
    { key: 'metrics',   prompt: "Indiquez votre taille et votre poids — je calcule votre indice de santé en temps réel.", controls: [] },
    { key: 'blood',     prompt: "Quel est votre groupe sanguin ? (optionnel)", controls: [] },
    { key: 'medical',   prompt: "Parlons de vos antécédents médicaux. Je génère votre dossier au fur et à mesure.", controls: [] },
    { key: 'emergency', prompt: "Qui devons-nous contacter en cas d'urgence ?", controls: ['emergencyContactName', 'emergencyContactPhone'] },
    { key: 'insurance', prompt: "Disposez-vous d'une assurance santé ? (optionnel)", controls: [] },
    { key: 'security',  prompt: "Dernière étape : sécurisons votre compte.", controls: ['password', 'confirmPassword', 'acceptTerms'] }
  ];

  get step(): WizardStep {
    return this.steps[this.currentStep];
  }

  get totalSteps(): number {
    return this.steps.length - 1; // hors intro
  }

  get progress(): number {
    return Math.round((this.currentStep / (this.steps.length - 1)) * 100);
  }

  get isLast(): boolean {
    return this.currentStep === this.steps.length - 1;
  }

  /** Le formulaire a-t-il assez d'infos valides pour passer cette étape ? */
  stepValid(index: number = this.currentStep): boolean {
    const s = this.steps[index];
    if (!s) return false;
    return s.controls.every(c => !!this.registerForm.get(c)?.valid);
  }

  next(): void {
    if (!this.stepValid()) {
      this.step.controls.forEach(c => this.registerForm.get(c)?.markAsTouched());
      return;
    }
    if (this.currentStep < this.steps.length - 1) {
      this.currentStep++;
      this.scheduleTyping();
    }
  }

  back(): void {
    if (this.currentStep > 0) {
      this.currentStep--;
      this.scheduleTyping();
    }
  }

  /** Champs concernés par l'option « Je ne sais pas », par étape. */
  private readonly medicalFieldsByStep: { [key: string]: string[] } = {
    metrics: ['height', 'weight'],
    blood:   ['bloodGroup'],
    medical: ['allergies', 'chronicDiseases', 'currentTreatments']
  };

  /** L'étape courante propose-t-elle l'option « Je ne sais pas » ? */
  get canSkipMedical(): boolean {
    return this.step.key in this.medicalFieldsByStep;
  }

  /** Le patient déclare ne pas connaître ces informations : on vide les champs et on avance. */
  dontKnow(): void {
    const fields = this.medicalFieldsByStep[this.step.key] || [];
    fields.forEach(c => {
      const ctrl = this.registerForm.get(c);
      ctrl?.setValue(c === 'height' || c === 'weight' ? null : '');
      ctrl?.markAsUntouched();
    });
    if (this.currentStep < this.steps.length - 1) {
      this.currentStep++;
      this.scheduleTyping();
    }
  }

  /** Indicateur de saisie de l'assistant avant l'affichage de la question. */
  private scheduleTyping(): void {
    this.assistantTyping = true;
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => (this.assistantTyping = false), 850);
  }

  selectGender(value: string): void {
    this.f['gender'].setValue(value);
    this.f['gender'].markAsTouched();
  }

  selectBlood(value: string): void {
    this.f['bloodGroup'].setValue(value);
    this.f['bloodGroup'].markAsTouched();
  }

  get genderValue(): string {
    return this.f['gender'].value;
  }

  get bloodValue(): string {
    return this.f['bloodGroup'].value;
  }

  // ----- Indice de masse corporelle (affichage) -----
  get bmi(): number | null {
    const h = Number(this.f['height'].value);
    const w = Number(this.f['weight'].value);
    if (!h || !w) return null;
    const m = h / 100;
    return Math.round((w / (m * m)) * 10) / 10;
  }

  get bmiClass(): string {
    const b = this.bmi;
    if (b === null) return '';
    if (b < 18.5) return 'low';
    if (b < 25) return 'normal';
    if (b < 30) return 'high';
    return 'vhigh';
  }

  get bmiLabel(): string {
    const b = this.bmi;
    if (b === null) return '';
    if (b < 18.5) return 'Insuffisant';
    if (b < 25) return 'Normal';
    if (b < 30) return 'Surpoids';
    return 'Obésité';
  }

  // ----- Morphing de l'hologramme selon taille / poids (affichage) -----
  get bodyScaleX(): number {
    const w = Number(this.f['weight'].value);
    if (!w) return 1;
    return Math.max(0.82, Math.min(1.35, 0.7 + (w / 100) * 0.6));
  }

  get bodyScaleY(): number {
    const h = Number(this.f['height'].value);
    if (!h) return 1;
    return Math.max(0.88, Math.min(1.12, h / 175));
  }

  /** Le dossier médical commence à s'assembler à partir du groupe sanguin. */
  get medicalCardVisible(): boolean {
    return this.currentStep >= this.steps.findIndex(s => s.key === 'blood');
  }

  /** L'hologramme n'apparaît qu'à partir de l'étape du choix du genre. */
  get showHologram(): boolean {
    return this.currentStep >= this.steps.findIndex(s => s.key === 'gender');
  }

  // ----- Génération de l'hélice ADN décorative (viewBox 0 0 1200 220) -----
  private static dnaPoints(dir: 1 | -1): string {
    const pts: string[] = [];
    for (let i = 0; i <= 120; i++) {
      const x = i / 120;
      const y = 110 + dir * Math.sin(x * Math.PI * 4) * 70;
      pts.push(`${(x * 1200).toFixed(1)},${y.toFixed(1)}`);
    }
    return pts.join(' ');
  }

  private static dnaRungList(): { x: number; y1: number; y2: number; op: number }[] {
    const rungs = [];
    for (let i = 0; i <= 24; i++) {
      const x = i / 24;
      const phase = x * Math.PI * 4;
      const s = Math.sin(phase);
      rungs.push({
        x: +(x * 1200).toFixed(1),
        y1: +(110 + s * 70).toFixed(1),
        y2: +(110 - s * 70).toFixed(1),
        op: +(0.15 + 0.5 * (0.5 + 0.5 * Math.cos(phase))).toFixed(2)
      });
    }
    return rungs;
  }
}
