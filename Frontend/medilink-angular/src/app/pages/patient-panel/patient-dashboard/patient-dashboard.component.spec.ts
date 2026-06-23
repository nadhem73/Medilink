import { AuthService } from '../../../core/services/auth.service';
import { PatientDashboardComponent } from './patient-dashboard.component';
import { setupDashboardTest, testDashboardCreation, testSummaryCards, testQuickActions } from '../../../core/testing/dashboard-test-helper';

describe('PatientDashboardComponent', () => {
  let component: PatientDashboardComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const ctx = await setupDashboardTest(PatientDashboardComponent, { firstName: 'Ahmed', lastName: 'Ben Ali' });
    component = ctx.component;
    authServiceSpy = ctx.authServiceSpy;
  });

  testDashboardCreation(() => component, () => authServiceSpy, 'Ahmed');

  testSummaryCards(() => component, [
    'Prochains rendez-vous',
    'Ordonnances actives',
    'Analyses en attente',
    'Messages medicaux'
  ]);

  testQuickActions(() => component);

  it('should have upcoming appointments defined', () => {
    expect(component.upcomingAppointments.length).toBe(3);
  });

  it('should have medication reminders defined', () => {
    expect(component.medicationReminders.length).toBe(3);
  });

  it('should have care timeline defined', () => {
    expect(component.careTimeline.length).toBe(3);
  });

  it('should have care team defined', () => {
    expect(component.careTeam.length).toBe(3);
  });
});
