import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { StorageService } from '../services/storage.service';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(StorageService);
  const authService = inject(AuthService);
  const router = inject(Router);

  const publicUrls = ['/auth/login', '/auth/register', '/auth/forgot-password', '/auth/reset-password', '/auth/refresh'];
  const isPublic = publicUrls.some(url => req.url.includes(url));

  if (isPublic) {
    return next(req);
  }

  const token = storage.getToken();
  if (token) {
    req = addToken(req, token);
  }

  return next(req).pipe(
    catchError(error => {
      if (error.status !== 401 || req.url.includes('/auth/refresh')) {
        return throwError(() => error);
      }

      const refreshToken = storage.getRefreshToken();
      if (!refreshToken) {
        authService.logout();
        router.navigate(['/auth/login']);
        return throwError(() => error);
      }

      if (!isRefreshing) {
        isRefreshing = true;
        refreshTokenSubject.next(null);

        return authService.refreshToken().pipe(
          switchMap(response => {
            isRefreshing = false;
            refreshTokenSubject.next(response.accessToken);
            return next(addToken(req, response.accessToken));
          }),
          catchError(refreshError => {
            isRefreshing = false;
            authService.logout();
            router.navigate(['/auth/login']);
            return throwError(() => refreshError);
          })
        );
      } else {
        return refreshTokenSubject.pipe(
          filter(token => token !== null),
          take(1),
          switchMap(newToken => next(addToken(req, newToken!)))
        );
      }
    })
  );
};

function addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}
