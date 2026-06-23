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

  function testReturnsTrueForRole(role: string): void {
    it(`should return true when authenticated and user has ${role} role`, () => {
      mockAuthService.isAuthenticated.and.returnValue(true);
      mockAuthService.getUserRole.and.returnValue([role]);

      const result = executeGuard({} as any, { url: '/admin' } as any);

      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  }

  testReturnsTrueForRole('ADMIN');
  testReturnsTrueForRole('ROLE_ADMIN');

  it('should return false and navigate to / when authenticated but not admin', () => {
    mockAuthService.isAuthenticated.and.returnValue(true);
    mockAuthService.getUserRole.and.returnValue(['PATIENT']);

    const result = executeGuard({} as any, { url: '/admin' } as any);

    expect(result).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
