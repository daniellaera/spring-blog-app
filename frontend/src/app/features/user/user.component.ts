import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { LoginService, UserSession } from '../../core/services/login.service';

interface Passkey {
  authenticatorAttachment: string;
  transports: string[];
  signatureCount: number;
  userPresence: boolean;
  userVerification: boolean;
  residentKey: boolean;
  keyId: string;
  publicKey: string;
  clientDataJSON: string;
  attestationObject: string;
  origin: string;
  showDebug?: boolean;
}

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    CardModule,
    TagModule,
    TableModule,
    ButtonModule,
  ],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss'
})
export class UserComponent implements OnInit {
  session: UserSession | null = null;

  constructor(private sessionService: LoginService) {}

  ngOnInit() {
    this.sessionService.getSession().subscribe({
      next: s => { this.session = s; },
      error: e => console.error('Not logged in', e)
    });
  }

  toggleDebug(passkey: Passkey) {
    passkey['showDebug'] = !passkey['showDebug'];
  }
}
