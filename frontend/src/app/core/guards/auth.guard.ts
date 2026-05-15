import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginService } from '../services/login.service';
import { catchError, map, of } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const loginService = inject(LoginService);

  return loginService.getSession().pipe(
    map(() => true),
    catchError(() => {
      return of(router.createUrlTree(['/login'], {
        queryParams: { returnUrl: encodeURIComponent(state.url) }
      }));
    })
  );
};
