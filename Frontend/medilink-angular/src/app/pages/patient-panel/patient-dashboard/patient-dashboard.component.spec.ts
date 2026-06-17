import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { PatientDashboardComponent } from './patient-dashboard.component';
import { AuthService } from '../../../core/services/auth.service';

describe('PatientDashboardComponent', () => {
  let component: PatientDashboardComponent;
  let fixture: ComponentFixture<PatientDashboardComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    authSpy.getCurrentUser.and.returnValue({ firstName: 'Ahmed', lastName: 'Ben Ali' });

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [PatientDashboardComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    fixture = TestBed.createComponent(PatientDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get current user from AuthService', () => {
    expect(authServiceSpy.getCurrentUser).toHaveBeenCalled();
    expect(component.currentUser?.firstName).toBe('Ahmed');
  });

  it('should have summary cards defined', () => {
    expect(component.summaryCards.length).toBe(4);
    expect(component.summaryCards[0].title).toBe('Prochains rendez-vous');
    expect(component.summaryCards[1].title).toBe('Ordonnances actives');
    expect(component.summaryCards[2].title).toBe('Analyses en attente');
    expect(component.summaryCards[3].title).toBe('Messages medicaux');
  });

  it('should have quick actions defined', () => {
    expect(component.quickActions.length).toBe(3);
  });

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
