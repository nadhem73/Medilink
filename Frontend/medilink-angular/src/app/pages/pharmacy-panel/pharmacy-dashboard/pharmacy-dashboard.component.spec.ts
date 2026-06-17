import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { PharmacyDashboardComponent } from './pharmacy-dashboard.component';
import { AuthService } from '../../../core/services/auth.service';

describe('PharmacyDashboardComponent', () => {
  let component: PharmacyDashboardComponent;
  let fixture: ComponentFixture<PharmacyDashboardComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    authSpy.getCurrentUser.and.returnValue({ firstName: 'Officine', lastName: 'Centrale' });

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [PharmacyDashboardComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    fixture = TestBed.createComponent(PharmacyDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get current user from AuthService', () => {
    expect(authServiceSpy.getCurrentUser).toHaveBeenCalled();
    expect(component.currentUser?.firstName).toBe('Officine');
  });

  it('should have summary cards defined', () => {
    expect(component.summaryCards.length).toBe(4);
    expect(component.summaryCards[0].title).toBe('Ordonnances recues');
    expect(component.summaryCards[1].title).toBe('Medicaments en stock');
    expect(component.summaryCards[2].title).toBe('Alertes de stock');
    expect(component.summaryCards[3].title).toBe('Commandes en cours');
  });

  it('should have quick actions defined', () => {
    expect(component.quickActions.length).toBe(3);
  });

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
