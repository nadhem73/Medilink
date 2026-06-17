import { AuthService } from '../../../core/services/auth.service';
import { DoctorDashboardComponent } from './doctor-dashboard.component';
import { setupDashboardTest, testDashboardCreation, testSummaryCards, testQuickActions } from '../../../core/testing/dashboard-test-helper';

describe('DoctorDashboardComponent', () => {
  let component: DoctorDashboardComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const ctx = await setupDashboardTest(DoctorDashboardComponent, { firstName: 'Sami', lastName: 'Ben Ahmed' });
    component = ctx.component;
    authServiceSpy = ctx.authServiceSpy;
  });

  testDashboardCreation(() => component, () => authServiceSpy, 'Sami');

  testSummaryCards(() => component, [
    'Patients du jour',
    'Consultations a venir',
    'Ordonnances a signer',
    'Messages patients'
  ]);

  testQuickActions(() => component);

  it('should have today appointments defined', () => {
    expect(component.todayAppointments.length).toBe(3);
  });

  it('should have pending tasks defined', () => {
    expect(component.pendingTasks.length).toBe(3);
  });

  it('should have activity timeline defined', () => {
    expect(component.activityTimeline.length).toBe(3);
  });

  it('should have care team defined', () => {
    expect(component.careTeam.length).toBe(3);
  });
});
