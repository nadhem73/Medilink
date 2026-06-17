import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { roleGuard } from './role.guard';

describe('roleGuard', () => {
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockRoute = { data: { roles: ['DOCTOR'] } } as any;
  const mockState = {} as any;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['getUserRole']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });
  });

  it('should allow access when user has expected role', () => {
    mockAuthService.getUserRole.and.returnValue(['DOCTOR']);
    const result = TestBed.runInInjectionContext(() => roleGuard(mockRoute, mockState));
    expect(result).toBeTrue();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should deny access when user does not have expected role', () => {
    mockAuthService.getUserRole.and.returnValue(['PATIENT']);
    const result = TestBed.runInInjectionContext(() => roleGuard(mockRoute, mockState));
    expect(result).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should deny access when user role is empty', () => {
    mockAuthService.getUserRole.and.returnValue([]);
    const result = TestBed.runInInjectionContext(() => roleGuard(mockRoute, mockState));
    expect(result).toBeFalse();
  });
});
