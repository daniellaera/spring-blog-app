import { Component } from '@angular/core';
import { LoginService } from '../../../core/services/login.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { startAuthentication } from '@simplewebauthn/browser';
import { firstValueFrom } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    ToastModule,
  ],
  providers: [MessageService],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  username = '';
  usernameError = '';
  loading = false;

  constructor(
    private loginService: LoginService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
  ) {}

  async signInWithPasskey(): Promise<void> {
    if (!this.username.trim()) {
      this.usernameError = 'Username is required';
      return;
    }
    this.usernameError = '';
    this.loading = true;

    try {
      const optionsJSON = await firstValueFrom(this.loginService.startLogin(this.username));
      const response = await startAuthentication({ optionsJSON });
      const res: any = await firstValueFrom(this.loginService.verify(this.username, response));
      this.loginService.login(res?.token ?? '', this.username);
      const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
      this.router.navigateByUrl(decodeURIComponent(returnUrl));
    } catch (err: any) {
      if (err?.name === 'NotAllowedError' || err?.name === 'AbortError') {
        this.messageService.add({
          severity: 'info',
          summary: 'Cancelled',
          detail: 'Sign in was cancelled.',
          life: 4000
        });
      } else {
        this.messageService.add({
          severity: 'error',
          summary: 'Sign in failed',
          detail: err?.error?.message || err?.message || 'Authentication failed. Please try again.',
          life: 5000
        });
      }
    } finally {
      this.loading = false;
    }
  }
}
