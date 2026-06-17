import { adminGuard } from './admin.guard';
import { setupGuardTest } from './guard-test-helpers';

describe('adminGuard', () => {
  const { mockAuthService, mockRouter, executeGuard, configureTestBed } = setupGuardTest(adminGuard, ['isAuthenticated', 'getUserRole']);

  beforeEach(() => configureTestBed());

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
