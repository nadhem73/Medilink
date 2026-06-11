import { HttpInterceptorFn } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        // Token invalide ou expiré
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        // Accès refusé
        console.error('Access denied');
      } else if (error.status === 500) {
        // Erreur serveur
        console.error('Server error:', error);
      }

      return throwError(() => error);
    })
  );
};
