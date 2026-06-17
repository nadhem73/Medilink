import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DoctorSectionComponent } from './doctor-section.component';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DoctorSectionComponent', () => {
  let component: DoctorSectionComponent;
  let fixture: ComponentFixture<DoctorSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DoctorSectionComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: ActivatedRoute, useValue: { data: of({ section: 'patients', title: 'Patients' }) } },
        { provide: AuthService, useValue: { getCurrentUser: () => ({}) } },
        { provide: AppointmentService, useValue: { getDoctorAppointments: () => of([]), getActiveDoctorIds: () => of([]) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
