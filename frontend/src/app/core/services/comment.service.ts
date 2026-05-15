import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { CommentDTO } from '../../dto/comment.dto';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = `${environment.apiUrl}/api/v3/comment`;

  constructor(private http: HttpClient) {}

  getComments(postId: number): Observable<CommentDTO[]> {
    return this.http.get<CommentDTO[]>(`${this.apiUrl}/${postId}/comments`).pipe(
      catchError(err => err.status === 404 ? of([]) : of([]))
    );
  }

  createComment(postId: number, comment: { text: string }): Observable<CommentDTO> {
    return this.http.post<CommentDTO>(`${this.apiUrl}/${postId}`, comment);
  }
}
