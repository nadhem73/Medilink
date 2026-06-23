import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { AuthService, LoginRequest, RegisterRequest, ForgotPasswordRequest, ResetPasswordRequest, AuthResponse, MessageResponse, PatientListDto } from './auth.service';
import { StorageService } from './storage.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let mockStorageService: jasmine.SpyObj<StorageService>;

  beforeEach(() => {
    mockStorageService = jasmine.createSpyObj('StorageService', [
      'setToken', 'getToken', 'removeToken',
      'setRefreshToken', 'getRefreshToken', 'removeRefreshToken',
      'setItem', 'getItem', 'removeItem', 'clear'
    ]);
    mockStorageService.getItem.and.returnValue(null);
    mockStorageService.getToken.and.returnValue(null);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: StorageService, useValue: mockStorageService }
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login()', () => {
    it('should POST to /api/auth/login and store tokens via handleAuthResponse', () => {
      const credentials: LoginRequest = { email: 'test@test.com', password: 'password' };
      const authResponse: AuthResponse = {
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
        user: {
          id: 1, email: 'test@test.com', firstName: 'Test', lastName: 'User',
          phone: '123456', address: 'Addr', birthDate: '2000-01-01', gender: 'M',
          status: 'ACTIVE', isEmailVerified: true, roles: ['PATIENT'], createdAt: '2024-01-01'
        }
      };

      service.login(credentials).subscribe(res => {
        expect(res).toEqual(authResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(credentials);
      req.flush(authResponse);

      expect(mockStorageService.setToken).toHaveBeenCalledWith('access-token');
      expect(mockStorageService.setRefreshToken).toHaveBeenCalledWith('refresh-token');
      expect(mockStorageService.setItem).toHaveBeenCalledWith('user', jasmine.any(String));
    });
  });

  describe('register()', () => {
    it('should POST to /api/auth/register', () => {
      const data: RegisterRequest = {
        email: 'new@test.com', password: 'password', firstName: 'New', lastName: 'User',
        role: 'PATIENT', phone: '123456'
      };
      const mockResponse: MessageResponse = { message: 'User registered', success: true };

      service.register(data).subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/auth/register');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(data);
      req.flush(mockResponse);
    });
  });

  describe('forgotPassword()', () => {
    it('should POST to /api/auth/forgot-password', () => {
      const data: ForgotPasswordRequest = { role: 'patient', email: 'test@test.com' };
      const mockResponse: MessageResponse = { message: 'Email sent', success: true };

      service.forgotPassword(data).subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/auth/forgot-password');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(data);
      req.flush(mockResponse);
    });
  });

  describe('resetPassword()', () => {
    it('should POST to /api/auth/reset-password', () => {
      const data: ResetPasswordRequest = { token: 'reset-token', newPassword: 'new-password' };
      const mockResponse: MessageResponse = { message: 'Password reset', success: true };

      service.resetPassword(data).subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/auth/reset-password');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(data);
      req.flush(mockResponse);
    });
  });

  describe('refreshToken()', () => {
    it('should POST to /api/auth/refresh with stored refresh token and update tokens', () => {
      mockStorageService.getRefreshToken.and.returnValue('stored-refresh-token');
      const authResponse: AuthResponse = {
        accessToken: 'new-access-token',
        refreshToken: 'new-refresh-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
        user: {
          id: 1, email: 'test@test.com', firstName: 'Test', lastName: 'User',
          phone: '', address: '', birthDate: '', gender: '', status: 'ACTIVE',
          isEmailVerified: true, roles: ['PATIENT'], createdAt: ''
        }
      };

      service.refreshToken().subscribe(res => {
        expect(res).toEqual(authResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/auth/refresh');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ refreshToken: 'stored-refresh-token' });
      req.flush(authResponse);

      expect(mockStorageService.setToken).toHaveBeenCalledWith('new-access-token');
      expect(mockStorageService.setRefreshToken).toHaveBeenCalledWith('new-refresh-token');
      expect(mockStorageService.setItem).toHaveBeenCalledWith('user', jasmine.any(String));
    });
  });

  describe('getAllPatients()', () => {
    it('should GET /api/auth/patients', () => {
      const patients: PatientListDto[] = [
        { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@test.com', phone: '123' },
        { id: 2, firstName: 'Jane', lastName: 'Doe', email: 'jane@test.com', phone: '456' }
      ];

      service.getAllPatients().subscribe(res => {
        expect(res).toEqual(patients);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/auth/patients');
      expect(req.request.method).toBe('GET');
      req.flush(patients);
    });
  });

  describe('logout()', () => {
    it('should clear tokens and user from storage and set currentUser to null', () => {
      service.logout();

      expect(mockStorageService.removeToken).toHaveBeenCalled();
      expect(mockStorageService.removeRefreshToken).toHaveBeenCalled();
      expect(mockStorageService.removeItem).toHaveBeenCalledWith('user');
      const user = service.getCurrentUser();
      expect(user).toBeNull();
    });
  });

  describe('isAuthenticated()', () => {
    it('should return true when token exists', () => {
      mockStorageService.getToken.and.returnValue('some-token');
      expect(service.isAuthenticated()).toBeTrue();
    });

    it('should return false when no token exists', () => {
      mockStorageService.getToken.and.returnValue(null);
      expect(service.isAuthenticated()).toBeFalse();
    });
  });

  describe('getCurrentUser()', () => {
    it('should return the current user from BehaviorSubject', () => {
      const user = { id: 1, email: 'test@test.com', firstName: 'Test', lastName: 'User' };
      mockStorageService.getItem.and.returnValue(JSON.stringify(user));

      const authResponse: AuthResponse = {
        accessToken: 'token', refreshToken: 'refresh', tokenType: 'Bearer',
        expiresIn: 3600,
        user: { ...user, phone: '', address: '', birthDate: '', gender: '', status: 'ACTIVE',
                isEmailVerified: true, roles: ['PATIENT'], createdAt: '' }
      };

      service.login({ email: 'test@test.com', password: 'pwd' }).subscribe();
      httpMock.expectOne('http://localhost:8765/api/auth/login').flush(authResponse);

      const currentUser = service.getCurrentUser();
      expect(currentUser).toEqual(authResponse.user);
    });

    it('should return null when no user is loaded', () => {
      mockStorageService.getItem.and.returnValue(null);
      const currentUser = service.getCurrentUser();
      expect(currentUser).toBeNull();
    });
  });

  describe('verifyEmailOtp()', () => {
    it('should POST to /api/auth/verify-email/verify and store user', () => {
      const code = '123456';
      const user = { id: 1, email: 'test@test.com', emailVerified: true };

      service.verifyEmailOtp(code).subscribe(res => {
        expect(res).toEqual(user);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/auth/verify-email/verify');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ code });
      req.flush(user);

      expect(mockStorageService.setItem).toHaveBeenCalledWith('user', JSON.stringify(user));
    });
  });
});
