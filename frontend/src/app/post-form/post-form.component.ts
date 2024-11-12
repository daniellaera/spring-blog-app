import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {PostDTO} from '../dto/post.dto';
import {FormsModule} from '@angular/forms';
import {NgClass, NgIf} from '@angular/common';
import {Button, ButtonDirective} from 'primeng/button';
import {PostService} from '../post.service';
import {ChipsModule} from 'primeng/chips';
import {SplitButtonModule} from 'primeng/splitbutton';

@Component({
  selector: 'app-post-form',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    ButtonDirective,
    Button,
    ChipsModule,
    NgClass,
    SplitButtonModule
  ],
  templateUrl: './post-form.component.html',
  styleUrl: './post-form.component.scss'
})
export class PostFormComponent implements OnChanges {
  @Input() post: PostDTO | null = null;
  @Output() postUpdated = new EventEmitter<PostDTO>();
  @Output() postDeleted = new EventEmitter<number>();
  @Output() postAdded = new EventEmitter<PostDTO>();

  title: string = '';
  content: string = '';

  constructor(private postService: PostService) {}

  ngOnChanges(): void {
    if (this.post) {
      this.title = this.post.title;
      this.content = this.post.content;
    }
  }

  // Handle Add Post logic
  onAddPost(): void {
    const newPost: PostDTO = { title: this.title, content: this.content, comments: [] };
    this.postService.createPost(newPost).subscribe({
      next: (createdPost: PostDTO) => {
        this.postAdded.emit(createdPost);
        this.clearForm();
      },
      error: (err: any) => {
        console.error('Error creating post:', err);
      }
    });
  }

  // Handle Update Post logic
  onUpdatePost(): void {
    if (this.post && this.post.id) {
      const updatedPost: PostDTO = { ...this.post, title: this.title, content: this.content };
      this.postService.updatePost(updatedPost).subscribe({
        next: (updated: PostDTO) => {
          this.postUpdated.emit(updated);
          this.clearForm();
        },
        error: (err: any) => {
          console.error('Error updating post:', err);
        }
      });
    } else {
      console.error('Post ID is undefined. Cannot update post.');
    }
  }

  // Handle Delete Post logic
  onDeletePost(): void {
    if (this.post && this.post.id !== undefined) {
      const postId = this.post.id;
      this.postService.deletePost(postId).subscribe({
        next: () => {
          this.postDeleted.emit(postId);
          this.clearForm();
        },
        error: (err: any) => {
          console.error('Error deleting post:', err);
        }
      });
    }
  }

  // Clear the form fields after a post is created or updated
  private clearForm(): void {
    this.title = '';
    this.content = '';
  }
}
