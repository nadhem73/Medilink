import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { StorageService } from './storage.service';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: string;
}

export interface MessageResponse {
  message: string;
  success: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    phone: string;
    address: string;
    birthDate: string;
    gender: string;
    status: string;
    isEmailVerified: boolean;
    roles: string[];
    createdAt: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8765/api/auth';
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private storage: StorageService
  ) {
    this.loadCurrentUser();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials)
      .pipe(
        tap(response => this.handleAuthResponse(response))
      );
  }

  register(data: RegisterRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.API_URL}/register`, data);
  }

  logout(): void {
    this.storage.removeToken();
    this.storage.removeRefreshToken();
    this.storage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!this.storage.getToken();
  }

  getToken(): string | null {
    return this.storage.getToken();
  }

  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  getUserRole(): string[] {
    const user = this.getCurrentUser();
    return user ? user.roles : [];
  }

  hasRole(role: string): boolean {
    const roles = this.getUserRole();
    return roles.includes(role);
  }

  private handleAuthResponse(response: AuthResponse): void {
    this.storage.setToken(response.accessToken);
    this.storage.setRefreshToken(response.refreshToken);
    this.storage.setItem('user', JSON.stringify(response.user));
    this.currentUserSubject.next(response.user);
  }

  private loadCurrentUser(): void {
    const userStr = this.storage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        this.currentUserSubject.next(user);
      } catch (e) {
        console.error('Error loading user from storage', e);
      }
    }
  }
}
