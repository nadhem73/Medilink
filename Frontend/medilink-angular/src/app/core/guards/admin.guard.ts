import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Doit etre authentifie
  if (!authService.isAuthenticated()) {
    router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // Doit posseder le role administrateur (tolere ROLE_ADMIN / ADMIN)
  const roles = authService.getUserRole() || [];
  const isAdmin = roles
    .map(r => String(r).toUpperCase())
    .some(r => r === 'ADMIN' || r === 'ROLE_ADMIN');

  if (isAdmin) {
    return true;
  }

  // Acces refuse : retour a l'accueil
  router.navigate(['/']);
  return false;
};
