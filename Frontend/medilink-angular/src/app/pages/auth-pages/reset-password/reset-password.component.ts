import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
  resetForm!: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  showPassword = false;
  showConfirm = false;
  token = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';

    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: ResetPasswordComponent.passwordsMatch });

    if (!this.token) {
      this.errorMessage = 'Lien de réinitialisation invalide ou incomplet.';
    }
  }

  /** Vérifie que les deux mots de passe sont identiques. */
  private static passwordsMatch(group: AbstractControl): ValidationErrors | null {
    const pwd = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pwd && confirm && pwd !== confirm ? { mismatch: true } : null;
  }

  onSubmit(): void {
    if (!this.token) {
      this.errorMessage = 'Lien de réinitialisation invalide ou incomplet.';
      return;
    }

    if (this.resetForm.invalid) {
      Object.keys(this.resetForm.controls).forEach(k => this.resetForm.get(k)?.markAsTouched());
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.resetPassword({
      token: this.token,
      newPassword: this.resetForm.value.newPassword
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.successMessage = res.message || 'Votre mot de passe a été réinitialisé.';
        setTimeout(() => this.router.navigate(['/auth/login']), 2500);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message
          || 'Le lien est invalide ou expiré. Refaites une demande de réinitialisation.';
      }
    });
  }
}
