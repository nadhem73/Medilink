import { TestBed } from '@angular/core/testing';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export interface GuardTestContext {
  mockAuthService: jasmine.SpyObj<AuthService>;
  mockRouter: jasmine.SpyObj<Router>;
  executeGuard: CanActivateFn;
  configureTestBed: () => void;
}

export function setupGuardTest(guardFn: CanActivateFn, spyMethods: string[]): GuardTestContext {
  const mockAuthService = jasmine.createSpyObj('AuthService', spyMethods);
  const mockRouter = jasmine.createSpyObj('Router', ['navigate']);

  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => guardFn(...guardParameters));

  function configureTestBed(): void {
    mockAuthService.isAuthenticated?.calls?.reset?.();
    mockAuthService.getUserRole?.calls?.reset?.();
    mockRouter.navigate.calls.reset();
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });
  }

  return { mockAuthService, mockRouter, executeGuard, configureTestBed };
}
