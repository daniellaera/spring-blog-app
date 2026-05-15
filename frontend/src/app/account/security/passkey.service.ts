import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, firstValueFrom } from 'rxjs';
import { PasskeyDto } from './passkey.dto';
import { LoginService } from '../../core/services/login.service';
import { RegisterService } from '../../core/services/register.service';
import { startRegistration } from '@simplewebauthn/browser';

@Injectable({ providedIn: 'root' })
export class PasskeyService {
  constructor(
    private http: HttpClient,
    private loginService: LoginService,
    private registerService: RegisterService
  ) {}

  list(): Observable<PasskeyDto[]> {
    return this.http.get<PasskeyDto[]>('http://localhost:8080/api/user/passkeys', { withCredentials: true });
  }

  rename(id: string, deviceName: string): Observable<PasskeyDto> {
    return this.http.patch<PasskeyDto>(
      `http://localhost:8080/api/user/passkeys/${id}`,
      { deviceName },
      { withCredentials: true }
    );
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`http://localhost:8080/api/user/passkeys/${id}`, { withCredentials: true });
  }

  cleanupOrphaned(): Observable<{ removed: number }> {
    return this.http.delete<{ removed: number }>(
      'http://localhost:8080/api/user/passkeys/orphaned',
      { withCredentials: true }
    );
  }

  async register(): Promise<void> {
    const session = await firstValueFrom(this.loginService.getSession());
    const options = await firstValueFrom(this.registerService.register(session.username));
    const response = await startRegistration({ optionsJSON: options });
    await firstValueFrom(this.registerService.verify(session.username, response));
  }
}
