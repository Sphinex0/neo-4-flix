import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      const isAuthEndpoint = req.url.includes('/api/auth/');

      if (error.status === 401 && !isAuthEndpoint) {
        authService.clearToken();
        router.navigate(['/login']);
      }

      return throwError(() => error);
    }),
  );
};
