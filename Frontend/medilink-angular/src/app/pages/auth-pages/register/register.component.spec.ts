import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../../core/services/auth.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  const validFormValues = {
    firstName: 'Ahmed',
    lastName: 'Ben Ali',
    email: 'ahmed@test.tn',
    phone: '+21650123456',
    birthDate: '1990-01-15',
    gender: 'MALE',
    address: 'Tunis',
    cin: '12345678',
    bloodGroup: '',
    height: null,
    weight: null,
    allergies: '',
    chronicDiseases: '',
    currentTreatments: '',
    emergencyContactName: 'Test Contact',
    emergencyContactPhone: '+21650123457',
    insuranceCompany: '',
    insuranceNumber: '',
    password: 'StrongPass1!',
    confirmPassword: 'StrongPass1!',
    acceptTerms: true,
    role: 'PATIENT'
  };

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['register', 'login']);
    const routerSpyObj = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpyObj }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with all fields', () => {
    expect(component.registerForm).toBeDefined();
    expect(component.registerForm.get('firstName')).toBeDefined();
    expect(component.registerForm.get('lastName')).toBeDefined();
    expect(component.registerForm.get('email')).toBeDefined();
    expect(component.registerForm.get('phone')).toBeDefined();
    expect(component.registerForm.get('birthDate')).toBeDefined();
    expect(component.registerForm.get('gender')).toBeDefined();
    expect(component.registerForm.get('address')).toBeDefined();
    expect(component.registerForm.get('cin')).toBeDefined();
    expect(component.registerForm.get('password')).toBeDefined();
    expect(component.registerForm.get('confirmPassword')).toBeDefined();
    expect(component.registerForm.get('acceptTerms')).toBeDefined();
    expect(component.registerForm.get('role')).toBeDefined();
  });

  it('should have empty initial field values', () => {
    expect(component.registerForm.get('firstName')?.value).toBe('');
    expect(component.registerForm.get('email')?.value).toBe('');
    expect(component.registerForm.get('password')?.value).toBe('');
    expect(component.registerForm.get('acceptTerms')?.value).toBeFalse();
    expect(component.registerForm.get('role')?.value).toBe('PATIENT');
  });

  it('should validate required fields', () => {
    expect(component.registerForm.get('firstName')?.valid).toBeFalse();
    expect(component.registerForm.get('email')?.valid).toBeFalse();
    expect(component.registerForm.get('password')?.valid).toBeFalse();

    component.registerForm.get('firstName')?.setValue('Ahmed');
    expect(component.registerForm.get('firstName')?.valid).toBeTrue();
  });

  it('should validate email format', () => {
    const email = component.registerForm.get('email');
    email?.setValue('invalid');
    expect(email?.valid).toBeFalse();

    email?.setValue('valid@email.tn');
    expect(email?.valid).toBeTrue();
  });

  it('should validate password minimum length', () => {
    const password = component.registerForm.get('password');
    password?.setValue('short');
    expect(password?.valid).toBeFalse();

    password?.setValue('LongEnough1!');
    expect(password?.valid).toBeTrue();
  });

  it('should validate confirmPassword match', () => {
    component.registerForm.get('password')?.setValue('StrongPass1!');
    component.registerForm.get('confirmPassword')?.setValue('DifferentPass1!');

    const confirm = component.registerForm.get('confirmPassword');
    expect(confirm?.errors?.['passwordMismatch']).toBeTrue();

    component.registerForm.get('confirmPassword')?.setValue('StrongPass1!');
    fixture.detectChanges();
    expect(confirm?.errors?.['passwordMismatch']).toBeFalsy();
  });

  it('should require acceptTerms to be true', () => {
    const acceptTerms = component.registerForm.get('acceptTerms');
    expect(acceptTerms?.valid).toBeFalse();

    acceptTerms?.setValue(true);
    expect(acceptTerms?.valid).toBeTrue();
  });

  it('should call authService.register on valid submit', () => {
    authServiceSpy.register.and.returnValue(of({} as any));
    authServiceSpy.login.and.returnValue(of({} as any));

    component.registerForm.patchValue(validFormValues);
    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalled();
  });

  it('should not submit when form is invalid', () => {
    component.onSubmit();
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should show error message on registration failure', () => {
    const errorResponse = { error: { message: 'Email already exists' } };
    authServiceSpy.register.and.returnValue(throwError(() => errorResponse));

    component.registerForm.patchValue(validFormValues);
    component.onSubmit();

    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Email already exists');
  });

  it('should show default error message when no server message', () => {
    authServiceSpy.register.and.returnValue(throwError(() => ({})));

    component.registerForm.patchValue(validFormValues);
    component.onSubmit();

    expect(component.errorMessage).toBe('Une erreur est survenue. Veuillez réessayer.');
  });

  it('should navigate to login on successful registration if auto-login fails', () => {
    authServiceSpy.register.and.returnValue(of({} as any));
    authServiceSpy.login.and.returnValue(throwError(() => ({})));

    component.registerForm.patchValue(validFormValues);
    component.onSubmit();

    expect(component.successMessage).toBe('Compte créé ! Veuillez vous connecter.');
  });

  it('should have passwordStrength computed properties', () => {
    const password = component.registerForm.get('password');
    expect(component.passwordStrength).toBe(0);

    password?.setValue('Abcdef1!');
    expect(component.passwordStrength).toBeGreaterThan(0);
    expect(component.passwordStrengthClass).toBeDefined();
    expect(component.passwordStrengthLabel).toBeDefined();
  });

  it('should navigate to next wizard step', () => {
    component.currentStep = 0;
    component.next();
    expect(component.currentStep).toBe(1);
  });

  it('should navigate to previous wizard step', () => {
    component.currentStep = 2;
    component.back();
    expect(component.currentStep).toBe(1);
  });

  it('should select gender', () => {
    component.selectGender('FEMALE');
    expect(component.registerForm.get('gender')?.value).toBe('FEMALE');
  });

  it('should select blood group', () => {
    component.selectBlood('A+');
    expect(component.registerForm.get('bloodGroup')?.value).toBe('A+');
  });

  it('should have blood groups defined', () => {
    expect(component.bloodGroups).toEqual(['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-']);
  });

  it('should compute BMI', () => {
    component.registerForm.get('height')?.setValue(175);
    component.registerForm.get('weight')?.setValue(70);
    expect(component.bmi).toBe(22.9);
    expect(component.bmiClass).toBe('normal');
    expect(component.bmiLabel).toBe('Normal');
  });
});
