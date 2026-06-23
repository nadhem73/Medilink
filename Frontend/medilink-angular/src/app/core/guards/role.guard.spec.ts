import { roleGuard } from './role.guard';
import { setupGuardTest } from './guard-test-helpers';

describe('roleGuard', () => {
  const { mockAuthService, mockRouter, executeGuard, configureTestBed } = setupGuardTest(roleGuard, ['getUserRole']);

  beforeEach(() => configureTestBed());

  it('should return true when user has expected role from route data', () => {
    const roles = ['ADMIN'];
    mockAuthService.getUserRole.and.returnValue(roles);

    const route = { data: { roles } } as any;
    const result = executeGuard(route, { url: '/admin' } as any);

    expect(result).toBeTrue();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should return false and navigate to / when user does not have the required role', () => {
    mockAuthService.getUserRole.and.returnValue(['PATIENT']);

    const route = { data: { roles: ['ADMIN'] } } as any;
    const result = executeGuard(route, { url: '/admin' } as any);

    expect(result).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should return false when user role is empty', () => {
    mockAuthService.getUserRole.and.returnValue([]);

    const route = { data: { roles: ['ADMIN'] } } as any;
    const result = executeGuard(route, { url: '/admin' } as any);

    expect(result).toBeFalse();
  });
});
