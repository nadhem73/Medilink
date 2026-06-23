import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA, Type } from '@angular/core';
import { AuthService } from '../services/auth.service';

export interface DashboardTestContext<T> {
  component: T;
  fixture: ComponentFixture<T>;
  authServiceSpy: jasmine.SpyObj<AuthService>;
}

export async function setupDashboardTest<T>(
  componentType: Type<T>,
  mockUser: { firstName: string; lastName: string }
): Promise<DashboardTestContext<T>> {
  const authSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
  authSpy.getCurrentUser.and.returnValue(mockUser);

  await TestBed.configureTestingModule({
    imports: [RouterTestingModule],
    declarations: [componentType],
    schemas: [NO_ERRORS_SCHEMA],
    providers: [
      { provide: AuthService, useValue: authSpy }
    ]
  }).compileComponents();

  const authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  const fixture = TestBed.createComponent(componentType);
  const component = fixture.componentInstance;
  fixture.detectChanges();

  return { component, fixture, authServiceSpy };
}

export function testDashboardCreation<T extends { currentUser?: any }>(
  getComponent: () => T,
  getAuthSpy: () => jasmine.SpyObj<AuthService>,
  expectedFirstName: string
) {
  it('should create', () => {
    expect(getComponent()).toBeTruthy();
  });

  it('should get current user from AuthService', () => {
    expect(getAuthSpy().getCurrentUser).toHaveBeenCalled();
    expect(getComponent().currentUser?.firstName).toBe(expectedFirstName);
  });
}

export function testSummaryCards<T extends { summaryCards: { title: string }[] }>(
  getComponent: () => T,
  titles: string[]
) {
  it('should have summary cards defined', () => {
    const cards = getComponent().summaryCards;
    expect(cards.length).toBe(titles.length);
    titles.forEach((title, i) => {
      expect(cards[i].title).toBe(title);
    });
  });
}

export function testQuickActions<T extends { quickActions: any[] }>(
  getComponent: () => T,
  expectedCount: number = 3
) {
  it('should have quick actions defined', () => {
    expect(getComponent().quickActions.length).toBe(expectedCount);
  });
}
