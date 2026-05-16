import { Component } from '@angular/core';
import { RegisterService } from '../../../core/services/register.service';
import { Router, RouterLink } from '@angular/router';
import { startRegistration, PublicKeyCredentialCreationOptionsJSON } from '@simplewebauthn/browser';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    ToastModule,
  ],
  providers: [MessageService],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  step = 1;
  username = '';
  usernameTouched = false;
  loading = false;

  constructor(
    private registerService: RegisterService,
    private router: Router,
    private messageService: MessageService,
  ) {}

  createAccount(): void {
    if (!this.username.trim()) {
      this.usernameTouched = true;
      return;
    }
    this.step = 2;
  }

  registerPasskey(): void {
    this.loading = true;
    this.registerService.register(this.username).subscribe({
      next: options => {
        startRegistration({ optionsJSON: options as PublicKeyCredentialCreationOptionsJSON })
          .then(response => {
            this.registerService.verify(this.username, response).subscribe({
              next: result => {
                const r = result as any;
                if (r?.verified === true || r?.verified === 'true') {
                  this.messageService.add({
                    severity: 'success',
                    summary: 'Account created',
                    detail: 'You can now sign in with your passkey.',
                    life: 3000
                  });
                  setTimeout(() => this.router.navigate(['/login']), 2000);
                } else {
                  this.loading = false;
                }
              },
              error: () => { this.loading = false; }
            });
          })
          .catch((err: any) => {
            this.loading = false;
            if (err?.name === 'NotAllowedError' || err?.name === 'AbortError') {
              this.messageService.add({
                severity: 'info',
                summary: 'Cancelled',
                detail: 'Passkey registration was cancelled. Try again when ready.',
                life: 4000
              });
            }
          });
      },
      error: () => { this.loading = false; }
    });
  }
}
