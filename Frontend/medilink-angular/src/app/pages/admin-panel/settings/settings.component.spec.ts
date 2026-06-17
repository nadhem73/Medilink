import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { SettingsComponent } from './settings.component';
import { AuthService } from '../../../core/services/auth.service';

describe('AdminSettingsComponent', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    authServiceSpy.getCurrentUser.and.returnValue({ firstName: 'Admin', lastName: 'User', email: 'admin@test.com' });

    await TestBed.configureTestingModule({
      declarations: [SettingsComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
