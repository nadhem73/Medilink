import { HttpInterceptorFn } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      if (error.status === 403) {
        console.error('Access denied');
      } else if (error.status === 500) {
        console.error('Server error:', error);
      }

      return throwError(() => error);
    })
  );
};
