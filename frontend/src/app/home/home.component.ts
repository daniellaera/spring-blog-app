import { Component } from '@angular/core';
import { LoginService } from '../login.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { startAuthentication } from '@simplewebauthn/browser';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ButtonModule,
    CardModule,
    InputTextModule,
    ToastModule,
    DialogModule,
  ],
  providers: [MessageService],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  username = '';
  loading = false;
  scenarioADialogVisible = false;
  scenarioBDialogVisible = false;

  constructor(
    private loginService: LoginService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
  ) {}

  signInWithPasskey(): void {
    if (!this.username.trim()) {
      return;
    }
    this.loading = true;
    this.loginService.login(this.username).subscribe({
      next: result => {
        startAuthentication({ optionsJSON: result })
          .then(response => {
            this.loginService.verify(this.username, response).subscribe({
              next: result => {
                const r = result as any;
                if (r?.verified === false || r?.verified === 'false') {
                  this.loading = false;
                } else {
                  const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
                  this.router.navigateByUrl(decodeURIComponent(returnUrl));
                }
              },
              error: (err: any) => {
                this.loading = false;
                const code = err?.error?.code;
                if (code === 'PASSKEY_NOT_FOUND_IN_DB' || code === 'PASSKEY_SIGNATURE_INVALID') {
                  this.scenarioADialogVisible = true;
                } else if (code === 'PASSKEY_NOT_IN_DB' || code === 'PASSKEY_UNKNOWN') {
                  this.scenarioBDialogVisible = true;
                }
              }
            });
          })
          .catch((err: any) => {
            this.loading = false;
            if (err?.name === 'NotAllowedError' || err?.name === 'AbortError') {
              this.messageService.add({
                severity: 'info',
                summary: 'Cancelled',
                detail: 'Passkey sign-in was cancelled. Try again when ready.',
                life: 4000
              });
            }
          });
      },
      error: () => { this.loading = false; }
    });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
