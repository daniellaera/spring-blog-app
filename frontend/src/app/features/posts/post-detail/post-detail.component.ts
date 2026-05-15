import { Component, OnInit, inject } from '@angular/core';
import { PostDTO } from '../../../dto/post.dto';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PostService } from '../../../core/services/post.service';
import { LoginService } from '../../../core/services/login.service';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { SkeletonModule } from 'primeng/skeleton';
import { DividerModule } from 'primeng/divider';
import { MessageService, ConfirmationService } from 'primeng/api';
import { CommentFormComponent } from './comment-form/comment-form.component';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [
    RouterLink,
    ButtonModule,
    AvatarModule,
    ToastModule,
    ConfirmDialogModule,
    SkeletonModule,
    DividerModule,
    CommentFormComponent,
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.scss'
})
export class PostDetailComponent implements OnInit {
  private loginService = inject(LoginService);
  private postService = inject(PostService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private confirmationService = inject(ConfirmationService);
  private messageService = inject(MessageService);

  currentUser = this.loginService.currentUser;

  post: PostDTO | null = null;
  loading = true;

  ngOnInit(): void {
    const id = this.route.snapshot.params['postId'];
    if (id) {
      this.postService.getById(+id).pipe(
        catchError(() => { this.loading = false; return of(null); })
      ).subscribe(post => {
        this.post = post;
        this.loading = false;
      });
    }
  }

  isAuthor(): boolean {
    return !!this.post?.author && this.currentUser() === this.post.author;
  }

  confirmDelete(): void {
    this.confirmationService.confirm({
      message: 'Are you sure you want to delete this post?',
      header: 'Delete Post',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        if (this.post?.id) {
          this.postService.delete(this.post.id).subscribe({
            next: () => {
              this.messageService.add({
                severity: 'success',
                summary: 'Deleted',
                detail: 'Post deleted.',
                life: 2000
              });
              setTimeout(() => this.router.navigate(['/']), 1000);
            },
            error: () => {}
          });
        }
      }
    });
  }
}
