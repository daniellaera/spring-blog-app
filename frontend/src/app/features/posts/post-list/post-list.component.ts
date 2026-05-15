import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PostService } from '../../../core/services/post.service';
import { LoginService } from '../../../core/services/login.service';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { CardModule } from 'primeng/card';
import { SkeletonModule } from 'primeng/skeleton';
import { PostDTO } from '../../../dto/post.dto';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    RouterLink,
    ButtonModule,
    AvatarModule,
    CardModule,
    SkeletonModule,
  ],
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.scss']
})
export class PostListComponent implements OnInit {
  private loginService = inject(LoginService);
  private postService = inject(PostService);

  posts: PostDTO[] = [];
  loading = false;
  isLoggedIn = this.loginService.isLoggedIn;

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts(): void {
    this.loading = true;
    this.postService.getAll().pipe(
      timeout(10000),
      catchError(() => {
        this.loading = false;
        return of([]);
      })
    ).subscribe(posts => {
      this.posts = posts.map(post => ({ ...post, comments: post.comments || [] }));
      this.loading = false;
    });
  }
}
