import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

/** Espaces gérés ici (la pharmacie est ignorée pour le moment). */
type RoleKey = 'patient' | 'doctor';

interface RoleTab {
  key: RoleKey;
  label: string;
  idLabel: string;
  idPlaceholder: string;
  idType: 'text' | 'email';
}

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent implements OnInit {
  forgotForm!: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';

  readonly roles: RoleTab[] = [
    { key: 'patient', label: 'Patient', idLabel: 'Email du compte',  idPlaceholder: 'vous@exemple.com', idType: 'email' },
    { key: 'doctor',  label: 'Médecin', idLabel: "Numéro d'ordre",   idPlaceholder: 'Ex : 12345',       idType: 'text'  }
  ];

  selectedRole: RoleKey = 'patient';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.forgotForm = this.fb.group({
      identifier: ['', [Validators.required]]
    });
  }

  selectRole(key: RoleKey): void {
    if (this.selectedRole === key) return;
    this.selectedRole = key;
    this.errorMessage = '';
    this.successMessage = '';
    this.forgotForm.reset();

    // Le patient saisit un email → on ajoute la validation email.
    const idCtrl = this.forgotForm.get('identifier');
    const validators = key === 'patient'
      ? [Validators.required, Validators.email]
      : [Validators.required];
    idCtrl?.setValidators(validators);
    idCtrl?.updateValueAndValidity();
  }

  get activeRole(): RoleTab {
    return this.roles.find(r => r.key === this.selectedRole) ?? this.roles[0];
  }

  get activeIndex(): number {
    return this.roles.findIndex(r => r.key === this.selectedRole);
  }

  onSubmit(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.get('identifier')?.markAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const identifier = (this.forgotForm.value.identifier || '').trim();
    const payload = this.selectedRole === 'patient'
      ? { role: 'patient', email: identifier }
      : { role: 'doctor', licenseNumber: identifier };

    this.authService.forgotPassword(payload).subscribe({
      next: (res) => {
        this.loading = false;
        this.successMessage = res.message
          || 'Si un compte correspond, un lien de réinitialisation a été envoyé par email.';
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Une erreur est survenue. Réessayez plus tard.';
      }
    });
  }
}
