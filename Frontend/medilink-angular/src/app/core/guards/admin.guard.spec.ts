import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { Router } from '@angular/router';
import { adminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';

describe('adminGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => adminGuard(...guardParameters));

  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

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

  it('should return false and navigate to /auth/login when not authenticated', () => {
    mockAuthService.isAuthenticated.and.returnValue(false);

    const result = executeGuard({} as any, { url: '/admin' } as any);

    expect(result).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['/auth/login'],
      { queryParams: { returnUrl: '/admin' } }
    );
  });

  it('should return true when authenticated and user has ADMIN role', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);
    mockAuthService.getUserRole.and.returnValue(['ADMIN']);

    const result = executeGuard({} as any, { url: '/admin' } as any);

    expect(result).toBeTrue();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should return true when authenticated and user has ROLE_ADMIN role', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);
    mockAuthService.getUserRole.and.returnValue(['ROLE_ADMIN']);

    const result = executeGuard({} as any, { url: '/admin' } as any);

    expect(result).toBeTrue();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should return false and navigate to / when authenticated but not admin', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);
    mockAuthService.getUserRole.and.returnValue(['PATIENT']);

    const result = executeGuard({} as any, { url: '/admin' } as any);

    expect(result).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
