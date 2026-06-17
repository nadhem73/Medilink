import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { AdminSectionComponent } from './admin-section.component';

describe('AdminSectionComponent', () => {
  let component: AdminSectionComponent;
  let fixture: ComponentFixture<AdminSectionComponent>;

  const activatedRouteStub = {
    data: of({ section: 'approvals', title: 'Validations' })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminSectionComponent],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteStub }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render different admin sections', () => {
    expect(component.section).toBe('approvals');
    expect(component.title).toBe('Validations');
  });
});
