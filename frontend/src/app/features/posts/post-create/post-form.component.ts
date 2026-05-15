import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { PostDTO } from '../../../dto/post.dto';
import { FormsModule } from '@angular/forms';
import { PostService } from '../../../core/services/post.service';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';

@Component({
  selector: 'app-post-form',
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    InputTextModule,
    TextareaModule,
  ],
  templateUrl: './post-form.component.html',
  styleUrl: './post-form.component.scss'
})
export class PostFormComponent implements OnChanges {
  @Input() post: PostDTO | null = null;
  @Output() postUpdated = new EventEmitter<PostDTO>();
  @Output() postDeleted = new EventEmitter<number>();
  @Output() postAdded = new EventEmitter<PostDTO>();

  title = '';
  content = '';
  saving = false;

  constructor(private postService: PostService) {}

  ngOnChanges(): void {
    if (this.post) {
      this.title = this.post.title;
      this.content = this.post.content;
    }
  }

  onAddPost(): void {
    const newPost: PostDTO = { title: this.title, content: this.content, comments: [] };
    this.saving = true;
    this.postService.create(newPost).subscribe({
      next: (createdPost: PostDTO) => {
        this.postAdded.emit(createdPost);
        this.clearForm();
        this.saving = false;
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  onUpdatePost(): void {
    if (this.post?.id) {
      const updatedPost: PostDTO = { ...this.post, title: this.title, content: this.content };
      this.saving = true;
      this.postService.update(this.post.id, updatedPost).subscribe({
        next: (updated: PostDTO) => {
          this.postUpdated.emit(updated);
          this.clearForm();
          this.saving = false;
        },
        error: () => {
          this.saving = false;
        }
      });
    }
  }

  onDeletePost(): void {
    if (this.post?.id !== undefined) {
      const postId = this.post.id;
      this.postService.delete(postId).subscribe({
        next: () => {
          this.postDeleted.emit(postId);
          this.clearForm();
        },
        error: () => {}
      });
    }
  }

  private clearForm(): void {
    this.title = '';
    this.content = '';
  }
}
