import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AmbulanceDashboardComponent } from './ambulance-dashboard.component';

describe('AmbulanceDashboardComponent', () => {
  let component: AmbulanceDashboardComponent;
  let fixture: ComponentFixture<AmbulanceDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AmbulanceDashboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AmbulanceDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
