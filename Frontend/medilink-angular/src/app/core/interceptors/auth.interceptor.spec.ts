import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import {
  HttpClient,
  HttpErrorResponse,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { StorageService } from '../services/storage.service';

describe('authInterceptor', () => {
  let httpTestingController: HttpTestingController;
  let mockStorageService: jasmine.SpyObj<StorageService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockStorageService = jasmine.createSpyObj('StorageService', [
      'getToken', 'getRefreshToken', 'removeToken', 'removeRefreshToken'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', [
      'logout', 'refreshToken'
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: StorageService, useValue: mockStorageService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should add Authorization: Bearer header for non-public URLs', () => {
    mockStorageService.getToken.and.returnValue('test-token');

    TestBed.inject(HttpClient).get('/api/data').subscribe();

    const req = httpTestingController.expectOne('/api/data');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush({});
  });

  it('should not add Authorization header for public URLs', () => {
    mockStorageService.getToken.and.returnValue('test-token');
    const http = TestBed.inject(HttpClient);
    const publicUrls = [
      '/auth/login',
      '/auth/register',
      '/auth/forgot-password',
      '/auth/reset-password',
      '/auth/refresh'
    ];

    for (const url of publicUrls) {
      http.get(url).subscribe();
      const req = httpTestingController.expectOne(url);
      expect(req.request.headers.has('Authorization')).toBeFalse();
      req.flush({});
    }
  });

  it('should re-throw non-401 errors without attempting refresh', () => {
    mockStorageService.getToken.and.returnValue('test-token');

    const http = TestBed.inject(HttpClient);
    let capturedError: any;
    http.get('/api/data').subscribe({
      error: (err) => { capturedError = err; }
    });

    const req = httpTestingController.expectOne('/api/data');
    req.flush('Server Error', { status: 500, statusText: 'Server Error' });

    expect(capturedError).toBeInstanceOf(HttpErrorResponse);
    expect(capturedError.status).toBe(500);
    expect(mockAuthService.refreshToken).not.toHaveBeenCalled();
  });

  it('should not intercept 401 on the refresh endpoint itself', () => {
    mockStorageService.getToken.and.returnValue('test-token');

    const http = TestBed.inject(HttpClient);
    let capturedError: any;
    http.post('/auth/refresh', {}).subscribe({
      error: (err) => { capturedError = err; }
    });

    const req = httpTestingController.expectOne('/auth/refresh');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(capturedError.status).toBe(401);
    expect(mockAuthService.refreshToken).not.toHaveBeenCalled();
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });

  it('should log out and redirect to login on 401 when no refresh token', () => {
    mockStorageService.getToken.and.returnValue('test-token');
    mockStorageService.getRefreshToken.and.returnValue(null);

    const http = TestBed.inject(HttpClient);
    let capturedError: any;
    http.get('/api/data').subscribe({
      error: (err) => { capturedError = err; }
    });

    const req = httpTestingController.expectOne('/api/data');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(capturedError.status).toBe(401);
    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('should attempt token refresh on 401 and retry the request with new token', () => {
    mockStorageService.getToken.and.returnValue('old-token');
    mockStorageService.getRefreshToken.and.returnValue('refresh-token');
    mockAuthService.refreshToken.and.returnValue(of({ accessToken: 'new-token' } as any));

    const http = TestBed.inject(HttpClient);
    http.get('/api/data').subscribe();

    const req1 = httpTestingController.expectOne('/api/data');
    expect(req1.request.headers.get('Authorization')).toBe('Bearer old-token');
    req1.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(mockAuthService.refreshToken).toHaveBeenCalledTimes(1);

    const req2 = httpTestingController.expectOne('/api/data');
    expect(req2.request.headers.get('Authorization')).toBe('Bearer new-token');
    req2.flush({});
  });

  it('should log out and redirect to login when token refresh fails', () => {
    mockStorageService.getToken.and.returnValue('test-token');
    mockStorageService.getRefreshToken.and.returnValue('refresh-token');
    mockAuthService.refreshToken.and.returnValue(
      throwError(() => new Error('Refresh failed'))
    );

    const http = TestBed.inject(HttpClient);
    let capturedError: any;
    http.get('/api/data').subscribe({
      error: (err) => { capturedError = err; }
    });

    const req = httpTestingController.expectOne('/api/data');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
    expect(capturedError).toBeDefined();
  });

  it('should queue concurrent requests during token refresh', fakeAsync(() => {
    mockStorageService.getToken.and.returnValue('old-token');
    mockStorageService.getRefreshToken.and.returnValue('refresh-token');

    let resolveRefresh!: (value: any) => void;
    mockAuthService.refreshToken.and.returnValue(
      new Observable(subscriber => {
        resolveRefresh = (value: any) => {
          subscriber.next(value);
          subscriber.complete();
        };
      })
    );

    const http = TestBed.inject(HttpClient);
    http.get('/api/data1').subscribe();
    http.get('/api/data2').subscribe();

    const req1 = httpTestingController.expectOne('/api/data1');
    req1.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    const req2 = httpTestingController.expectOne('/api/data2');
    req2.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(mockAuthService.refreshToken).toHaveBeenCalledTimes(1);

    resolveRefresh({ accessToken: 'new-token' });
    tick();

    const retry1 = httpTestingController.expectOne('/api/data1');
    expect(retry1.request.headers.get('Authorization')).toBe('Bearer new-token');
    retry1.flush({});

    const retry2 = httpTestingController.expectOne('/api/data2');
    expect(retry2.request.headers.get('Authorization')).toBe('Bearer new-token');
    retry2.flush({});
  }));
});
