import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { DoctorDashboardComponent } from './doctor-dashboard.component';
import { AuthService } from '../../../core/services/auth.service';

describe('DoctorDashboardComponent', () => {
  let component: DoctorDashboardComponent;
  let fixture: ComponentFixture<DoctorDashboardComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    authSpy.getCurrentUser.and.returnValue({ firstName: 'Sami', lastName: 'Ben Ahmed' });

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [DoctorDashboardComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    fixture = TestBed.createComponent(DoctorDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get current user from AuthService', () => {
    expect(authServiceSpy.getCurrentUser).toHaveBeenCalled();
    expect(component.currentUser?.lastName).toBe('Ben Ahmed');
  });

  it('should have summary cards defined', () => {
    expect(component.summaryCards.length).toBe(4);
    expect(component.summaryCards[0].title).toBe('Patients du jour');
    expect(component.summaryCards[1].title).toBe('Consultations a venir');
    expect(component.summaryCards[2].title).toBe('Ordonnances a signer');
    expect(component.summaryCards[3].title).toBe('Messages patients');
  });

  it('should have quick actions defined', () => {
    expect(component.quickActions.length).toBe(3);
  });

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
