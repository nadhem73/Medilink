import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { PharmacySectionComponent } from './pharmacy-section.component';

describe('PharmacySectionComponent', () => {
  let component: PharmacySectionComponent;
  let fixture: ComponentFixture<PharmacySectionComponent>;

  const activatedRouteStub = {
    data: of({ section: 'prescriptions', title: 'Ordonnances' })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PharmacySectionComponent],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteStub }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PharmacySectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render different pharmacy sections', () => {
    expect(component.section).toBe('prescriptions');
    expect(component.title).toBe('Ordonnances');
  });
});
