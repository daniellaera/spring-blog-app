import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PublicKeyCredentialRequestOptionsJSON } from '@simplewebauthn/browser';

const TOKEN_KEY = 'username';

export interface UserSession {
  username: string;
  lastLogin: string | null;
  passkeys: Passkey[];
}

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

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private loggedIn = signal(!!localStorage.getItem(TOKEN_KEY));
  isLoggedIn = this.loggedIn.asReadonly();

  private user = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  currentUser = this.user.asReadonly();

  constructor(private http: HttpClient) {}

  startLogin(username: string): Observable<PublicKeyCredentialRequestOptionsJSON> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      })
    };
    return this.http.post<PublicKeyCredentialRequestOptionsJSON>(
      'http://localhost:8080/login/start',
      { username },
      httpOptions
    );
  }

  verify(username: string, assertionResponse: any) {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }),
      withCredentials: true
    };
    return this.http.post(
      'http://localhost:8080/login/verify',
      { username, response: assertionResponse },
      httpOptions
    );
  }

  getSession(): Observable<UserSession> {
    return this.http.get<UserSession>(
      'http://localhost:8080/session/me',
      { withCredentials: true }
    );
  }

  login(token: string, username: string): void {
    localStorage.setItem(TOKEN_KEY, username);
    this.loggedIn.set(true);
    this.user.set(username);
  }

  setSession(session: UserSession | null): void {
    if (session) {
      localStorage.setItem(TOKEN_KEY, session.username);
      this.user.set(session.username);
      this.loggedIn.set(true);
    } else {
      this.logout();
    }
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.loggedIn.set(false);
    this.user.set(null);
  }
}
