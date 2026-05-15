import { Component, Input, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { CommentService } from '../../../../core/services/comment.service';
import { LoginService } from '../../../../core/services/login.service';
import { CommentDTO } from '../../../../dto/comment.dto';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { ToastModule } from 'primeng/toast';
import { AvatarModule } from 'primeng/avatar';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-comment-form',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    DatePipe,
    ButtonModule,
    TextareaModule,
    ToastModule,
    AvatarModule,
  ],
  providers: [MessageService],
  templateUrl: './comment-form.component.html',
  styleUrl: './comment-form.component.scss'
})
export class CommentFormComponent implements OnInit {
  @Input() postId!: number;

  private commentService = inject(CommentService);
  private loginService = inject(LoginService);
  private messageService = inject(MessageService);

  isLoggedIn = this.loginService.isLoggedIn;
  comments: CommentDTO[] = [];
  newComment = '';
  submitting = false;

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.commentService.getComments(this.postId).subscribe(comments => {
      this.comments = comments;
    });
  }

  submitComment(): void {
    if (!this.postId || !this.newComment.trim()) return;
    this.submitting = true;
    this.commentService.createComment(this.postId, { text: this.newComment.trim() }).subscribe({
      next: comment => {
        this.comments = [...this.comments, comment];
        this.newComment = '';
        this.submitting = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Comment posted',
          detail: 'Your comment has been added.',
          life: 3000
        });
      },
      error: () => {
        this.submitting = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to post comment. Please try again.',
          life: 3000
        });
      }
    });
  }
}
