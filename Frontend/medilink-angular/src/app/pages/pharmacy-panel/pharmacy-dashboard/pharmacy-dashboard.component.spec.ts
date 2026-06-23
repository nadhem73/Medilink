import { AuthService } from '../../../core/services/auth.service';
import { PharmacyDashboardComponent } from './pharmacy-dashboard.component';
import { setupDashboardTest, testDashboardCreation, testSummaryCards, testQuickActions } from '../../../core/testing/dashboard-test-helper';

describe('PharmacyDashboardComponent', () => {
  let component: PharmacyDashboardComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const ctx = await setupDashboardTest(PharmacyDashboardComponent, { firstName: 'Officine', lastName: 'Centrale' });
    component = ctx.component;
    authServiceSpy = ctx.authServiceSpy;
  });

  testDashboardCreation(() => component, () => authServiceSpy, 'Officine');

  testSummaryCards(() => component, [
    'Ordonnances recues',
    'Medicaments en stock',
    'Alertes de stock',
    'Commandes en cours'
  ]);

  testQuickActions(() => component);

  it('should have pending prescriptions defined', () => {
    expect(component.pendingPrescriptions.length).toBe(3);
  });

  it('should have stock alerts defined', () => {
    expect(component.stockAlerts.length).toBe(3);
  });

  it('should have activity timeline defined', () => {
    expect(component.activityTimeline.length).toBe(3);
  });

  it('should have suppliers defined', () => {
    expect(component.suppliers.length).toBe(3);
  });
});
