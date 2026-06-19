import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FooterComponent } from './footer.component';

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [FooterComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render footer element', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.footer')).toBeTruthy();
  });

  it('should display brand name MediLink Tunisia', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('MediLink Tunisia');
  });

  it('should render quick links', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const links = compiled.querySelectorAll('.footer-section ul li a');
    expect(links.length).toBe(3);
    expect(links[0].textContent).toContain('Accueil');
    expect(links[1].textContent).toContain('Connexion');
    expect(links[2].textContent).toContain('Inscription');
  });

  it('should display contact email', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('contact');
    expect(compiled.textContent).toContain('medilink.tn');
  });

  it('should display copyright', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('MediLink Tunisia');
    expect(compiled.textContent).toContain('Tous droits réservés');
  });
});
