import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommentService} from '../comment.service';
import {FormsModule, NgForm} from '@angular/forms';
import {NgIf} from '@angular/common';
import {MessageModule} from 'primeng/message';
import {ButtonModule} from 'primeng/button';
import {InputTextareaModule} from 'primeng/inputtextarea';

@Component({
  selector: 'app-comment-form',
  standalone: true,
  imports: [
    FormsModule,
    InputTextareaModule,
    ButtonModule,
    MessageModule,
    NgIf,
  ],
  templateUrl: './comment-form.component.html',
  styleUrl: './comment-form.component.scss'
})
export class CommentFormComponent {

  @Input() postId!: number; // Post ID to associate the comment with
  @Output() commentAdded = new EventEmitter<any>(); // Emit when a comment is added
  commentText: string = ''; // Model for the comment text

  constructor(private commentService: CommentService) {}

  onSubmit(form: NgForm): void {
    if (this.postId && this.commentText.trim()) {
      const newComment = { text: this.commentText.trim() };

      this.commentService.createComment(this.postId, newComment).subscribe({
        next: (comment) => {
          this.commentAdded.emit(comment); // Emit the new comment to parent
          form.reset(); // Reset the form
        },
        error: (err) => {
          console.error('Error adding comment:', err);
        },
      });
    }
  }

}
