import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

/** Espaces de connexion proposés en onglets. */
type RoleKey = 'patient' | 'doctor' | 'pharmacy';

/** Métadonnées d'affichage d'un onglet + champ identifiant associé. */
interface RoleTab {
  key: RoleKey;
  label: string;
  idLabel: string;            // libellé du champ identifiant
  idPlaceholder: string;
  idType: 'text' | 'email';
  idAutocomplete: string;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  errorMessage = '';
  returnUrl = '/';
  showPassword = false;

  // ---- Sélecteur d'espace (Patient / Médecin / Pharmacie) ----
  readonly roles: RoleTab[] = [
    { key: 'patient',  label: 'Patient',   idLabel: 'Numéro CIN',       idPlaceholder: 'Ex : 12345678',          idType: 'text',  idAutocomplete: 'off' },
    { key: 'doctor',   label: 'Médecin',   idLabel: "Numéro d'ordre",   idPlaceholder: 'Ex : 12345',             idType: 'text',  idAutocomplete: 'off' },
    { key: 'pharmacy', label: 'Pharmacie', idLabel: 'Email',            idPlaceholder: 'officine@medilink.tn',   idType: 'email', idAutocomplete: 'email' }
  ];

  selectedRole: RoleKey = 'patient';

  selectRole(key: RoleKey): void {
    if (this.selectedRole === key) return;
    this.selectedRole = key;
    this.errorMessage = '';
  }

  get activeRole(): RoleTab {
    return this.roles.find(r => r.key === this.selectedRole) ?? this.roles[0];
  }

  get activeIndex(): number {
    return this.roles.findIndex(r => r.key === this.selectedRole);
  }

  // ---- Décor d'arrière-plan médical (présentation pure, calculé une fois) ----
  readonly bgBokeh = Array.from({ length: 12 }, (_, i) => i);
  readonly dnaStrandA = LoginComponent.dnaPoints(1);
  readonly dnaStrandB = LoginComponent.dnaPoints(-1);
  readonly dnaRungs = LoginComponent.dnaRungList();

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

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      identifier: ['', [Validators.required]],
      password: ['', Validators.required]
    });

    // Récupérer l'URL de retour
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const identifier = (this.loginForm.value.identifier || '').trim();
      const password = this.loginForm.value.password;

      // Patient → CIN ; médecin → numéro d'ordre ; pharmacie → email.
      let credentials: { cin?: string; licenseNumber?: string; email?: string; password: string };
      if (this.selectedRole === 'patient') {
        credentials = { cin: identifier, password };
      } else if (this.selectedRole === 'doctor') {
        credentials = { licenseNumber: identifier, password };
      } else {
        credentials = { email: identifier, password };
      }

      this.authService.login(credentials).subscribe({
        next: () => {
          this.loading = false;

          // Apres connexion, on renvoie l'utilisateur vers la home.
          // Il accedera ensuite a son panel via le menu du navbar.
          this.router.navigate(['/']);
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = error.error?.message || 'Identifiant ou mot de passe incorrect';
          console.error('Erreur de connexion:', error);
        }
      });
    } else {
      // Marquer tous les champs comme touchés pour afficher les erreurs
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
    }
  }
}
