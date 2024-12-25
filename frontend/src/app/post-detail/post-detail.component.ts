import { Component } from '@angular/core';
import {PostDTO} from '../dto/post.dto';
import {ActivatedRoute, Router} from '@angular/router';
import {PostService} from '../post.service';
import {PostFormComponent} from '../post-form/post-form.component';
import {NgForOf, NgIf} from '@angular/common';
import {CommentFormComponent} from '../comment-form/comment-form.component';
import {CommentDTO} from '../dto/comment.dto';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [
    PostFormComponent,
    NgIf,
    NgForOf,
    CommentFormComponent,
  ],
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.scss'
})
export class PostDetailComponent {
  post: PostDTO | null = null;
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const postId = this.route.snapshot.paramMap.get('postId');
    if (postId) {
      this.loadPost(+postId);
    }
  }

  loadPost(postId: number): void {
    this.postService.getPostById(postId).subscribe({
      next: (post) => {
        this.post = post;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching post:', err);
        this.loading = false;
      },
    });
  }

  onUpdatePost(updatedPost: PostDTO): void {
    if (updatedPost.id) {
      this.postService.updatePost(updatedPost).subscribe({
        next: (updated) => {
          this.post = updated;
        },
        error: (err) => {
          console.error('Error updating post:', err);
        },
      });
    }
  }

  onDeletePost(postId: number | undefined): void {
    this.postService.deletePost(postId).subscribe({
      next: () => {
        // Navigate back to the post list
        console.log('Post deleted successfully');
        this.router.navigate(['/']).then(r => console.log('routed to home'));
      },
      error: (err) => {
        console.error('Error deleting post:', err);
      },
    });
  }

  // Navigate back to the home page
  navigateHome(): void {
    this.router.navigate(['/']).then(() => console.log('Navigated to home'));
  }

  onCommentAdded(newComment: CommentDTO): void {
    if (this.post) {
      this.post.comments.push(newComment); // Add the new comment to the post's comments list
    }
  }
}
