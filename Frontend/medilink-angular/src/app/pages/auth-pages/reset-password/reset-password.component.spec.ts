import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';

import { ResetPasswordComponent } from './reset-password.component';
import { AuthService } from '../../../core/services/auth.service';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const activatedRouteStub = {
    snapshot: { queryParams: { token: 'test-token-123' } }
  };
  const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['resetPassword']);
    authServiceSpy.resetPassword.and.returnValue(of({ message: 'ok', success: true }));

    await TestBed.configureTestingModule({
      declarations: [ResetPasswordComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: Router, useValue: routerSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have form with password fields', () => {
    expect(component.resetForm).toBeDefined();
    expect(component.resetForm.contains('newPassword')).toBeTrue();
    expect(component.resetForm.contains('confirmPassword')).toBeTrue();
  });

  it('should call resetPassword on submit', () => {
    component.resetForm.patchValue({ newPassword: 'NewPass123!', confirmPassword: 'NewPass123!' });
    component.onSubmit();
    expect(authServiceSpy.resetPassword).toHaveBeenCalled();
  });
});
