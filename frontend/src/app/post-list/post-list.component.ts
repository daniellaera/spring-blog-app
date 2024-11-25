import { Component, OnInit } from '@angular/core';
import { PostService } from '../post.service';
import { PostFormComponent } from '../post-form/post-form.component';
import {NgForOf, NgIf} from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonDirective } from 'primeng/button';
import {PostDTO} from '../dto/post.dto';

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    NgForOf,
    CardModule,
    ButtonDirective,
    PostFormComponent,
    NgIf,
    // Import the form component
  ],
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.scss']
})
export class PostListComponent implements OnInit {
  posts: PostDTO[] = [];
  selectedPost: PostDTO | null = null; // To keep track of the selected post for editing
  loading: boolean = false; // Track loading state

  constructor(private postService: PostService) { }

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts() {
    this.loading = true; // Start loader
    this.postService.getPosts().subscribe({
      next: (data) => {
        this.posts = data.map(post => ({
          ...post,
          comments: post.comments || []  // Ensure comments is always an array
        }));
      },
      complete: (() => {
        this.loading = false; // Stop loader
      }),
      error: (err) => {
        console.error('Error fetching posts:', err);
        this.loading = false; // Stop loader
      }
    });
  }

  onUpdatePost(post: PostDTO): void {
    this.selectedPost = { ...post };  // Set selected post for editing
  }

  onPostUpdated(updatedPost: PostDTO): void {
    const index = this.posts.findIndex(post => post.id === updatedPost.id);
    if (index !== -1) {
      this.posts[index] = updatedPost;  // Update the post in the list
    }
  }

  onPostAdded(newPost: PostDTO): void {
    this.posts.push(newPost);  // Add a new post to the list
  }

  onPostDeleted(postId: number): void {
    this.posts = this.posts.filter(post => post.id !== postId);  // Remove the post from the list
  }
}
