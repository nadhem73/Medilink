import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { SettingsComponent } from './settings.component';
import { AuthService } from '../../../core/services/auth.service';

describe('SettingsComponent', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockUser = {
    firstName: 'Yasmine',
    lastName: 'Ben Salem',
    email: 'yasmine@test.com',
    phone: '20111111',
    specialty: 'Cardiologie',
    licenseNumber: '12345',
    facility: 'Clinique El Manar'
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    authServiceSpy.getCurrentUser.and.returnValue(mockUser);

    await TestBed.configureTestingModule({
      declarations: [SettingsComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
