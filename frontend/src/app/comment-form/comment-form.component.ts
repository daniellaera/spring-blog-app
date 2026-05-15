import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommentService } from '../comment.service';
import { FormsModule, NgForm } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-comment-form',
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    TextareaModule,
    MessageModule,
  ],
  templateUrl: './comment-form.component.html',
  styleUrl: './comment-form.component.scss'
})
export class CommentFormComponent {
  @Input() postId!: number;
  @Output() commentAdded = new EventEmitter<any>();
  commentText = '';
  submitting = false;

  constructor(private commentService: CommentService) {}

  onSubmit(form: NgForm): void {
    if (this.postId && this.commentText.trim()) {
      this.submitting = true;
      this.commentService.createComment(this.postId, { text: this.commentText.trim() }).subscribe({
        next: comment => {
          this.commentAdded.emit(comment);
          form.reset();
          this.submitting = false;
        },
        error: err => {
          console.error('Error adding comment:', err);
          this.submitting = false;
        }
      });
    }
  }
}
