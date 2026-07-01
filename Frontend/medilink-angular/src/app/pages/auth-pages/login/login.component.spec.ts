import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockActivatedRoute = {
    snapshot: {
      queryParams: { returnUrl: '/dashboard' }
    }
  };

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['login']);
    const routerSpyObj = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpyObj },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty fields', () => {
    expect(component.loginForm.get('identifier')?.value).toBe('');
    expect(component.loginForm.get('password')?.value).toBe('');
  });

  it('should have selectedRole default to patient', () => {
    expect(component.selectedRole).toBe('patient');
  });

  it('should have three role tabs', () => {
    expect(component.roles.length).toBe(3);
    expect(component.roles[0].key).toBe('patient');
    expect(component.roles[1].key).toBe('doctor');
    expect(component.roles[2].key).toBe('pharmacy');
  });

  it('should validate required fields', () => {
    const identifier = component.loginForm.get('identifier');
    const password = component.loginForm.get('password');

    identifier?.setValue('');
    password?.setValue('');
    expect(identifier?.valid).toBeFalse();
    expect(password?.valid).toBeFalse();

    identifier?.setValue('12345678');
    password?.setValue('password123');
    expect(identifier?.valid).toBeTrue();
    expect(password?.valid).toBeTrue();
  });

  it('should call authService.login() on valid submit for patient role', () => {
    authServiceSpy.login.and.returnValue(of({} as any));

    component.selectedRole = 'patient';
    component.loginForm.get('identifier')?.setValue('12345678');
    component.loginForm.get('password')?.setValue('secret123');
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      cin: '12345678',
      password: 'secret123'
    });
  });

  it('should use licenseNumber as identifier for doctor role', () => {
    authServiceSpy.login.and.returnValue(of({} as any));

    component.selectedRole = 'doctor';
    component.loginForm.get('identifier')?.setValue('54321');
    component.loginForm.get('password')?.setValue('secret123');
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      licenseNumber: '54321',
      password: 'secret123'
    });
  });

  it('should use licenseNumber as identifier for pharmacy role', () => {
    authServiceSpy.login.and.returnValue(of({} as any));

    component.selectedRole = 'pharmacy';
    component.loginForm.get('identifier')?.setValue('PH-00123');
    component.loginForm.get('password')?.setValue('secret123');
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      licenseNumber: 'PH-00123',
      password: 'secret123'
    });
  });

  it('should show error message on login failure', () => {
    const errorResponse = { error: { message: 'Identifiants invalides' } };
    authServiceSpy.login.and.returnValue(throwError(() => errorResponse));

    component.loginForm.get('identifier')?.setValue('12345678');
    component.loginForm.get('password')?.setValue('wrongpass');
    component.onSubmit();

    fixture.detectChanges();

    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Identifiants invalides');
  });

  it('should show default error message when no server message', () => {
    authServiceSpy.login.and.returnValue(throwError(() => ({})));

    component.loginForm.get('identifier')?.setValue('12345678');
    component.loginForm.get('password')?.setValue('wrongpass');
    component.onSubmit();

    expect(component.errorMessage).toBe('Identifiant ou mot de passe incorrect');
  });

  it('should navigate on successful login', () => {
    authServiceSpy.login.and.returnValue(of({} as any));

    component.returnUrl = '/';
    component.loginForm.get('identifier')?.setValue('12345678');
    component.loginForm.get('password')?.setValue('secret123');
    component.onSubmit();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should not submit when form is invalid', () => {
    component.loginForm.get('identifier')?.setValue('');
    component.loginForm.get('password')?.setValue('');
    component.onSubmit();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should switch role with selectRole method', () => {
    component.selectRole('doctor');
    expect(component.selectedRole).toBe('doctor');
    expect(component.errorMessage).toBe('');

    component.selectRole('pharmacy');
    expect(component.selectedRole).toBe('pharmacy');
  });

  it('should not switch role if same role is selected', () => {
    component.selectRole('patient');
    expect(component.selectedRole).toBe('patient');
  });

  it('should render the returnUrl from query params', () => {
    expect(component.returnUrl).toBe('/dashboard');
  });
});
