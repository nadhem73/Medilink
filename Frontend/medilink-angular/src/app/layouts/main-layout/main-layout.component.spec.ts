import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { MainLayoutComponent } from './main-layout.component';

@Component({ selector: 'app-navbar', template: '' })
class MockNavbarComponent {}

@Component({ selector: 'app-footer', template: '' })
class MockFooterComponent {}

describe('MainLayoutComponent', () => {
  let component: MainLayoutComponent;
  let fixture: ComponentFixture<MainLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [MainLayoutComponent, MockNavbarComponent, MockFooterComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MainLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render navbar component', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-navbar')).toBeTruthy();
  });

  it('should render footer component', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-footer')).toBeTruthy();
  });

  it('should have a main content area with router outlet', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const main = compiled.querySelector('main.content');
    expect(main).toBeTruthy();
    expect(main?.querySelector('router-outlet')).toBeTruthy();
  });
});
