import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LaboratoryDashboardComponent } from './laboratory-dashboard.component';

describe('LaboratoryDashboardComponent', () => {
  let component: LaboratoryDashboardComponent;
  let fixture: ComponentFixture<LaboratoryDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LaboratoryDashboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LaboratoryDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
