import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { MedicalHologramComponent } from './medical-hologram.component';

describe('MedicalHologramComponent', () => {
  let component: MedicalHologramComponent;
  let fixture: ComponentFixture<MedicalHologramComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedicalHologramComponent],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicalHologramComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display hologram', () => {
    const hostEl: HTMLDivElement = fixture.nativeElement.querySelector('.holo-gl');
    expect(hostEl).toBeTruthy();
  });
});
