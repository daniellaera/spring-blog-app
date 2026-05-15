import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { PostDTO } from '../../dto/post.dto';

@Injectable({ providedIn: 'root' })
export class PostService {
  private apiUrl = `${environment.apiUrl}/api/v3/post`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(this.apiUrl);
  }

  getById(id: number): Observable<PostDTO> {
    return this.http.get<PostDTO>(`${this.apiUrl}/${id}`);
  }

  create(data: Partial<PostDTO>): Observable<PostDTO> {
    return this.http.post<PostDTO>(this.apiUrl, data, { withCredentials: true });
  }

  update(id: number, data: Partial<PostDTO>): Observable<PostDTO> {
    return this.http.put<PostDTO>(`${this.apiUrl}/${id}`, data, { withCredentials: true });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }
}
