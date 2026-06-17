import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TeleconsultationComponent } from './teleconsultation.component';

describe('TeleconsultationComponent', () => {
  let component: TeleconsultationComponent;
  let fixture: ComponentFixture<TeleconsultationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TeleconsultationComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(TeleconsultationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
