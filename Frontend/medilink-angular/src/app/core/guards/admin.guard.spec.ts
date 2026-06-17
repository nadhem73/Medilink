import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { adminGuard } from './admin.guard';

describe('adminGuard', () => {
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockRoute = {} as any;
  const mockState = { url: '/dashboard/admin' } as any;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['isAuthenticated', 'getUserRole']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });
  });

  it('should allow access when authenticated with ADMIN role', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);
    mockAuthService.getUserRole.and.returnValue(['ADMIN']);
    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));
    expect(result).toBeTrue();
  });

  it('should allow access with ROLE_ADMIN format', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);
    mockAuthService.getUserRole.and.returnValue(['ROLE_ADMIN']);
    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));
    expect(result).toBeTrue();
  });

  it('should redirect to login when not authenticated', () => {
    mockAuthService.isAuthenticated.and.returnValue(false);
    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));
    expect(result).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login'], {
      queryParams: { returnUrl: '/dashboard/admin' }
    });
  });

  it('should redirect to home when not admin', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);
    mockAuthService.getUserRole.and.returnValue(['DOCTOR']);
    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));
    expect(result).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
