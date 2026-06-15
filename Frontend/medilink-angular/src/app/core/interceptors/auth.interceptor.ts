import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { StorageService } from '../services/storage.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(StorageService);
  const token = storage.getToken();

  const publicUrls = ['/auth/login', '/auth/register', '/auth/forgot-password', '/auth/reset-password'];
  const isPublic = publicUrls.some(url => req.url.includes(url));

  if (token && !isPublic) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedRequest);
  }

  return next(req);
};
