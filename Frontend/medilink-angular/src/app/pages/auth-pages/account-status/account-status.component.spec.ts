import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { AccountStatusComponent } from './account-status.component';

describe('AccountStatusComponent', () => {
  let component: AccountStatusComponent;
  let fixture: ComponentFixture<AccountStatusComponent>;
  let routerSpy: jasmine.SpyObj<Router>;

  // ActivatedRoute mutable : on ajuste queryParams avant de créer le composant.
  const route = { snapshot: { queryParams: {} as any } };

  function build(params: any): void {
    route.snapshot.queryParams = params;
    fixture = TestBed.createComponent(AccountStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // ngOnInit
  }

  beforeEach(async () => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [AccountStatusComponent],
      imports: [CommonModule],
      providers: [
        { provide: ActivatedRoute, useValue: route },
        { provide: Router, useValue: routerSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  afterEach(() => {
    // Évite les timers qui fuient entre les tests.
    component?.ngOnDestroy();
  });

  it('should default to SUSPENDED / patient theme when params are missing', () => {
    build({});
    expect(component.status).toBe('SUSPENDED');
    expect(component.role).toBe('patient');
    expect(component.theme.primary).toBe('#0066a2');
  });

  it('should apply doctor theme (green) from role param', () => {
    build({ status: 'SUSPENDED', role: 'doctor', until: new Date(Date.now() + 3600000).toISOString() });
    expect(component.role).toBe('doctor');
    expect(component.theme.primary).toBe('#2e8b57');
  });

  it('should apply pharmacy theme (purple) from role param', () => {
    build({ status: 'INACTIVE', role: 'pharmacy' });
    expect(component.theme.primary).toBe('#6d28d9');
  });

  it('should parse INACTIVE status without countdown', () => {
    build({ status: 'INACTIVE', role: 'patient' });
    expect(component.status).toBe('INACTIVE');
    expect(component.expired).toBeFalse();
  });

  it('should compute countdown for a future suspension', () => {
    const until = new Date(Date.now() + (2 * 86400000 + 3 * 3600000)); // 2j 3h
    build({ status: 'SUSPENDED', role: 'doctor', until: until.toISOString() });

    expect(component.expired).toBeFalse();
    expect(component.days).toBe(2);
    expect(component.hours).toBeGreaterThanOrEqual(2);
  });

  it('should mark as expired when suspendUntil is in the past', () => {
    build({ status: 'SUSPENDED', role: 'doctor', until: new Date(Date.now() - 1000).toISOString() });
    expect(component.expired).toBeTrue();
    expect(component.days).toBe(0);
  });

  it('pad should zero-pad single digits', () => {
    build({});
    expect(component.pad(5)).toBe('05');
    expect(component.pad(12)).toBe('12');
  });

  it('goToLogin should navigate to the login page', () => {
    build({});
    component.goToLogin();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
