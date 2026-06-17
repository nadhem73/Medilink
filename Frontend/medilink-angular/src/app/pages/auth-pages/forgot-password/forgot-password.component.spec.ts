import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { ForgotPasswordComponent } from './forgot-password.component';
import { AuthService } from '../../../core/services/auth.service';

describe('ForgotPasswordComponent', () => {
  let component: ForgotPasswordComponent;
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['forgotPassword']);
    authServiceSpy.forgotPassword.and.returnValue(of({ message: 'ok', success: true }));

    await TestBed.configureTestingModule({
      declarations: [ForgotPasswordComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: AuthService, useValue: authServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ForgotPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have form with email field', () => {
    expect(component.forgotForm).toBeDefined();
    expect(component.forgotForm.contains('identifier')).toBeTrue();
  });

  it('should call forgotPassword on submit', () => {
    component.forgotForm.patchValue({ identifier: 'test@example.com' });
    component.selectedRole = 'patient';
    component.onSubmit();
    expect(authServiceSpy.forgotPassword).toHaveBeenCalled();
  });
});
