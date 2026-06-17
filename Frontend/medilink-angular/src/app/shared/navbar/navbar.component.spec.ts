import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { NavbarComponent } from './navbar.component';
import { AuthService } from '../../core/services/auth.service';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let authService: any;
  let router: Router;
  let currentUserSubject: BehaviorSubject<any>;

  const mockUser = {
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    roles: ['ROLE_DOCTOR'],
    avatarUrl: 'http://example.com/avatar.jpg',
  };

  beforeEach(async () => {
    currentUserSubject = new BehaviorSubject<any>(null);
    const mockAuth = {
      currentUser$: currentUserSubject.asObservable(),
      logout: jasmine.createSpy('logout'),
    };

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [NavbarComponent],
      providers: [
        { provide: AuthService, useValue: mockAuth },
      ],
    }).compileComponents();

    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);

    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function setUser(user: any | null) {
    currentUserSubject.next(user);
    fixture.detectChanges();
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('when unauthenticated', () => {
    it('should have isAuthenticated as false', () => {
      expect(component.isAuthenticated).toBeFalse();
    });

    it('should display login and register links', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.btn-login')).toBeTruthy();
      expect(compiled.querySelector('.btn-register')).toBeTruthy();
    });

    it('should not display user menu', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.user-menu')).toBeFalsy();
    });
  });

  describe('when authenticated', () => {
    beforeEach(() => {
      setUser(mockUser);
    });

    it('should have isAuthenticated as true', () => {
      expect(component.isAuthenticated).toBeTrue();
    });

    it('should set currentUser from auth service', () => {
      expect(component.currentUser).toEqual(mockUser);
    });

    it('should display user menu when authenticated', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.user-menu')).toBeTruthy();
    });

    it('should display the user display name', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.user-trigger-name')?.textContent).toContain('John Doe');
    });

    it('should have correct panelRoute for doctor', () => {
      expect(component.panelRoute).toBe('/dashboard/doctor');
    });

    it('should have correct panelLabel for doctor', () => {
      expect(component.panelLabel).toBe('Doctor panel');
    });

    it('should return avatarUrl from currentUser', () => {
      expect(component.avatarUrl).toBe('http://example.com/avatar.jpg');
    });

    it('should return avatarInitial from firstName', () => {
      expect(component.avatarInitial).toBe('J');
    });

    describe('toggleUserMenu', () => {
      it('should toggle isUserMenuOpen', () => {
        const event = new MouseEvent('click');
        component.isUserMenuOpen = false;
        component.toggleUserMenu(event);
        expect(component.isUserMenuOpen).toBeTrue();
        component.toggleUserMenu(event);
        expect(component.isUserMenuOpen).toBeFalse();
      });

      it('should stop event propagation', () => {
        const event = new MouseEvent('click');
        spyOn(event, 'stopPropagation');
        component.toggleUserMenu(event);
        expect(event.stopPropagation).toHaveBeenCalled();
      });
    });

    describe('goToPanel', () => {
      it('should navigate to panelRoute', () => {
        spyOn(router, 'navigateByUrl');
        component.goToPanel();
        expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard/doctor');
        expect(component.isUserMenuOpen).toBeFalse();
      });
    });

    describe('goToSettings', () => {
      it('should navigate to panelRoute', () => {
        spyOn(router, 'navigateByUrl');
        component.goToSettings();
        expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard/doctor');
        expect(component.isUserMenuOpen).toBeFalse();
      });
    });

    describe('logout', () => {
      it('should call authService.logout and navigate home', () => {
        spyOn(router, 'navigate');
        component.logout();
        expect(authService.logout).toHaveBeenCalled();
        expect(router.navigate).toHaveBeenCalledWith(['/']);
        expect(component.isUserMenuOpen).toBeFalse();
      });
    });

    describe('onAvatarError', () => {
      it('should set avatarError to true', () => {
        component.avatarError = false;
        component.onAvatarError();
        expect(component.avatarError).toBeTrue();
      });
    });

    describe('displayName', () => {
      it('should return full name when firstName and lastName exist', () => {
        component.currentUser = { ...mockUser, firstName: 'Jane', lastName: 'Smith' };
        expect(component.displayName).toBe('Jane Smith');
      });

      it('should return email prefix when no name', () => {
        component.currentUser = { ...mockUser, firstName: '', lastName: '' };
        expect(component.displayName).toBe('john');
      });

      it('should return Utilisateur when nothing available', () => {
        (component as any).currentUser = { ...mockUser, firstName: '', lastName: '', email: '' };
        expect(component.displayName).toBe('Utilisateur');
      });
    });

    describe('avatarInitial', () => {
      it('should return first char of firstName', () => {
        expect(component.avatarInitial).toBe('J');
      });

      it('should return U when no user info', () => {
        (component as any).currentUser = { ...mockUser, firstName: '', lastName: '', email: '' };
        expect(component.avatarInitial).toBe('U');
      });
    });

    describe('panelRoute by role', () => {
      it('should return admin route for ADMIN role', () => {
        component.currentUser = { ...mockUser, roles: ['ROLE_ADMIN'] };
        expect(component.panelRoute).toBe('/dashboard/admin');
      });

      it('should return pharmacy route for PHARMACY role', () => {
        component.currentUser = { ...mockUser, roles: ['ROLE_PHARMACY'] };
        expect(component.panelRoute).toBe('/dashboard/pharmacy');
      });

      it('should return patient route for unknown role', () => {
        component.currentUser = { ...mockUser, roles: ['ROLE_UNKNOWN'] };
        expect(component.panelRoute).toBe('/dashboard/patient');
      });
    });

    describe('closeUserMenu', () => {
      it('should close user menu on document click', () => {
        component.isUserMenuOpen = true;
        component.closeUserMenu();
        expect(component.isUserMenuOpen).toBeFalse();
      });
    });
  });
});
