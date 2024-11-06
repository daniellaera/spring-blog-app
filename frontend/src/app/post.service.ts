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
  private apiUrl = environment.apiUrl;  // The API endpoint

  constructor(private http: HttpClient) {
    fetch(environment.apiUrl).then(response => response.json()).then(data => {
      console.log('env->', environment.apiUrl)
    });
  }

  // This method fetches posts from the backend API
  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(this.apiUrl);  // Making a GET request to the backend API
  }
}
