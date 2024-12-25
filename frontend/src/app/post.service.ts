import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../environments/environment';
import {PostDTO} from './dto/post.dto';

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private apiUrl = `${environment.apiUrl}/api/v3/post`;  // The API endpoint

  constructor(private http: HttpClient) {
      console.log('environment.apiUrl->', environment.apiUrl)
      console.log('this.apiUrl->', this.apiUrl)
  }

  // This method fetches posts from the backend API
  getPosts(): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(this.apiUrl);  // Making a GET request to the backend API
  }

  getPostById(postId: number): Observable<PostDTO> {
    return this.http.get<PostDTO>(`${this.apiUrl}/${postId}`);
  }

  // Create a new post
  createPost(post: PostDTO): Observable<PostDTO> {
    return this.http.post<PostDTO>(this.apiUrl, post);  // POST request to create a new post
  }

  // Update an existing post
  // PostService
  updatePost(post: PostDTO): Observable<PostDTO> {
    if (post.id === undefined) {
      throw new Error('Post ID is required to update.');
    }
    return this.http.put<PostDTO>(`${this.apiUrl}/${post.id}`, post);
  }

  // Delete a post by its ID
  deletePost(postId: number | undefined): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${postId}`);  // DELETE request to delete a post
  }
}
