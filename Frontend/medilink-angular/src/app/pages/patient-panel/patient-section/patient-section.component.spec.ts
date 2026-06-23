import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PatientSectionComponent } from './patient-section.component';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('PatientSectionComponent', () => {
  let component: PatientSectionComponent;
  let fixture: ComponentFixture<PatientSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PatientSectionComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: ActivatedRoute, useValue: { data: of({ section: 'appointments', title: 'Appointments' }) } },
        { provide: AuthService, useValue: { getCurrentUser: () => ({}) } },
        { provide: DoctorService, useValue: { getAllDoctors: () => of([]), getDoctorsWithProfiles: () => of([]) } },
        { provide: AppointmentService, useValue: { getMyAppointments: () => of([]), createAppointment: () => of({}), cancelAppointment: () => of({}), getActiveDoctorIds: () => of([]) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PatientSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
