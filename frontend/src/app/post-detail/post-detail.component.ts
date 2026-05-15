import { Component, OnInit } from '@angular/core';
import { PostDTO } from '../dto/post.dto';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../post.service';
import { LoginService, UserSession } from '../login.service';
import { PostFormComponent } from '../post-form/post-form.component';
import { CommentFormComponent } from '../comment-form/comment-form.component';
import { CommentDTO } from '../dto/comment.dto';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { AvatarModule } from 'primeng/avatar';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [
    PostFormComponent,
    CommentFormComponent,
    DatePipe,
    ButtonModule,
    CardModule,
    DividerModule,
    AvatarModule,
  ],
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.scss'
})
export class PostDetailComponent implements OnInit {
  post: PostDTO | null = null;
  loading = true;
  session: UserSession | null = null;
  showEdit = false;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    private loginService: LoginService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginService.getSession().subscribe({
      next: s => { this.session = s; },
      error: () => { this.session = null; }
    });
    const postId = this.route.snapshot.paramMap.get('postId');
    if (postId) {
      this.loadPost(+postId);
    }
  }

  get isAuthor(): boolean {
    return !!this.session && !!this.post?.author && this.session.username === this.post.author;
  }

  get authorInitial(): string {
    return this.post?.author?.charAt(0).toUpperCase() || '?';
  }

  loadPost(postId: number): void {
    this.postService.getPostById(postId).subscribe({
      next: post => {
        this.post = post;
        this.loading = false;
      },
      error: err => {
        console.error('Error fetching post:', err);
        this.loading = false;
      }
    });
  }

  onUpdatePost(updatedPost: PostDTO): void {
    if (updatedPost.id) {
      this.postService.updatePost(updatedPost).subscribe({
        next: updated => {
          this.post = updated;
          this.showEdit = false;
        },
        error: err => console.error('Error updating post:', err)
      });
    }
  }

  onDeletePost(postId: number | undefined): void {
    this.postService.deletePost(postId).subscribe({
      next: () => this.router.navigate(['/']),
      error: err => console.error('Error deleting post:', err)
    });
  }

  navigateBack(): void {
    this.router.navigate(['/']);
  }

  onCommentAdded(newComment: CommentDTO): void {
    if (this.post) {
      this.post.comments.push(newComment);
    }
  }
}
