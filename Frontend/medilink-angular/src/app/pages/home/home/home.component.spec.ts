import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HomeComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default selectedCategory as teleconsultation', () => {
    expect(component.selectedCategory).toBe('teleconsultation');
  });

  it('should have services defined', () => {
    expect(component.services.length).toBeGreaterThan(0);
  });

  it('should have doctors defined', () => {
    expect(component.doctors.length).toBeGreaterThan(0);
  });

  it('should filter services by category', () => {
    component.selectCategory('pharmacie');
    expect(component.selectedCategory).toBe('pharmacie');
    expect(component.filteredServices.every(s => s.type === 'pharmacie')).toBeTrue();
  });

  it('should filter services on init', () => {
    expect(component.filteredServices.length).toBeGreaterThan(0);
    expect(component.filteredServices.every(s => s.type === 'teleconsultation')).toBeTrue();
  });

  it('should select a different category', () => {
    component.selectCategory('laboratoire');
    expect(component.selectedCategory).toBe('laboratoire');
    expect(component.filteredServices.length).toBeGreaterThan(0);
  });

  it('should have testimonials defined', () => {
    expect(component.testimonials.length).toBeGreaterThan(0);
  });
});
