import { Component } from '@angular/core';
import { LoginService } from '../../../core/services/login.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { startAuthentication } from '@simplewebauthn/browser';
import { firstValueFrom } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { MessageModule } from 'primeng/message';
import { MessageService } from 'primeng/api';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    ToastModule,
    DialogModule,
    MessageModule,
  ],
  providers: [MessageService],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  username = '';
  usernameTouched = false;
  loading = false;

  recoveryStep = 0;
  recoveryUsername = '';
  recoveryEmail = '';
  recoveryCode = '';
  recoveryLoading = false;
  devOtp: string | null = null;

  get recoveryVisible(): boolean { return this.recoveryStep > 0; }
  set recoveryVisible(v: boolean) { if (!v) this.recoveryStep = 0; }

  constructor(
    private loginService: LoginService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    private http: HttpClient,
  ) {}

  async signInWithPasskey(): Promise<void> {
    if (!this.username.trim()) {
      this.usernameTouched = true;
      return;
    }
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
        this.openRecovery();
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

  openRecovery(): void {
    this.recoveryUsername = this.username;
    this.recoveryStep = 1;
  }

  async sendRecoveryCode(): Promise<void> {
    this.recoveryLoading = true;
    try {
      const res = await firstValueFrom(
        this.http.post<{ message: string; devOtp: string | null }>(
          `${environment.apiUrl}/recovery/start`,
          { username: this.recoveryUsername, email: this.recoveryEmail }
        )
      );
      this.devOtp = res.devOtp;
      this.recoveryStep = 2;
      this.messageService.add({
        severity: 'info',
        summary: 'Code sent',
        detail: res.message,
        life: 5000
      });
    } catch (err: any) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: err?.error?.error || 'Failed to send recovery code.',
        life: 5000
      });
    } finally {
      this.recoveryLoading = false;
    }
  }

  async verifyAndReregister(): Promise<void> {
    this.recoveryLoading = true;
    try {
      const { startRegistration } = await import('@simplewebauthn/browser');

      const options = await firstValueFrom(
        this.http.post<any>(
          `${environment.apiUrl}/recovery/verify`,
          { username: this.recoveryUsername, code: this.recoveryCode }
        )
      );

      const regResponse = await startRegistration({ optionsJSON: options });

      await firstValueFrom(
        this.http.post(
          `${environment.apiUrl}/register/verify`,
          { username: this.recoveryUsername, response: regResponse },
          { withCredentials: true }
        )
      );

      this.recoveryStep = 3;
      this.messageService.add({
        severity: 'success',
        summary: 'Passkey registered',
        detail: 'You can now sign in with your new passkey.',
        life: 4000
      });
    } catch (err: any) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: err?.error?.error || 'Verification failed.',
        life: 5000
      });
    } finally {
      this.recoveryLoading = false;
    }
  }
}
