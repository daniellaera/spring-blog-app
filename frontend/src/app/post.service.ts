import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../environments/environment';

interface Post {
  title: string;
  content: string;
}

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private apiUrl = `${environment.apiUrl}/api/v3/post`;  // The API endpoint

  constructor(private http: HttpClient) {
      console.log('env->', this.apiUrl)
  }

  // This method fetches posts from the backend API
  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(this.apiUrl);  // Making a GET request to the backend API
  }
}
