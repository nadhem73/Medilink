import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    spyOn(console, 'error');

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting()
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should pass through successful responses', () => {
    TestBed.inject(HttpClient).get('/api/data').subscribe(response => {
      expect(response).toEqual({ success: true });
    });

    const req = httpTestingController.expectOne('/api/data');
    req.flush({ success: true });

    expect(console.error).not.toHaveBeenCalled();
  });

  it('should log Access denied for 403 errors and re-throw', () => {
    const http = TestBed.inject(HttpClient);
    let capturedError: any;
    http.get('/api/data').subscribe({
      error: (err) => { capturedError = err; }
    });

    const req = httpTestingController.expectOne('/api/data');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(capturedError.status).toBe(403);
    expect(console.error).toHaveBeenCalledWith('Access denied');
  });

  it('should log Server error for 500 errors and re-throw', () => {
    const http = TestBed.inject(HttpClient);
    let capturedError: any;
    http.get('/api/data').subscribe({
      error: (err) => { capturedError = err; }
    });

    const req = httpTestingController.expectOne('/api/data');
    req.flush('Server Error', { status: 500, statusText: 'Server Error' });

    expect(capturedError.status).toBe(500);
    expect(console.error).toHaveBeenCalledWith('Server error:', jasmine.any(Object));
  });
});
