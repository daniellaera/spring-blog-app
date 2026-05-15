import { Component } from '@angular/core';
import { RegisterService } from '../register.service';
import { Router, RouterLink } from '@angular/router';
import { startRegistration, PublicKeyCredentialCreationOptionsJSON } from '@simplewebauthn/browser';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
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
    CardModule,
    InputTextModule,
    ToastModule,
  ],
  providers: [MessageService],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  username = '';
  loading = false;

  constructor(
    private registerService: RegisterService,
    private router: Router,
    private messageService: MessageService,
  ) {}

  onSubmit(): void {
    if (!this.username.trim()) {
      return;
    }
    this.loading = true;
    this.registerService.register(this.username).subscribe({
      next: options => {
        startRegistration({ optionsJSON: options as PublicKeyCredentialCreationOptionsJSON })
          .then(response => {
            this.registerService.verify(this.username, response).subscribe({
              next: result => {
                const r = result as any;
                if (r?.verified === true || r?.verified === 'true') {
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
                detail: 'Passkey sign-in was cancelled. Try again when ready.',
                life: 4000
              });
            }
          });
      },
      error: () => { this.loading = false; }
    });
  }
}
