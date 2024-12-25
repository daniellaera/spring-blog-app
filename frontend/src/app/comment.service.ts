import { Injectable } from '@angular/core';
import {environment} from '../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CommentDTO} from './dto/comment.dto';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = `${environment.apiUrl}/api/v3/comment`;  // The API endpoint

  constructor(private http: HttpClient) {
  }

  createComment(postId: number, comment: { text: string }) {
    return this.http.post<CommentDTO>(`${this.apiUrl}/${postId}`, comment);
  }
}
