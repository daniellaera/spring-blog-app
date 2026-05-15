import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { MessageService } from 'primeng/api';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const messageService = inject(MessageService);
  const silent = req.url.includes('/session/me');

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!silent) {
        let message: string;
        if (error.error && typeof error.error === 'object' && error.error['error']) {
          message = error.error['error'];
        } else {
          switch (error.status) {
            case 401: message = 'Please sign in to continue'; break;
            case 403: message = "You don't have permission to do this"; break;
            case 404: message = 'Not found'; break;
            case 409: message = 'Already exists'; break;
            case 500: message = 'Server error — please try again'; break;
            default:  message = 'An unexpected error occurred';
          }
        }
        messageService.add({ severity: 'error', summary: 'Error', detail: message, life: 4000 });
      }
      return throwError(() => error);
    })
  );
};
