import { authGuard } from './auth.guard';
import { setupGuardTest } from './guard-test-helpers';

describe('authGuard', () => {
  const { mockAuthService, mockRouter, executeGuard, configureTestBed } = setupGuardTest(authGuard, ['isAuthenticated']);

  beforeEach(() => configureTestBed());

  it('should return true when user is authenticated', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);

    const result = executeGuard({} as any, { url: '/protected' } as any);

    expect(result).toBeTrue();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should navigate to /auth/login with returnUrl and return false when not authenticated', () => {
    mockAuthService.isAuthenticated.and.returnValue(false);

    const result = executeGuard({} as any, { url: '/protected-page' } as any);

    expect(result).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['/auth/login'],
      { queryParams: { returnUrl: '/protected-page' } }
    );
  });
});
