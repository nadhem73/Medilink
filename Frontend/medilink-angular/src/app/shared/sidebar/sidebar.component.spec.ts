import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, throwError, of } from 'rxjs';
import { SidebarComponent } from './sidebar.component';
import { AuthService } from '../../core/services/auth.service';

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;
  let authService: any;
  let router: Router;
  let currentUserSubject: BehaviorSubject<any>;

  const mockUser = {
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    roles: ['ROLE_DOCTOR'],
    avatarUrl: 'http://example.com/avatar.jpg',
    isEmailVerified: true,
  };

  function createMockAuthService(initialUser: any): any {
    currentUserSubject = new BehaviorSubject<any>(initialUser);
    return {
      currentUser$: currentUserSubject,
      logout: jasmine.createSpy('logout'),
      requestEmailVerification: jasmine.createSpy('requestEmailVerification'),
      verifyEmailOtp: jasmine.createSpy('verifyEmailOtp'),
    };
  }

  function setup(user: any, url: string) {
    const userCopy = user ? { ...user } : null;
    const mockAuth = createMockAuthService(userCopy);
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, FormsModule],
      declarations: [SidebarComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: AuthService, useValue: mockAuth },
      ],
    });

    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);

    spyOnProperty(router, 'url', 'get').and.returnValue(url);

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', () => {
    setup(null, '/dashboard/patient');
    expect(component).toBeTruthy();
  });

  describe('with doctor user on doctor dashboard', () => {
    beforeEach(() => setup(mockUser, '/dashboard/doctor'));

    it('should set currentUser from auth service', () => {
      expect(component.currentUser).toEqual(mockUser);
    });

    it('should be identified as doctor', () => {
      expect(component.isDoctor).toBeTrue();
      expect(component.isAdmin).toBeFalse();
      expect(component.isPharmacy).toBeFalse();
    });

    it('should return doctor links', () => {
      expect(component.links).toBe(component.doctorLinks);
    });

    it('should have role label Medecin', () => {
      expect(component.roleLabel).toBe('Medecin');
    });

    it('should prefix display name with Dr', () => {
      expect(component.displayName).toBe('Dr John Doe');
    });

    it('should return D as avatar initial for doctor when no name', () => {
      Object.assign(component.currentUser, { firstName: '', lastName: '', email: '' });
      expect(component.avatarInitial).toBe('D');
    });

    it('should return email prefix with Dr when no name', () => {
      component.currentUser = { ...component.currentUser, firstName: '', lastName: '', email: 'doc@test.com' };
      expect(component.displayName).toBe('Dr doc');
    });

    it('should be verified', () => {
      expect(component.isVerified).toBeTrue();
    });

    it('should return avatarUrl', () => {
      expect(component.avatarUrl).toBe('http://example.com/avatar.jpg');
    });

    it('should return null avatarUrl when no url fields', () => {
      Object.assign(component.currentUser, { imageUrl: null, avatarUrl: null, photoUrl: null, photo: null });
      expect(component.avatarUrl).toBeNull();
    });

    it('should call authService.logout and navigate on logout', () => {
      spyOn(router, 'navigate');
      component.logout();
      expect(authService.logout).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should set avatarError on error', () => {
      component.onAvatarError();
      expect(component.avatarError).toBeTrue();
    });
  });

  function testRoleIdentification(roleKey: string, roleLabel: string, initials: string, url: string) {
    const isProp = `is${roleKey.charAt(0).toUpperCase() + roleKey.slice(1)}` as keyof SidebarComponent;
    const linksProp = `${roleKey}Links` as keyof SidebarComponent;

    it(`should be identified as ${roleKey}`, () => {
      if (roleKey === 'patient') {
        expect(component.isDoctor).toBeFalse();
        expect(component.isAdmin).toBeFalse();
        expect(component.isPharmacy).toBeFalse();
      } else {
        expect((component as any)[isProp]).toBeTrue();
      }
    });

    it(`should return ${roleKey} links`, () => {
      expect(component.links).toBe((component as any)[linksProp]);
    });

    it(`should have role label ${roleLabel}`, () => {
      expect(component.roleLabel).toBe(roleLabel);
    });

    it(`should return ${initials} as avatar initial for ${roleKey} when no name`, () => {
      Object.assign(component.currentUser, { firstName: '', lastName: '', email: '' });
      expect(component.avatarInitial).toBe(initials);
    });
  }

  describe('with admin user on admin dashboard', () => {
    beforeEach(() => setup({ ...mockUser, roles: ['ROLE_ADMIN'] }, '/dashboard/admin'));
    testRoleIdentification('admin', 'Administrateur', 'A', '/dashboard/admin');
  });

  describe('with pharmacy user on pharmacy dashboard', () => {
    beforeEach(() => setup({ ...mockUser, roles: ['ROLE_PHARMACY'] }, '/dashboard/pharmacy'));
    testRoleIdentification('pharmacy', 'Pharmacie', 'Ph', '/dashboard/pharmacy');
  });

  describe('as patient (default)', () => {
    beforeEach(() => setup({ ...mockUser, roles: ['ROLE_PATIENT'] }, '/dashboard/patient'));
    testRoleIdentification('patient', 'Patient', 'P', '/dashboard/patient');

    it('should display name without Dr prefix', () => {
      expect(component.displayName).toBe('John Doe');
    });
  });

  describe('OTP Verification Modal', () => {
    beforeEach(() => setup({ ...mockUser, isEmailVerified: false }, '/dashboard/doctor'));

    it('should not be verified', () => {
      expect(component.isVerified).toBeFalse();
    });

    describe('triggerVerification', () => {
      it('should open modal and request email verification on success', () => {
        authService.requestEmailVerification.and.returnValue(of({ message: 'OK', success: true }));
        component.triggerVerification();
        expect(component.showVerificationModal).toBeTrue();
        expect(authService.requestEmailVerification).toHaveBeenCalled();
      });

      it('should set error message on failure', fakeAsync(() => {
        authService.requestEmailVerification.and.returnValue(throwError(() => new Error('fail')));
        component.triggerVerification();
        tick();
        expect(component.otpError).toContain('Impossible');
        expect(component.otpLoading).toBeFalse();
      }));
    });

    describe('verifyOtp', () => {
      it('should show error if code is not 6 digits', () => {
        component.otpCode = '123';
        component.verifyOtp();
        expect(component.otpError).toContain('6 chiffres');
        expect(component.otpLoading).toBeFalse();
      });

      it('should call verifyEmailOtp on valid code', () => {
        authService.verifyEmailOtp.and.returnValue(of({}));
        component.otpCode = '123456';
        component.verifyOtp();
        expect(authService.verifyEmailOtp).toHaveBeenCalledWith('123456');
      });

      it('should set success message on verification success', () => {
        authService.verifyEmailOtp.and.returnValue(of({}));
        component.otpCode = '123456';
        component.verifyOtp();
        expect(component.otpSuccess).toContain('vérifiée');
      });

      it('should set error message on verification failure', () => {
        authService.verifyEmailOtp.and.returnValue(throwError(() => ({ error: { message: 'Invalid code' } })));
        component.otpCode = '123456';
        component.verifyOtp();
        expect(component.otpError).toContain('Invalid code');
        expect(component.otpLoading).toBeFalse();
      });
    });

    describe('resendOtp', () => {
      it('should not resend if countdown > 0', () => {
        component.resendCountdown = 30;
        component.resendOtp();
        expect(authService.requestEmailVerification).not.toHaveBeenCalled();
      });

      it('should resend if countdown is 0', () => {
        authService.requestEmailVerification.and.returnValue(of({ message: 'OK', success: true }));
        component.resendCountdown = 0;
        component.resendOtp();
        expect(authService.requestEmailVerification).toHaveBeenCalled();
      });
    });

    describe('closeVerificationModal', () => {
      it('should reset modal state', () => {
        component.showVerificationModal = true;
        component.otpCode = '123';
        component.otpError = 'error';
        component.otpSuccess = 'success';
        component.closeVerificationModal();
        expect(component.showVerificationModal).toBeFalse();
        expect(component.otpCode).toBe('');
        expect(component.otpError).toBe('');
        expect(component.otpSuccess).toBe('');
      });
    });

    describe('startResendCountdown', () => {
      beforeEach(() => {
        jasmine.clock().install();
      });

      afterEach(() => {
        jasmine.clock().uninstall();
      });

      it('should set countdown to 60 and decrement', () => {
        component.startResendCountdown();
        expect(component.resendCountdown).toBe(60);
        jasmine.clock().tick(1000);
        expect(component.resendCountdown).toBe(59);
        jasmine.clock().tick(59000);
        expect(component.resendCountdown).toBe(0);
      });
    });
  });
});
