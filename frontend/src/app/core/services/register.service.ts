import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PublicKeyCredentialCreationOptionsJSON} from '@simplewebauthn/browser';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {

  constructor(private http: HttpClient) { }

  register(username: string): Observable<PublicKeyCredentialCreationOptionsJSON> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      })
    };

    const body = {
      'username': username
    };

    return this.http.post<PublicKeyCredentialCreationOptionsJSON>('http://localhost:8080/register/start', body, httpOptions).pipe();
  }

  verify(username: any, attestationResponse: any) {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      })
    };

    const body = {
      'username': username,
      'response': attestationResponse
    };

    return this.http.post('http://localhost:8080/register/verify', body, httpOptions).pipe();
  }
}
